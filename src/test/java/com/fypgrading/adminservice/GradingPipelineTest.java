package com.fypgrading.adminservice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fypgrading.adminservice.clients.DemoCopyClient;
import com.fypgrading.adminservice.clients.EvaluationClient;
import com.jayway.jsonpath.JsonPath;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.rabbitmq.RabbitMQContainer;

/**
 * Plan item 1.6: the event-driven grading pipeline end to end, on real Postgres and RabbitMQ. Every evaluation of
 * a team is handed over; the aggregation must produce each team grade, the final grade and the
 * notifications, exactly once, even when a hand-off is retried. The other services are stubbed at the HTTP edge.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class GradingPipelineTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:4.0-management");

    static final UUID RANIA = UUID.fromString("00000000-0000-4000-a000-000000000001");
    static final UUID JANE = UUID.fromString("00000000-0000-4000-a000-000000000002");
    static final UUID JOHN = UUID.fromString("00000000-0000-4000-a000-000000000003");
    static final UUID EMILY = UUID.fromString("00000000-0000-4000-a000-000000000005");
    static final UUID KARIM = UUID.fromString("00000000-0000-4000-a000-000000000007");
    static final UUID NOUR = UUID.fromString("00000000-0000-4000-a000-000000000101");
    static final UUID AHMAD = UUID.fromString("00000000-0000-4000-a000-000000000102");

    @Autowired
    MockMvc mvc;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RabbitAdmin rabbitAdmin;

    @Autowired
    org.springframework.jdbc.core.JdbcTemplate jdbc;

    @MockitoBean
    EvaluationClient evaluations;

    @MockitoBean
    DemoCopyClient otherServices;

    UUID session = UUID.randomUUID();
    Queue notifications;

    static JwtRequestPostProcessor as(UUID id, String... roles) {
        SimpleGrantedAuthority[] authorities = java.util.Arrays.stream(roles)
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .toArray(SimpleGrantedAuthority[]::new);
        return jwt().jwt(
                        j -> j.subject(id.toString()).claim("name", "Test User").claim("preferred_username", "x"))
                .authorities(authorities);
    }

    @BeforeEach
    void listenToNotifications() {
        TopicExchange exchange = new TopicExchange("assay.notifications");
        rabbitAdmin.declareExchange(exchange);
        notifications = new AnonymousQueue();
        rabbitAdmin.declareQueue(notifications);
        Binding binding = BindingBuilder.bind(notifications).to(exchange).with("#");
        rabbitAdmin.declareBinding(binding);
    }

    private List<String> drainNotifications(int atLeast) {
        List<String> received = new ArrayList<>();
        await().atMost(Duration.ofSeconds(20)).until(() -> {
            Message m;
            while ((m = rabbitTemplate.receive(notifications.getName())) != null) {
                received.add(new String(m.getBody(), StandardCharsets.UTF_8));
            }
            return received.size() >= atLeast;
        });
        return received;
    }

    private void grade(UUID team, String assessment, UUID reviewer, double score, UUID evaluationId) throws Exception {
        handIn(team, assessment, reviewer, score, evaluationId).andExpect(status().isOk());
    }

    /** The late flag is decided by evaluation-service; here the advisor's grades are the late ones. */
    private org.springframework.test.web.servlet.ResultActions handIn(
            UUID team, String assessment, UUID reviewer, double score, UUID evaluationId) throws Exception {
        return mvc.perform(post("/internal/grades")
                .header("X-Demo-Session", session)
                .with(as(reviewer, "REVIEWER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(new tools.jackson.databind.json.JsonMapper()
                        .writeValueAsString(Map.of(
                                "evaluationId",
                                evaluationId,
                                "teamId",
                                team,
                                "assessment",
                                assessment,
                                "reviewerId",
                                reviewer,
                                "score",
                                score,
                                "submittedAt",
                                "2026-06-10T08:00:00Z",
                                "submittedLate",
                                "ADVISOR_ASSESSMENT".equals(assessment)))));
    }

    @Test
    void everyEvaluationOfATeamFlowsIntoTeamGradesAFinalGradeAndNotificationsExactlyOnce() throws Exception {
        String created = mvc.perform(post("/api/teams")
                        .header("X-Demo-Session", session)
                        .with(as(RANIA, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Omega\", \"projectTitle\": \"Pipeline test\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.composition.valid").value(false))
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID team = UUID.fromString(JsonPath.read(created, "$.id"));
        // Plan 3.4: the row records the coordinator who created it, from the token (it used to say SYSTEM).
        assertThat(jdbc.queryForObject("select created_by from team where id = ?", UUID.class, team))
                .isEqualTo(RANIA);

        mvc.perform(put("/api/teams/" + team + "/reviewers")
                        .header("X-Demo-Session", session)
                        .with(as(RANIA, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignments\": [{\"reviewerId\": \"%s\", \"role\": \"JURY\"}]}".formatted(JANE)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.code").value("composition-invalid"))
                .andExpect(jsonPath("$.problems[0].code").value("NO_ADVISOR"));
        mvc.perform(put("/api/teams/" + team + "/reviewers")
                        .header("X-Demo-Session", session)
                        .with(as(RANIA, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"assignments": [{"reviewerId": "%s", "role": "ADVISOR"},
                                                 {"reviewerId": "%s", "role": "JURY"},
                                                 {"reviewerId": "%s", "role": "JURY"}]}""".formatted(EMILY, JANE, JOHN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.composition.valid").value(true));
        drainNotifications(3); // the three assignment notices

        // A due date in the past: every grade handed in below is a late submission, and stays marked as one.
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch(
                                "/api/assessments/ADVISOR_ASSESSMENT")
                        .header("X-Demo-Session", session)
                        .with(as(RANIA, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dueAt\": \"2026-06-01T21:59:00Z\"}"))
                .andExpect(status().isOk());

        // A jury member can't hand in the advisor's assessment.
        mvc.perform(post("/internal/grades")
                        .header("X-Demo-Session", session)
                        .with(as(JANE, "REVIEWER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"evaluationId": "%s", "teamId": "%s", "assessment": "ADVISOR_ASSESSMENT",
                                 "reviewerId": "%s", "score": 90, "submittedAt": "2026-06-10T08:00:00Z",
                                 "submittedLate": false}""".formatted(UUID.randomUUID(), team, JANE)))
                .andExpect(status().isForbidden());

        for (String jury : List.of("PROPOSAL_PRESENTATION", "PROGRESS_REPORT", "FINAL_REPORT", "FINAL_PRESENTATION")) {
            UUID janeEvaluation = UUID.randomUUID();
            grade(team, jury, JANE, 80.0, janeEvaluation);
            grade(team, jury, JANE, 80.0, janeEvaluation); // a retried hand-off: recognised, not counted twice
            // The same evaluation handed in again with another score is refused, not silently ignored.
            handIn(team, jury, JANE, 75.0, janeEvaluation)
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("grade-mismatch"));
            grade(team, jury, JOHN, 90.0, UUID.randomUUID());
        }
        grade(team, "ADVISOR_ASSESSMENT", EMILY, 70.0, UUID.randomUUID());

        // Aggregation runs with the hand-off (review C2), so the grades are there as soon as it returns.
        // 15% x 85 + 0% x 85 + 25% x 70 + 30% x 85 + 30% x 85 = 81.25, shown 81.3
        mvc.perform(get("/api/teams/" + team).header("X-Demo-Session", session).with(as(RANIA, "ADMIN")))
                .andExpect(jsonPath("$.finalGrade").value(81.3))
                .andExpect(jsonPath("$.finalState").value("COMPUTED"))
                .andExpect(jsonPath("$.assessments[0].grade").value(85.0))
                .andExpect(jsonPath("$.assessments[0].submittedCount").value(2))
                .andExpect(jsonPath("$.assessments[0].spread").value(10.0))
                .andExpect(
                        jsonPath("$.assessments[2].reviewers[0].submittedLate").value(true))
                .andExpect(jsonPath("$.assessments[2].reviewers[0].overdue").value(false));

        finalGradesCantChange(team);

        List<String> sent = drainNotifications(7); // 1 overdue + 5 grades final + the final grade
        assertThat(sent.stream().filter(n -> n.contains("\"GRADE_FINAL\""))).hasSize(5);
        assertThat(sent.stream().filter(n -> n.contains("\"FINAL_GRADE_COMPUTED\"")))
                .hasSize(1);
        assertThat(sent).anyMatch(n -> n.contains("Team Omega's final grade is computed: 81.3."));
        assertThat(sent.stream().filter(n -> n.contains("GRADE_FINAL") || n.contains("FINAL_GRADE_COMPUTED")))
                .allMatch(n -> n.contains(RANIA.toString()) && n.contains(JANE.toString()));
        // The past due date told the one reviewer still owing an evaluation at that moment (Emily) it's overdue.
        assertThat(sent).anyMatch(n -> n.contains("\"OVERDUE\"") && n.contains(EMILY.toString()));
    }

    @Test
    void theSeedMatchesTheDesignAndTheReleaseFlowReachesStudents() throws Exception {
        mvc.perform(get("/api/demo/personas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[4].username").value("ahmad.fares"));
        mvc.perform(put("/api/demo/session").header("X-Demo-Session", session).with(as(RANIA, "ADMIN")))
                .andExpect(status().isCreated());
        mvc.perform(put("/api/demo/session").header("X-Demo-Session", session).with(as(JANE, "REVIEWER")))
                .andExpect(status().isOk());

        String overview = mvc.perform(
                        get("/api/overview").header("X-Demo-Session", session).with(as(RANIA, "ADMIN")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        assertThat(JsonPath.<List<Double>>read(overview, "$.teams[?(@.team.name == 'Alpha')].finalGrade"))
                .containsExactly(88.0);
        assertThat(JsonPath.<List<Double>>read(overview, "$.teams[?(@.team.name == 'Gamma')].finalGrade"))
                .containsExactly(92.2);
        assertThat(JsonPath.<List<Double>>read(overview, "$.teams[?(@.team.name == 'Alpha')].cells[0].grade"))
                .containsExactly(86.7);
        assertThat(JsonPath.<List<Double>>read(overview, "$.teams[?(@.team.name == 'Beta')].cells[0].spread"))
                .containsExactly(21.3);
        assertThat(JsonPath.<List<String>>read(overview, "$.teams[?(@.team.name == 'Beta')].cells[3].state"))
                .containsExactly("WAITING");
        assertThat(JsonPath.<List<String>>read(overview, "$.teams[?(@.team.name == 'Alpha')].finalState"))
                .containsExactly("VISIBLE");

        assertInsights(overview);

        mvc.perform(get("/api/me/assignments").header("X-Demo-Session", session).with(as(JANE, "REVIEWER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teams.length()").value(3));

        // A reviewer opens a team they're on: other reviewers' scores stay hidden until the grade is final.
        String beta = JsonPath.<List<String>>read(overview, "$.teams[?(@.team.name == 'Beta')].team.id")
                .getFirst();
        String gamma = JsonPath.<List<String>>read(overview, "$.teams[?(@.team.name == 'Gamma')].team.id")
                .getFirst();
        String asJane = mvc.perform(get("/api/teams/" + beta)
                        .header("X-Demo-Session", session)
                        .with(as(JANE, "REVIEWER")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        List<Object> hiddenFinalReport =
                JsonPath.read(asJane, "$.assessments[3].reviewers[?(@.status == 'SUBMITTED')].score");
        assertThat(hiddenFinalReport).hasSize(2).containsOnlyNulls();
        assertThat(JsonPath.<Object>read(asJane, "$.assessments[3].spread")).isNull();
        List<Object> visibleProposal = JsonPath.read(asJane, "$.assessments[0].reviewers[*].score");
        assertThat(visibleProposal).hasSize(3).doesNotContainNull();
        mvc.perform(get("/api/teams/" + gamma).header("X-Demo-Session", session).with(as(JANE, "REVIEWER")))
                .andExpect(status().isForbidden());

        // Late submissions stay marked: Delta's progress report was handed in before its due date, so it isn't late.
        String delta = JsonPath.<List<String>>read(overview, "$.teams[?(@.team.name == 'Delta')].team.id")
                .getFirst();
        mvc.perform(get("/api/teams/" + delta).header("X-Demo-Session", session).with(as(RANIA, "ADMIN")))
                .andExpect(
                        jsonPath("$.assessments[1].reviewers[0].submittedLate").value(false))
                .andExpect(jsonPath("$.assessments[2].reviewers[0].overdue").value(true));

        mvc.perform(get("/api/me/team").header("X-Demo-Session", session).with(as(NOUR, "STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.team.name").value("Alpha"))
                .andExpect(jsonPath("$.finalGradeVisible").value(true))
                .andExpect(jsonPath("$.finalGrade").value(88.0))
                .andExpect(jsonPath("$.timeline[4].released").value(true));

        mvc.perform(post("/api/assessments/ADVISOR_ASSESSMENT/release")
                        .header("X-Demo-Session", session)
                        .with(as(RANIA, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.releasedTeamIds.length()").value(2)) // Beta and Epsilon were final
                .andExpect(jsonPath("$.alreadyReleasedTeamIds.length()").value(2));
        assertThat(jdbc.queryForObject(
                        "select released_by from team_assessment where team_id = ? and assessment = 'ADVISOR_ASSESSMENT'",
                        UUID.class,
                        UUID.fromString(beta)))
                .isEqualTo(RANIA);
        mvc.perform(post("/api/assessments/ADVISOR_ASSESSMENT/release")
                        .header("X-Demo-Session", session)
                        .with(as(RANIA, "ADMIN")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("nothing-to-release"));

        mvc.perform(get("/api/schedule").header("X-Demo-Session", session).with(as(RANIA, "ADMIN")))
                .andExpect(jsonPath("$.slots.length()").value(6))
                .andExpect(jsonPath("$.conflicts[0].reviewer.displayName").value("John Doe"));
    }

    @Test
    void theSecondStudentPersonaSeesTheWaitingState() throws Exception {
        mvc.perform(put("/api/demo/session").header("X-Demo-Session", session).with(as(AHMAD, "STUDENT")))
                .andExpect(status().isCreated());
        // The second student persona, Team Beta: only the proposal and progress report are out.
        mvc.perform(get("/api/me/team").header("X-Demo-Session", session).with(as(AHMAD, "STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.team.name").value("Beta"))
                .andExpect(jsonPath("$.finalGradeVisible").value(false))
                .andExpect(jsonPath("$.timeline[0].released").value(true))
                .andExpect(jsonPath("$.timeline[1].released").value(true))
                .andExpect(jsonPath("$.timeline[3].released").value(false));
        mvc.perform(get("/api/me/team/results/FINAL_REPORT")
                        .header("X-Demo-Session", session)
                        .with(as(AHMAD, "STUDENT")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("results-not-released"));
    }

    @Test
    void rolesAreEnforcedAndCopiesAreIsolated() throws Exception {
        mvc.perform(put("/api/demo/session").header("X-Demo-Session", session).with(as(RANIA, "ADMIN")))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/overview").header("X-Demo-Session", session).with(as(NOUR, "STUDENT")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/overview").header("X-Demo-Session", session).with(as(JANE, "REVIEWER")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/overview").header("X-Demo-Session", session)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/overview")
                        .header("X-Demo-Session", UUID.randomUUID())
                        .with(as(RANIA, "ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teams.length()").value(0));
        mvc.perform(get("/api/demo/session")
                        .header("X-Demo-Session", UUID.randomUUID())
                        .with(as(RANIA, "ADMIN")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("demo-session-not-found"));

        // Review M16: a team id from one copy is not found from another copy.
        String overview = mvc.perform(
                        get("/api/overview").header("X-Demo-Session", session).with(as(RANIA, "ADMIN")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String alpha = JsonPath.<List<String>>read(overview, "$.teams[?(@.team.name == 'Alpha')].team.id")
                .getFirst();
        mvc.perform(get("/api/teams/" + alpha)
                        .header("X-Demo-Session", UUID.randomUUID())
                        .with(as(RANIA, "ADMIN")))
                .andExpect(status().isNotFound());

        // Review M6: a reset hands out a new copy id; the old one is gone (the gateway then answers 404 for it).
        String reset = mvc.perform(post("/api/demo/session/reset")
                        .header("X-Demo-Session", session)
                        .with(as(RANIA, "ADMIN")))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID fresh = UUID.fromString(JsonPath.read(reset, "$.id"));
        assertThat(fresh).isNotEqualTo(session);
        mvc.perform(get("/internal/demo-sessions/" + session).with(as(RANIA, "ADMIN")))
                .andExpect(status().isNotFound());
        mvc.perform(get("/internal/demo-sessions/" + fresh).with(as(RANIA, "ADMIN")))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/overview").header("X-Demo-Session", fresh).with(as(RANIA, "ADMIN")))
                .andExpect(jsonPath("$.teams[?(@.team.name == 'Alpha')]").isNotEmpty());
    }

    /** Plan 3.5: reviewers more than 15 points apart are flagged; the grade stays the plain average. */
    private static void assertInsights(String overview) {
        assertThat(JsonPath.<List<Boolean>>read(overview, "$.teams[?(@.team.name == 'Beta')].cells[0].disagreement"))
                .containsExactly(true);
        assertThat(JsonPath.<List<Boolean>>read(overview, "$.teams[?(@.team.name == 'Alpha')].cells[0].disagreement"))
                .containsExactly(false);
        assertThat(JsonPath.<Double>read(overview, "$.insights.disagreementThreshold"))
                .isEqualTo(15.0);
        assertThat(JsonPath.<List<String>>read(overview, "$.insights.disagreements[*].team.name"))
                .contains("Beta", "Delta");
        assertThat(JsonPath.<List<Integer>>read(overview, "$.insights.finalGradeDistribution[*].count"))
                .hasSize(10);
        assertThat(JsonPath.<Integer>read(overview, "$.insights.finalGradeDistribution[8].count"))
                .isEqualTo(1); // Alpha's 88.0 sits in [80, 90)
    }

    /** Review H1: once a grade is final, its reviewer set and its grades can't change any more. */
    private void finalGradesCantChange(UUID team) throws Exception {
        mvc.perform(put("/api/teams/" + team + "/reviewers")
                        .header("X-Demo-Session", session)
                        .with(as(RANIA, "ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"assignments": [{"reviewerId": "%s", "role": "ADVISOR"},
                                                 {"reviewerId": "%s", "role": "JURY"},
                                                 {"reviewerId": "%s", "role": "JURY"}]}""".formatted(EMILY, JANE, KARIM)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("assessment-final"));
        handIn(team, "FINAL_REPORT", JANE, 10.0, UUID.randomUUID())
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("assessment-final"));
        // Review H1: a grade can't be handed in under someone else's name.
        mvc.perform(post("/internal/grades")
                        .header("X-Demo-Session", session)
                        .with(as(JOHN, "REVIEWER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"evaluationId": "%s", "teamId": "%s", "assessment": "FINAL_REPORT", "reviewerId": "%s",
                                 "score": 10, "submittedAt": "2026-06-10T08:00:00Z", "submittedLate": false}""".formatted(UUID.randomUUID(), team, JANE)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("not-your-grade"));
    }
}
