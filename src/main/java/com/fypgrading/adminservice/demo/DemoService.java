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
import com.fypgrading.adminservice.schedule.DueReminders;
import com.fypgrading.adminservice.views.Views.DemoSessionView;
import com.fypgrading.adminservice.views.Views.Persona;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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
                    List.of("Read the jury's comments on your final presentation", "Follow your team's timeline")),
            new Persona(
                    "student-waiting",
                    "ahmad.fares",
                    "Ahmad Fares",
                    "Student",
                    List.of("STUDENT"),
                    List.of("See what a student sees before results are released", "Check tomorrow's defense")));

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
    private final DueReminders reminders;
    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final Clock clock;
    private final DemoSeed seed;
    private final int maxCopies;

    /** A CREATING copy older than this is treated as a failed attempt and rebuilt. */
    static final Duration CREATING_TIMEOUT = Duration.ofSeconds(30);

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
            DueReminders reminders,
            JdbcTemplate jdbc,
            TransactionTemplate tx,
            Clock clock,
            JsonMapper json,
            @Value("${assay.demo.max-copies:2000}") int maxCopies)
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
        this.reminders = reminders;
        this.jdbc = jdbc;
        this.tx = tx;
        this.clock = clock;
        this.maxCopies = maxCopies;
        try (InputStream in = new ClassPathResource("demo/seed.json").getInputStream()) {
            this.seed = json.readValue(in, DemoSeed.class);
        }
    }

    public List<Persona> personas() {
        return PERSONAS;
    }

    /** Only a complete copy counts: one still being created, or never created, is "not found". */
    public Optional<DemoSessionView> find(UUID session) {
        return copies.findById(session).filter(DemoCopy::isReady).map(DemoService::view);
    }

    public boolean isReady(UUID session) {
        return copies.findById(session).map(DemoCopy::isReady).orElse(false);
    }

    public record Ensured(DemoSessionView session, boolean created) {}

    /**
     * Creates the visitor's copy if it doesn't exist yet. Idempotent; switching persona keeps the copy. The
     * {@code demo_copy} row is the lock: CREATING while the four services build their parts, READY after. A second
     * tab waits for the first instead of building a second copy.
     */
    public Ensured ensure(UUID session) {
        Optional<DemoCopy> existing = copies.findById(session);
        if (existing.isPresent() && existing.get().isReady()) {
            return new Ensured(view(existing.get()), false);
        }
        SeedClock at = SeedClock.startingNow(clock);
        if (existing.isPresent()) {
            boolean stale = existing.get().getCreatedAt().isBefore(at.now().minus(CREATING_TIMEOUT));
            if (!stale) {
                return new Ensured(waitUntilReady(session), false);
            }
            tx.executeWithoutResult(s -> copies.findById(session).ifPresent(c -> c.restart(at.now(), at.anchor())));
        } else {
            if (copies.count() >= maxCopies) {
                throw new ApiException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "demo-capacity",
                        "The demo is full right now. Try again after midnight (Paris time).");
            }
            try {
                tx.executeWithoutResult(s -> copies.saveAndFlush(new DemoCopy(session, at.now(), at.anchor())));
            } catch (DataIntegrityViolationException racedWithAnotherTab) {
                return new Ensured(waitUntilReady(session), false);
            }
        }
        build(session, at);
        // The copy's reminder run (due soon, overdue): a copy lives one day, so this is the only one it needs.
        DemoSession.runAs(session, () -> tx.executeWithoutResult(s -> reminders.remindAll()));
        tx.executeWithoutResult(s -> copies.findById(session).ifPresent(c -> c.ready(at.now(), at.anchor())));
        return new Ensured(view(copies.findById(session).orElseThrow()), true);
    }

    /**
     * "Reset my demo": a fresh copy under a <b>new</b> session id, which the browser must store (review M6). Seeded
     * ids derive from the session, so reusing it would let events still in flight from the old copy land in the
     * new one. The old copy is then deleted.
     */
    public DemoSessionView reset(UUID oldSession) {
        UUID fresh = UUID.randomUUID();
        DemoSessionView created = DemoSession.callAs(fresh, () -> ensure(fresh).session());
        DemoSession.runAs(oldSession, () -> {
            remote.deleteEverywhere(oldSession);
            deleteOwnData(oldSession);
            tx.executeWithoutResult(s -> copies.deleteById(oldSession));
        });
        return created;
    }

    /**
     * Midnight purge: one TRUNCATE of every copy table, which is atomic and waits for transactions in flight (review
     * M4). The other services purge their own parts; whatever they keep is replaced when the session's copy is
     * created again.
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Paris")
    public void purgeAll() {
        tx.executeWithoutResult(s -> jdbc.execute("TRUNCATE " + String.join(", ", COPY_TABLES) + ", demo_copy"));
        log.info("Midnight purge: every demo copy deleted");
    }

    /** If the service was down at midnight, the copies from before it are purged on the next start. */
    @EventListener(ApplicationReadyEvent.class)
    public void purgeCopiesFromBeforeLastMidnight() {
        Instant lastMidnight = SeedClock.nextPurge(clock.instant()).minus(Duration.ofDays(1));
        if (copies.existsByCreatedAtBefore(lastMidnight)) {
            purgeAll();
        }
    }

    private DemoSessionView waitUntilReady(UUID session) {
        long deadline = System.nanoTime() + CREATING_TIMEOUT.toNanos();
        while (System.nanoTime() < deadline) {
            Optional<DemoCopy> copy = copies.findById(session);
            if (copy.isPresent() && copy.get().isReady()) {
                return view(copy.get());
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        throw ApiException.conflict(
                "demo-copy-in-progress", "Your demo copy is still being created. Try again in a few seconds.");
    }

    private void build(UUID session, SeedClock at) {
        try {
            remote.replaceEverywhere(session, at.now());
            replaceOwnData(session, at);
        } catch (RuntimeException e) {
            log.warn("Creating demo copy {} failed, rolling back: {}", fingerprint(session), e.getMessage());
            remote.deleteEverywhere(session);
            deleteOwnData(session);
            tx.executeWithoutResult(s -> copies.deleteById(session));
            throw e instanceof ApiException api
                    ? api
                    : ApiException.unavailable("The demo is starting up, try again in a minute.");
        }
    }

    private void deleteOwnData(UUID session) {
        tx.executeWithoutResult(s -> deleteRows(session));
    }

    private void deleteRows(UUID session) {
        COPY_TABLES.forEach(
                table -> jdbc.update("delete from " + table + " where demo_session = ?", session.toString()));
    }

    /** The session id is a bearer-like secret: logs only show a short hash of it. */
    static String fingerprint(UUID session) {
        try {
            byte[] hash = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(session.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash, 0, 4);
        } catch (java.security.NoSuchAlgorithmException e) {
            return "session";
        }
    }

    /**
     * Deletes the session's own rows and writes the seed in one transaction, then runs the real aggregation so the
     * grades match the live maths.
     */
    void replaceOwnData(UUID session, SeedClock at) {
        Map<Assessment, Instant> due = new EnumMap<>(Assessment.class);
        seed.assessments().forEach(a -> due.put(a.code(), at.resolve(a.dueAt())));
        DemoSession.runAs(
                session,
                () -> tx.executeWithoutResult(s -> {
                    deleteRows(session);
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
                            Instant submittedAt = at.resolve(g.submittedAt());
                            grades.save(new Grade(
                                    SeedIds.of(session, g.evaluationKey() + "/grade"),
                                    SeedIds.of(session, g.evaluationKey()),
                                    teamId,
                                    g.assessment(),
                                    g.reviewerId(),
                                    g.score(),
                                    submittedAt,
                                    submittedAt.isAfter(due.get(g.assessment()))));
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
