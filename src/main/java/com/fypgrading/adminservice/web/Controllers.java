package com.fypgrading.adminservice.web;

import com.fypgrading.adminservice.common.demo.DemoSession;
import com.fypgrading.adminservice.common.security.CurrentUser;
import com.fypgrading.adminservice.common.security.Roles;
import com.fypgrading.adminservice.common.web.ApiException;
import com.fypgrading.adminservice.demo.DemoService;
import com.fypgrading.adminservice.domain.Assessment;
import com.fypgrading.adminservice.domain.ReviewerRepository;
import com.fypgrading.adminservice.grading.GradeIntake;
import com.fypgrading.adminservice.me.MeService;
import com.fypgrading.adminservice.schedule.AssessmentCommands;
import com.fypgrading.adminservice.teams.TeamCommands;
import com.fypgrading.adminservice.views.ReadModel;
import com.fypgrading.adminservice.views.Views;
import com.fypgrading.adminservice.views.Views.AssessmentView;
import com.fypgrading.adminservice.views.Views.AssignmentCheck;
import com.fypgrading.adminservice.views.Views.DemoSessionView;
import com.fypgrading.adminservice.views.Views.PageView;
import com.fypgrading.adminservice.views.Views.ReviewerView;
import com.fypgrading.adminservice.views.Views.StudentView;
import com.fypgrading.adminservice.views.Views.TeamAssessmentState;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** HTTP entry points of admin-service, grouped by screen. Kept thin: rules live in the services. */
public final class Controllers {

    private Controllers() {}

    /** P1/P2: personas and the visitor's private copy. */
    @RestController
    @RequestMapping("/api/demo")
    public static class Demo {

        private final DemoService demo;

        public Demo(DemoService demo) {
            this.demo = demo;
        }

        @GetMapping("/personas")
        public List<Views.Persona> personas() {
            return demo.personas();
        }

        @GetMapping("/session")
        public DemoSessionView get() {
            return demo.find(DemoSession.require())
                    .orElseThrow(() -> new ApiException(
                            HttpStatus.NOT_FOUND,
                            "demo-session-not-found",
                            "Your demo copy was reset. Start a fresh one."));
        }

        @PutMapping("/session")
        public ResponseEntity<DemoSessionView> ensure() {
            DemoService.Ensured ensured = demo.ensure(DemoSession.require());
            return ensured.created()
                    ? ResponseEntity.created(URI.create("/api/demo/session")).body(ensured.session())
                    : ResponseEntity.ok(ensured.session());
        }

        @PostMapping("/session/reset")
        public DemoSessionView reset() {
            return demo.reset(DemoSession.require());
        }
    }

    /** Who is signed in; P3 for reviewers; P11 for students. */
    @RestController
    @RequestMapping("/api/me")
    public static class Me {

        private final MeService me;
        private final ReadModel read;

        public Me(MeService me, ReadModel read) {
            this.me = me;
            this.read = read;
        }

        @GetMapping
        public Views.Me get() {
            return me.me();
        }

        @GetMapping("/assignments")
        @PreAuthorize("hasRole('REVIEWER')")
        public Views.MyAssignments assignments() {
            return read.myAssignments(CurrentUser.require().id());
        }

        @GetMapping("/team")
        @PreAuthorize("hasRole('STUDENT')")
        public Views.StudentHome team() {
            return read.studentHome(CurrentUser.require().id());
        }

        @GetMapping("/team/results/{assessment}")
        @PreAuthorize("hasRole('STUDENT')")
        public Views.ReleasedResults results(@PathVariable Assessment assessment) {
            return me.releasedResults(assessment);
        }
    }

    /** The five assessments, due dates (P9) and release (P5). */
    @RestController
    @RequestMapping("/api/assessments")
    public static class Assessments {

        private final ReadModel read;
        private final AssessmentCommands commands;

        public Assessments(ReadModel read, AssessmentCommands commands) {
            this.read = read;
            this.commands = commands;
        }

        @GetMapping
        public List<AssessmentView> list() {
            return read.assessments(read.snapshot(false));
        }

        @PatchMapping("/{assessment}")
        @PreAuthorize("hasRole('ADMIN')")
        public AssessmentView update(
                @PathVariable Assessment assessment, @Valid @RequestBody AssessmentCommands.AssessmentPatch body) {
            commands.setDueDate(assessment, body);
            return read.assessments(read.snapshot(false)).get(assessment.ordinal());
        }

        @PostMapping("/{assessment}/release")
        @PreAuthorize("hasRole('ADMIN')")
        public Views.ReleaseResult release(@PathVariable Assessment assessment) {
            return commands.release(assessment);
        }
    }

    /** P5: the coordinator's overview, and its CSV export (3.6). */
    @RestController
    @RequestMapping("/api/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public static class Overview {

        private final ReadModel read;

        public Overview(ReadModel read) {
            this.read = read;
        }

        @GetMapping
        public Views.Overview get() {
            return read.overview();
        }

        @GetMapping(value = "/export", produces = "text/csv")
        public ResponseEntity<String> export() {
            Views.Overview overview = read.overview();
            StringBuilder csv = new StringBuilder("team,project_title");
            overview.assessments()
                    .forEach(a -> csv.append(',').append(a.name().toLowerCase().replace(' ', '_')));
            csv.append(",final_grade,final_state\n");
            for (Views.OverviewRow row : overview.teams()) {
                csv.append(Csv.cell(row.team().name()))
                        .append(',')
                        .append(Csv.cell(row.team().projectTitle()));
                row.cells()
                        .forEach(c -> csv.append(',')
                                .append(
                                        c.state() == TeamAssessmentState.FINAL
                                                        || c.state() == TeamAssessmentState.RELEASED
                                                ? String.format(java.util.Locale.ROOT, "%.1f", c.grade())
                                                : ""));
                csv.append(',')
                        .append(
                                row.finalGrade() == null
                                        ? ""
                                        : String.format(java.util.Locale.ROOT, "%.1f", row.finalGrade()))
                        .append(',')
                        .append(row.finalState())
                        .append('\n');
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"assay-results.csv\"")
                    .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                    .body(csv.toString());
        }
    }

    /** P6 and P8: teams, students, reviewer assignment (3.1) and defense slots (P9). */
    @RestController
    @RequestMapping("/api/teams")
    @Validated
    public static class Teams {

        private final ReadModel read;
        private final TeamCommands commands;

        public Teams(ReadModel read, TeamCommands commands) {
            this.read = read;
            this.commands = commands;
        }

        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        public PageView<Views.TeamSummary> list(
                @RequestParam(defaultValue = "0") @Min(0) int page,
                @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
            return PageView.of(read.teamSummaries(), page, size);
        }

        @PostMapping
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Views.TeamDetail> create(@Valid @RequestBody TeamCommands.TeamCreate body) {
            UUID id = commands.create(body);
            return ResponseEntity.created(URI.create("/api/teams/" + id)).body(detail(id));
        }

        @GetMapping("/{teamId}")
        @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
        public Views.TeamDetail get(@PathVariable UUID teamId) {
            return detail(teamId);
        }

        @PatchMapping("/{teamId}")
        @PreAuthorize("hasRole('ADMIN')")
        public Views.TeamDetail update(@PathVariable UUID teamId, @Valid @RequestBody TeamCommands.TeamPatch body) {
            commands.update(teamId, body);
            return detail(teamId);
        }

        @DeleteMapping("/{teamId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Void> delete(@PathVariable UUID teamId) {
            commands.delete(teamId);
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/{teamId}/students")
        @PreAuthorize("hasRole('ADMIN')")
        public List<StudentView> students(@PathVariable UUID teamId) {
            return detail(teamId).students();
        }

        @PostMapping("/{teamId}/students")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<StudentView> addStudent(
                @PathVariable UUID teamId, @Valid @RequestBody TeamCommands.StudentCreate body) {
            StudentView student = StudentView.of(commands.addStudent(teamId, body));
            return ResponseEntity.created(URI.create("/api/teams/" + teamId + "/students/" + student.id()))
                    .body(student);
        }

        @PatchMapping("/{teamId}/students/{studentId}")
        @PreAuthorize("hasRole('ADMIN')")
        public StudentView updateStudent(
                @PathVariable UUID teamId,
                @PathVariable UUID studentId,
                @Valid @RequestBody TeamCommands.StudentPatch body) {
            return StudentView.of(commands.updateStudent(teamId, studentId, body));
        }

        @DeleteMapping("/{teamId}/students/{studentId}")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Void> removeStudent(@PathVariable UUID teamId, @PathVariable UUID studentId) {
            commands.removeStudent(teamId, studentId);
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/{teamId}/reviewers")
        @PreAuthorize("hasAnyRole('ADMIN', 'REVIEWER')")
        public Views.TeamReviewersView reviewers(@PathVariable UUID teamId) {
            Views.TeamDetail team = detail(teamId);
            return new Views.TeamReviewersView(team.reviewers(), team.composition());
        }

        @PutMapping("/{teamId}/reviewers")
        @PreAuthorize("hasRole('ADMIN')")
        public Views.TeamReviewersView replaceReviewers(
                @PathVariable UUID teamId, @Valid @RequestBody TeamCommands.ReviewersPut body) {
            commands.replaceReviewers(teamId, body);
            return reviewers(teamId);
        }

        @PutMapping("/{teamId}/defense-slot")
        @PreAuthorize("hasRole('ADMIN')")
        public Views.DefenseSlotView setSlot(@PathVariable UUID teamId, @Valid @RequestBody TeamCommands.SlotPut body) {
            commands.setSlot(teamId, body);
            return read.snapshot(false).slotView(teamId);
        }

        @DeleteMapping("/{teamId}/defense-slot")
        @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Void> clearSlot(@PathVariable UUID teamId) {
            commands.clearSlot(teamId);
            return ResponseEntity.noContent().build();
        }

        private Views.TeamDetail detail(UUID teamId) {
            CurrentUser me = CurrentUser.require();
            return read.team(teamId, me.id(), me.isAdmin());
        }
    }

    /** The faculty who can be assigned (P8), and the schedule (P9). */
    @RestController
    @Validated
    public static class People {

        private final ReviewerRepository reviewers;
        private final ReadModel read;

        public People(ReviewerRepository reviewers, ReadModel read) {
            this.reviewers = reviewers;
            this.read = read;
        }

        @GetMapping("/api/reviewers")
        @PreAuthorize("hasRole('ADMIN')")
        public PageView<ReviewerView> reviewers(
                @RequestParam(defaultValue = "0") @Min(0) int page,
                @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
            var counts = read.snapshot(false).teamCountByReviewer();
            List<ReviewerView> all = reviewers.findAllByOrderByLastNameAscFirstNameAsc().stream()
                    .map(r -> {
                        Views.PersonView p = Views.PersonView.of(r);
                        return new ReviewerView(
                                p.id(),
                                p.firstName(),
                                p.lastName(),
                                p.displayName(),
                                p.initials(),
                                p.email(),
                                r.isCoordinator(),
                                counts.getOrDefault(r.getId(), 0L));
                    })
                    .toList();
            return PageView.of(all, page, size);
        }

        @GetMapping("/api/schedule")
        public Views.Schedule schedule() {
            CurrentUser me = CurrentUser.require();
            return read.schedule(me.id(), me.isAdmin(), me.hasRole(Roles.STUDENT));
        }
    }

    /** Internal, for evaluation-service only. The gateway doesn't route /internal/**. */
    @RestController
    @RequestMapping("/internal")
    public static class Internal {

        private final ReadModel read;
        private final GradeIntake intake;
        private final DemoService demo;

        public Internal(ReadModel read, GradeIntake intake, DemoService demo) {
            this.read = read;
            this.intake = intake;
            this.demo = demo;
        }

        /** For the gateway (review M5): 204 when the copy exists and is complete, 404 otherwise. */
        @GetMapping("/demo-sessions/{session}")
        public ResponseEntity<Void> copyReady(@PathVariable UUID session) {
            return demo.isReady(session)
                    ? ResponseEntity.noContent().build()
                    : ResponseEntity.notFound().build();
        }

        @GetMapping("/assignments/{teamId}/{assessment}")
        public AssignmentCheck assignment(
                @PathVariable UUID teamId, @PathVariable Assessment assessment, @RequestParam UUID reviewerId) {
            // Called on every autosave: a few targeted queries, not the whole copy (review M11).
            return read.assignmentCheck(teamId, assessment, reviewerId);
        }

        @PostMapping("/grades")
        public GradeIntake.Receipt grade(@Valid @RequestBody GradeIntake.GradeSubmission body) {
            return intake.submit(body);
        }
    }

    static final class Csv {
        private Csv() {}

        static String cell(String value) {
            if (value == null) {
                return "";
            }
            String safe =
                    value.startsWith("=") || value.startsWith("+") || value.startsWith("-") || value.startsWith("@")
                            ? "'" + value
                            : value;
            return safe.contains(",") || safe.contains("\"") || safe.contains("\n")
                    ? "\"" + safe.replace("\"", "\"\"") + "\""
                    : safe;
        }
    }
}
