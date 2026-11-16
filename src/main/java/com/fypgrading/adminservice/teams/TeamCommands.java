package com.fypgrading.adminservice.teams;

import com.fypgrading.adminservice.common.security.CurrentUser;
import com.fypgrading.adminservice.common.web.ApiException;
import com.fypgrading.adminservice.domain.Assessment;
import com.fypgrading.adminservice.domain.DefenseSlot;
import com.fypgrading.adminservice.domain.DefenseSlotRepository;
import com.fypgrading.adminservice.domain.GradeRepository;
import com.fypgrading.adminservice.domain.Reviewer;
import com.fypgrading.adminservice.domain.ReviewerRepository;
import com.fypgrading.adminservice.domain.ReviewerRole;
import com.fypgrading.adminservice.domain.Student;
import com.fypgrading.adminservice.domain.StudentRepository;
import com.fypgrading.adminservice.domain.Team;
import com.fypgrading.adminservice.domain.TeamAssessmentRepository;
import com.fypgrading.adminservice.domain.TeamRepository;
import com.fypgrading.adminservice.domain.TeamReviewer;
import com.fypgrading.adminservice.domain.TeamReviewerRepository;
import com.fypgrading.adminservice.events.Events;
import com.fypgrading.adminservice.grading.GradeAggregator;
import com.fypgrading.adminservice.schedule.Dates;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** P8 and P9 changes made by the coordinator: teams, students, reviewer assignment and defense slots. */
@Service
public class TeamCommands {

    /** The student persona keeps a team, so the student demo always works. */
    static final UUID PERSONA_STUDENT = UUID.fromString("00000000-0000-4000-a000-000000000101");

    private final TeamRepository teams;
    private final StudentRepository students;
    private final TeamReviewerRepository assignments;
    private final GradeRepository grades;
    private final TeamAssessmentRepository teamAssessments;
    private final DefenseSlotRepository slots;
    private final ReviewerRepository reviewers;
    private final GradeAggregator aggregator;
    private final Events events;
    private final Clock clock;

    public TeamCommands(
            TeamRepository teams,
            StudentRepository students,
            TeamReviewerRepository assignments,
            GradeRepository grades,
            TeamAssessmentRepository teamAssessments,
            DefenseSlotRepository slots,
            ReviewerRepository reviewers,
            GradeAggregator aggregator,
            Events events,
            Clock clock) {
        this.teams = teams;
        this.students = students;
        this.assignments = assignments;
        this.grades = grades;
        this.teamAssessments = teamAssessments;
        this.slots = slots;
        this.reviewers = reviewers;
        this.aggregator = aggregator;
        this.events = events;
        this.clock = clock;
    }

    public record TeamCreate(
            @NotBlank @Size(max = 60) String name,
            @NotBlank @Size(max = 200) String projectTitle,
            @Size(max = 2000) String description) {}

    public record TeamPatch(
            @Size(min = 1, max = 60) String name,
            @Size(min = 1, max = 200) String projectTitle,
            @Size(max = 2000) String description) {}

    public record StudentCreate(
            @NotBlank @Size(max = 80) String firstName,
            @NotBlank @Size(max = 80) String lastName,
            @NotBlank @Email @Size(max = 200) String email) {}

    public record StudentPatch(
            @Size(min = 1, max = 80) String firstName,
            @Size(min = 1, max = 80) String lastName,
            @Email @Size(max = 200) String email,
            UUID teamId) {}

    public record AssignmentInput(
            @NotNull UUID reviewerId, @NotNull ReviewerRole role) {}

    public record ReviewersPut(@NotNull @Size(max = 10) List<@Valid @NotNull AssignmentInput> assignments) {}

    public record SlotPut(
            @NotNull Instant startsAt,
            @Min(10) @Max(240) Integer durationMinutes,
            @NotBlank @Size(max = 60) String room) {}

    // ------------------------------------------------------------------ teams

    @Transactional
    public UUID create(TeamCreate body) {
        String name = body.name().strip();
        if (teams.existsByNameIgnoreCase(name)) {
            throw ApiException.conflict("team-name-taken", "A team called " + name + " already exists.");
        }
        Team team =
                teams.save(new Team(UUID.randomUUID(), name, body.projectTitle().strip(), body.description()));
        return team.getId();
    }

    @Transactional
    public void update(UUID teamId, TeamPatch body) {
        Team team = team(teamId);
        if (body.name() != null && !body.name().strip().equalsIgnoreCase(team.getName())) {
            if (teams.existsByNameIgnoreCase(body.name().strip())) {
                throw ApiException.conflict(
                        "team-name-taken", "A team called " + body.name().strip() + " already exists.");
            }
            team.rename(body.name().strip());
        }
        if (body.projectTitle() != null) {
            team.setProjectTitle(body.projectTitle().strip());
        }
        if (body.description() != null) {
            team.setDescription(body.description());
        }
    }

    @Transactional
    public void delete(UUID teamId) {
        Team team = team(teamId);
        if (grades.existsByTeamId(teamId)) {
            throw ApiException.conflict(
                    "team-has-submissions",
                    "Team " + team.getName() + " has submitted evaluations, which are permanent.");
        }
        if (students.findByTeamIdOrderByLastNameAscFirstNameAsc(teamId).stream()
                .anyMatch(s -> PERSONA_STUDENT.equals(s.getUserId()))) {
            throw ApiException.conflict("student-is-demo-persona", "This team holds the student demo persona.");
        }
        students.deleteByTeamId(teamId);
        assignments.deleteByTeamId(teamId);
        slots.deleteByTeamId(teamId);
        teamAssessments.deleteByTeamId(teamId);
        teams.delete(team);
    }

    // ------------------------------------------------------------------ students

    @Transactional
    public Student addStudent(UUID teamId, StudentCreate body) {
        team(teamId);
        String email = body.email().strip().toLowerCase();
        if (students.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("student-email-taken", email + " is already on a team.");
        }
        return students.save(new Student(
                UUID.randomUUID(),
                teamId,
                body.firstName().strip(),
                body.lastName().strip(),
                email,
                null));
    }

    @Transactional
    public Student updateStudent(UUID teamId, UUID studentId, StudentPatch body) {
        Student student =
                students.findByIdAndTeamId(studentId, teamId).orElseThrow(() -> ApiException.notFound("The student"));
        String email = body.email() == null ? null : body.email().strip().toLowerCase();
        if (email != null && !email.equalsIgnoreCase(student.getEmail()) && students.existsByEmailIgnoreCase(email)) {
            throw ApiException.conflict("student-email-taken", email + " is already on a team.");
        }
        if (body.teamId() != null) {
            team(body.teamId());
        }
        student.update(body.firstName(), body.lastName(), email, body.teamId());
        return student;
    }

    @Transactional
    public void removeStudent(UUID teamId, UUID studentId) {
        Student student =
                students.findByIdAndTeamId(studentId, teamId).orElseThrow(() -> ApiException.notFound("The student"));
        if (PERSONA_STUDENT.equals(student.getUserId())) {
            throw ApiException.conflict(
                    "student-is-demo-persona",
                    student.getFirstName() + " " + student.getLastName()
                            + " is the student demo persona and stays on a team.");
        }
        students.delete(student);
    }

    // ------------------------------------------------------------------ 3.1 reviewer assignment

    /**
     * Replaces the team's reviewers. The new set must satisfy the composition rules, and nobody who already
     * submitted for this team can be removed or change role (submissions are permanent).
     */
    @Transactional
    public void replaceReviewers(UUID teamId, ReviewersPut body) {
        Team team = team(teamId);
        Map<UUID, Reviewer> known =
                reviewers.findAll().stream().collect(Collectors.toMap(Reviewer::getId, Function.identity()));
        List<Composition.Assignment> wanted = body.assignments().stream()
                .map(a -> new Composition.Assignment(a.reviewerId(), a.role()))
                .toList();
        Composition.Result result = Composition.check(wanted, known.keySet());
        if (!result.valid()) {
            throw ApiException.unprocessable(
                            "composition-invalid",
                            result.problems().stream()
                                    .map(Composition.Problem::message)
                                    .collect(Collectors.joining(" ")))
                    .with("problems", result.problems());
        }
        List<TeamReviewer> current = assignments.findByTeamId(teamId);
        Map<UUID, ReviewerRole> next = wanted.stream()
                .collect(Collectors.toMap(Composition.Assignment::reviewerId, Composition.Assignment::role));
        for (TeamReviewer existing : current) {
            ReviewerRole role = next.get(existing.getReviewerId());
            if ((role == null || role != existing.getRole())
                    && grades.existsByTeamIdAndReviewerId(teamId, existing.getReviewerId())) {
                throw ApiException.conflict(
                        "reviewer-has-submissions",
                        known.get(existing.getReviewerId()).displayName() + " already submitted for Team "
                                + team.getName() + ", so they stay on it with the same role.");
            }
        }
        Instant now = clock.instant();
        Set<UUID> affectedRoles = new LinkedHashSet<>();
        for (TeamReviewer existing : current) {
            ReviewerRole role = next.remove(existing.getReviewerId());
            if (role == null) {
                assignments.delete(existing);
                affectedRoles.add(existing.getReviewerId());
            } else if (role != existing.getRole()) {
                existing.changeRole(role);
                events.notify(
                        "role:" + teamId + ":" + existing.getReviewerId() + ":" + role + ":" + now,
                        "ROLE_CHANGED",
                        "Your role on Team " + team.getName() + " is now " + label(role) + ".",
                        now,
                        Events.Link.of("MY_TEAMS", teamId, null),
                        List.of(existing.getReviewerId()));
                affectedRoles.add(existing.getReviewerId());
            }
        }
        next.forEach((reviewerId, role) -> {
            assignments.save(new TeamReviewer(UUID.randomUUID(), teamId, reviewerId, role));
            events.notify(
                    "assigned:" + teamId + ":" + reviewerId + ":" + now,
                    "ASSIGNED",
                    "You were assigned to Team " + team.getName() + " as " + article(role) + ".",
                    now,
                    Events.Link.of("MY_TEAMS", teamId, null),
                    List.of(reviewerId));
        });
        CurrentUser me = CurrentUser.require();
        events.activity(
                "ASSIGNMENT",
                me.id(),
                me.displayName(),
                "updated the reviewers of Team " + team.getName() + ": "
                        + result.reviewerCount() + " reviewers, " + result.juryCount() + " jury, "
                        + result.advisorCount()
                        + " advisor" + (result.advisorCount() == 1 ? "" : "s") + ".",
                now,
                Events.Link.team(teamId, null));
        // A changed set can complete an assessment (e.g. a reviewer who never graded was removed).
        assignments.flush();
        for (Assessment a : Assessment.values()) {
            aggregator.aggregate(teamId, a, true);
        }
    }

    // ------------------------------------------------------------------ P9 defense slot

    @Transactional
    public void setSlot(UUID teamId, SlotPut body) {
        Team team = team(teamId);
        int minutes = body.durationMinutes() == null ? 40 : body.durationMinutes();
        Instant ends = body.startsAt().plus(Duration.ofMinutes(minutes));
        Optional<DefenseSlot> existing = slots.findByTeamId(teamId);
        boolean moved = existing.isPresent();
        existing.ifPresentOrElse(
                s -> s.move(body.startsAt(), ends, body.room().strip()),
                () -> slots.save(new DefenseSlot(
                        UUID.randomUUID(),
                        teamId,
                        body.startsAt(),
                        ends,
                        body.room().strip())));
        slots.flush();

        Instant now = clock.instant();
        String when = Dates.dayAndTime(body.startsAt()) + ", " + body.room().strip();
        List<UUID> recipients = new ArrayList<>();
        reviewers.findByCoordinatorTrue().forEach(r -> recipients.add(r.getId()));
        assignments.findByTeamId(teamId).stream()
                .filter(a -> a.getRole() == ReviewerRole.JURY)
                .forEach(a -> recipients.add(a.getReviewerId()));
        students.findByTeamIdOrderByLastNameAscFirstNameAsc(teamId).stream()
                .map(Student::getUserId)
                .filter(java.util.Objects::nonNull)
                .forEach(recipients::add);
        events.notify(
                "defense:" + teamId + ":" + body.startsAt() + ":" + now,
                "DEFENSE_SCHEDULED",
                (moved ? "Defense moved: Team " : "Defense scheduled: Team ") + team.getName() + ", " + when + ".",
                now,
                Events.Link.of("SCHEDULE", teamId, null),
                recipients);
        events.activity(
                "SCHEDULE",
                null,
                "Schedule",
                "Team " + team.getName() + (moved ? " moved to " : " scheduled for ") + when + "."
                        + conflictNote(teamId),
                now,
                Events.Link.of("SCHEDULE", teamId, null));
    }

    @Transactional
    public void clearSlot(UUID teamId) {
        team(teamId);
        slots.deleteByTeamId(teamId);
    }

    private String conflictNote(UUID teamId) {
        DefenseSlot mine = slots.findByTeamId(teamId).orElseThrow();
        Set<UUID> jury = assignments.findByTeamId(teamId).stream()
                .filter(a -> a.getRole() == ReviewerRole.JURY)
                .map(TeamReviewer::getReviewerId)
                .collect(Collectors.toSet());
        Set<String> clashes = new LinkedHashSet<>();
        for (DefenseSlot other : slots.findAll()) {
            if (other.getTeamId().equals(teamId) || !other.overlaps(mine)) {
                continue;
            }
            assignments.findByTeamId(other.getTeamId()).stream()
                    .filter(a -> a.getRole() == ReviewerRole.JURY && jury.contains(a.getReviewerId()))
                    .forEach(a -> reviewers.findById(a.getReviewerId()).ifPresent(r -> clashes.add(r.displayName())));
        }
        if (clashes.isEmpty()) {
            return "";
        }
        return " " + String.join(" and ", clashes) + (clashes.size() == 1 ? " is" : " are") + " now in two defenses at "
                + Dates.time(mine.getStartsAt()) + ".";
    }

    private Team team(UUID id) {
        return teams.findById(id).orElseThrow(() -> ApiException.notFound("The team"));
    }

    private static String label(ReviewerRole role) {
        return role == ReviewerRole.JURY ? "jury member" : "advisor";
    }

    private static String article(ReviewerRole role) {
        return role == ReviewerRole.JURY ? "a jury member" : "an advisor";
    }
}
