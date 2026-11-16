package com.fypgrading.adminservice.demo;

import com.fypgrading.adminservice.clients.DemoCopyClient;
import com.fypgrading.adminservice.common.demo.DemoSession;
import com.fypgrading.adminservice.common.demo.SeedClock;
import com.fypgrading.adminservice.common.demo.SeedIds;
import com.fypgrading.adminservice.common.web.ApiException;
import com.fypgrading.adminservice.domain.Assessment;
import com.fypgrading.adminservice.domain.AssessmentSchedule;
import com.fypgrading.adminservice.domain.AssessmentScheduleRepository;
import com.fypgrading.adminservice.domain.DefenseSlot;
import com.fypgrading.adminservice.domain.DefenseSlotRepository;
import com.fypgrading.adminservice.domain.DemoCopy;
import com.fypgrading.adminservice.domain.DemoCopyRepository;
import com.fypgrading.adminservice.domain.Grade;
import com.fypgrading.adminservice.domain.GradeRepository;
import com.fypgrading.adminservice.domain.ReviewerRole;
import com.fypgrading.adminservice.domain.Student;
import com.fypgrading.adminservice.domain.StudentRepository;
import com.fypgrading.adminservice.domain.Team;
import com.fypgrading.adminservice.domain.TeamAssessmentRepository;
import com.fypgrading.adminservice.domain.TeamRepository;
import com.fypgrading.adminservice.domain.TeamReviewer;
import com.fypgrading.adminservice.domain.TeamReviewerRepository;
import com.fypgrading.adminservice.grading.GradeAggregator;
import com.fypgrading.adminservice.views.Views.DemoSessionView;
import com.fypgrading.adminservice.views.Views.Persona;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

/**
 * Each visitor's private copy of the demo data (brief 4.7). The first persona click creates it in all four
 * services from their seed files; "Reset my demo" replaces it; every copy is deleted at midnight (Europe/Paris).
 * Seed dates are relative: "today" in a copy is the day it was created, which is the middle defense day.
 */
@Service
public class DemoService {

    private static final Logger log = LoggerFactory.getLogger(DemoService.class);

    /** Tables holding demo copies, children first. */
    private static final List<String> COPY_TABLES = List.of(
            "grade", "team_assessment", "defense_slot", "team_reviewer", "student", "team", "assessment_schedule");

    private static final List<Persona> PERSONAS = List.of(
            new Persona(
                    "coordinator",
                    "rania.haddad",
                    "Rania Haddad",
                    "Coordinator",
                    List.of("ADMIN", "REVIEWER"),
                    List.of(
                            "Release the advisor assessment results",
                            "Fix the defense conflict on day 3",
                            "Open Team Beta's reviewer spread")),
            new Persona(
                    "jury",
                    "jane.smith",
                    "Jane Smith",
                    "Jury member",
                    List.of("REVIEWER"),
                    List.of("Finish the Team Beta final report draft", "Grade Team Epsilon's final presentation")),
            new Persona(
                    "advisor",
                    "karim.nassar",
                    "Karim Nassar",
                    "Advisor",
                    List.of("REVIEWER"),
                    List.of(
                            "Submit the overdue advisor assessment for Team Delta",
                            "Check your defenses as a jury member")),
            new Persona(
                    "student",
                    "nour.khoury",
                    "Nour Khoury",
                    "Student",
                    List.of("STUDENT"),
                    List.of("Read the jury's comments on your final presentation", "Follow your team's timeline")));

    private final DemoCopyRepository copies;
    private final AssessmentScheduleRepository schedules;
    private final TeamRepository teams;
    private final StudentRepository students;
    private final TeamReviewerRepository assignments;
    private final DefenseSlotRepository slots;
    private final GradeRepository grades;
    private final TeamAssessmentRepository teamAssessments;
    private final GradeAggregator aggregator;
    private final DemoCopyClient remote;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final Clock clock;
    private final DemoSeed seed;

    public DemoService(
            DemoCopyRepository copies,
            AssessmentScheduleRepository schedules,
            TeamRepository teams,
            StudentRepository students,
            TeamReviewerRepository assignments,
            DefenseSlotRepository slots,
            GradeRepository grades,
            TeamAssessmentRepository teamAssessments,
            GradeAggregator aggregator,
            DemoCopyClient remote,
            JdbcTemplate jdbc,
            TransactionTemplate tx,
            Clock clock,
            JsonMapper json)
            throws IOException {
        this.copies = copies;
        this.schedules = schedules;
        this.teams = teams;
        this.students = students;
        this.assignments = assignments;
        this.slots = slots;
        this.grades = grades;
        this.teamAssessments = teamAssessments;
        this.aggregator = aggregator;
        this.remote = remote;
        this.jdbc = jdbc;
        this.tx = tx;
        this.clock = clock;
        try (InputStream in = new ClassPathResource("demo/seed.json").getInputStream()) {
            this.seed = json.readValue(in, DemoSeed.class);
        }
    }

    public List<Persona> personas() {
        return PERSONAS;
    }

    public Optional<DemoSessionView> find(UUID session) {
        return copies.findById(session).map(DemoService::view);
    }

    public record Ensured(DemoSessionView session, boolean created) {}

    /** Creates the visitor's copy if it doesn't exist yet. Idempotent; switching persona keeps the copy. */
    public Ensured ensure(UUID session) {
        Optional<DemoSessionView> existing = find(session);
        if (existing.isPresent()) {
            return new Ensured(existing.get(), false);
        }
        SeedClock at = SeedClock.startingNow(clock);
        try {
            tx.executeWithoutResult(s -> copies.saveAndFlush(new DemoCopy(session, at.now(), at.anchor())));
        } catch (DataIntegrityViolationException racedWithAnotherTab) {
            return new Ensured(find(session).orElseThrow(), false);
        }
        try {
            remote.replaceEverywhere(session);
            seedOwnData(session, at);
        } catch (RuntimeException e) {
            log.warn("Creating demo copy {} failed, rolling back: {}", session, e.getMessage());
            remote.deleteEverywhere(session);
            deleteOwnData(session);
            tx.executeWithoutResult(s -> copies.deleteById(session));
            throw e instanceof ApiException api
                    ? api
                    : ApiException.unavailable("The demo is starting up, try again in a minute.");
        }
        return new Ensured(view(copies.findById(session).orElseThrow()), true);
    }

    /** "Reset my demo": a fresh copy under the same id. */
    public DemoSessionView reset(UUID session) {
        remote.deleteEverywhere(session);
        deleteOwnData(session);
        tx.executeWithoutResult(s -> copies.deleteById(session));
        return ensure(session).session();
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Paris")
    public void purgeAll() {
        tx.executeWithoutResult(s -> {
            COPY_TABLES.forEach(table -> jdbc.update("delete from " + table));
            jdbc.update("delete from demo_copy");
        });
        log.info("Midnight purge: every demo copy deleted");
    }

    private void deleteOwnData(UUID session) {
        tx.executeWithoutResult(s -> COPY_TABLES.forEach(
                table -> jdbc.update("delete from " + table + " where demo_session = ?", session.toString())));
    }

    /** Writes the seed under the session, then runs the real aggregation so grades match the live maths. */
    void seedOwnData(UUID session, SeedClock at) {
        DemoSession.runAs(
                session,
                () -> tx.executeWithoutResult(s -> {
                    for (DemoSeed.SeedAssessment a : seed.assessments()) {
                        schedules.save(new AssessmentSchedule(
                                SeedIds.of(session, "schedule:" + a.code()), a.code(), at.resolve(a.dueAt())));
                    }
                    for (DemoSeed.SeedTeam t : seed.teams()) {
                        UUID teamId = SeedIds.of(session, t.key());
                        teams.save(new Team(teamId, t.name(), t.projectTitle(), t.description()));
                        for (DemoSeed.SeedStudent st : t.students()) {
                            students.save(new Student(
                                    SeedIds.of(session, st.key()),
                                    teamId,
                                    st.firstName(),
                                    st.lastName(),
                                    st.email(),
                                    st.userId()));
                        }
                        for (DemoSeed.SeedReviewer r : t.reviewers()) {
                            assignments.save(new TeamReviewer(
                                    SeedIds.of(session, t.key() + "/reviewer:" + r.reviewerId()),
                                    teamId,
                                    r.reviewerId(),
                                    r.role()));
                        }
                        if (t.defenseSlot() != null) {
                            Instant starts = at.resolve(t.defenseSlot().startsAt());
                            slots.save(new DefenseSlot(
                                    SeedIds.of(session, t.key() + "/slot"),
                                    teamId,
                                    starts,
                                    starts.plus(
                                            Duration.ofMinutes(t.defenseSlot().durationMinutes())),
                                    t.defenseSlot().room()));
                        }
                        for (DemoSeed.SeedGrade g : t.grades()) {
                            grades.save(new Grade(
                                    SeedIds.of(session, g.evaluationKey() + "/grade"),
                                    SeedIds.of(session, g.evaluationKey()),
                                    teamId,
                                    g.assessment(),
                                    g.reviewerId(),
                                    g.score(),
                                    at.resolve(g.submittedAt())));
                        }
                    }
                    grades.flush();
                    for (DemoSeed.SeedTeam t : seed.teams()) {
                        UUID teamId = SeedIds.of(session, t.key());
                        for (Assessment a : Assessment.values()) {
                            aggregator.aggregate(teamId, a, false);
                        }
                        teamAssessments.flush();
                        for (DemoSeed.SeedRelease r : t.releases()) {
                            teamAssessments
                                    .findByTeamIdAndAssessment(teamId, r.assessment())
                                    .ifPresent(ta -> ta.release(at.resolve(r.releasedAt()), null));
                        }
                    }
                }));
    }

    private static DemoSessionView view(DemoCopy copy) {
        return new DemoSessionView(
                copy.getId(),
                copy.getCreatedAt(),
                SeedClock.nextPurge(copy.getCreatedAt()),
                copy.getAnchorDate(),
                SeedClock.ZONE.getId());
    }

    /** The shape of {@code demo/seed.json} (generated by fyp-grading-docker-compose/seed/generate.py). */
    record DemoSeed(List<SeedAssessment> assessments, List<SeedTeam> teams) {

        record SeedAssessment(Assessment code, String dueAt) {}

        record SeedTeam(
                String key,
                String name,
                String projectTitle,
                String description,
                List<SeedStudent> students,
                List<SeedReviewer> reviewers,
                SeedSlot defenseSlot,
                List<SeedGrade> grades,
                List<SeedRelease> releases) {}

        record SeedStudent(String key, String firstName, String lastName, String email, UUID userId) {}

        record SeedReviewer(UUID reviewerId, ReviewerRole role) {}

        record SeedSlot(String startsAt, int durationMinutes, String room) {}

        record SeedGrade(
                Assessment assessment, UUID reviewerId, String evaluationKey, double score, String submittedAt) {}

        record SeedRelease(Assessment assessment, String releasedAt) {}
    }
}
