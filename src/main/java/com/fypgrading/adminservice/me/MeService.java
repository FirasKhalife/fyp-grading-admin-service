package com.fypgrading.adminservice.me;

import com.fypgrading.adminservice.clients.EvaluationClient;
import com.fypgrading.adminservice.common.security.CurrentUser;
import com.fypgrading.adminservice.common.security.Roles;
import com.fypgrading.adminservice.common.web.ApiException;
import com.fypgrading.adminservice.domain.Assessment;
import com.fypgrading.adminservice.domain.ReviewerRepository;
import com.fypgrading.adminservice.domain.Student;
import com.fypgrading.adminservice.domain.StudentRepository;
import com.fypgrading.adminservice.domain.TeamAssessment;
import com.fypgrading.adminservice.domain.TeamAssessmentRepository;
import com.fypgrading.adminservice.grading.GradeMath;
import com.fypgrading.adminservice.views.Views.Me;
import com.fypgrading.adminservice.views.Views.PersonView;
import com.fypgrading.adminservice.views.Views.ReleasedCriterion;
import com.fypgrading.adminservice.views.Views.ReleasedLevel;
import com.fypgrading.adminservice.views.Views.ReleasedResults;
import com.fypgrading.adminservice.views.Views.ReleasedReviewer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Who is signed in, and the student's released results. */
@Service
public class MeService {

    private static final Map<String, String> PERSONA_BY_USERNAME = Map.of(
            "rania.haddad", "coordinator",
            "jane.smith", "jury",
            "karim.nassar", "advisor",
            "nour.khoury", "student",
            "ahmad.fares", "student-waiting");

    private final ReviewerRepository reviewers;
    private final StudentRepository students;
    private final TeamAssessmentRepository teamAssessments;
    private final EvaluationClient evaluations;

    public MeService(
            ReviewerRepository reviewers,
            StudentRepository students,
            TeamAssessmentRepository teamAssessments,
            EvaluationClient evaluations) {
        this.reviewers = reviewers;
        this.students = students;
        this.teamAssessments = teamAssessments;
        this.evaluations = evaluations;
    }

    public Me me() {
        CurrentUser user = CurrentUser.require();
        List<String> roles = new ArrayList<>();
        for (String role : List.of(Roles.ADMIN, Roles.REVIEWER, Roles.STUDENT)) {
            if (user.hasRole(role)) {
                roles.add(role);
            }
        }
        String first =
                Optional.ofNullable(user.jwt().getClaimAsString("given_name")).orElse("");
        String last =
                Optional.ofNullable(user.jwt().getClaimAsString("family_name")).orElse("");
        String email = user.jwt().getClaimAsString("email");
        var reviewer = reviewers.findById(user.id());
        if (reviewer.isPresent()) {
            first = reviewer.get().getFirstName();
            last = reviewer.get().getLastName();
            email = reviewer.get().getEmail();
        }
        Optional<Student> student = students.findByUserId(user.id());
        if (student.isPresent()) {
            first = student.get().getFirstName();
            last = student.get().getLastName();
            email = student.get().getEmail();
        }
        PersonView p = PersonView.of(user.id(), first, last, email);
        return new Me(
                user.id(),
                first,
                last,
                p.displayName(),
                p.initials(),
                email,
                roles,
                PERSONA_BY_USERNAME.get(user.jwt().getClaimAsString("preferred_username")),
                student.map(Student::getTeamId).orElse(null));
    }

    /** Levels and comments per reviewer, once the coordinator released this assessment. */
    public ReleasedResults releasedResults(Assessment assessment) {
        Student me =
                students.findByUserId(CurrentUser.require().id()).orElseThrow(() -> ApiException.notFound("Your team"));
        TeamAssessment ta = teamAssessments
                .findByTeamIdAndAssessment(me.getTeamId(), assessment)
                .filter(TeamAssessment::isReleased)
                .orElseThrow(() -> new ApiException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "results-not-released",
                        "Results will appear here once the coordinator releases them."));
        List<EvaluationClient.Submitted> submitted = evaluations.submitted(me.getTeamId(), assessment);
        List<ReleasedReviewer> reviewerViews = submitted.stream()
                .map(e -> new ReleasedReviewer(
                        PersonView.of(
                                e.reviewer().id(),
                                e.reviewer().firstName(),
                                e.reviewer().lastName(),
                                null),
                        e.score(),
                        e.overallComment()))
                .toList();
        Map<UUID, ReleasedCriterionBuilder> byCriterion = new LinkedHashMap<>();
        submitted.stream()
                .findFirst()
                .ifPresent(first -> first.criteria().stream()
                        .sorted(Comparator.comparingInt(EvaluationClient.Criterion::position))
                        .forEach(c -> byCriterion.put(c.criterionId(), new ReleasedCriterionBuilder(c))));
        for (EvaluationClient.Submitted e : submitted) {
            for (EvaluationClient.Criterion c : e.criteria()) {
                ReleasedCriterionBuilder b =
                        byCriterion.computeIfAbsent(c.criterionId(), id -> new ReleasedCriterionBuilder(c));
                // Never null: submit refuses while a level is missing, each evaluation keeps the rubric it was
                // started with (a later rubric edit can't add an unscored criterion to it), and the demo seed is
                // checked for the same in evaluation-service (SeedInvariantTest). Fail loudly rather than show 0.
                int level = Objects.requireNonNull(c.level(), "submitted evaluation without a level");
                b.levels.add(new ReleasedLevel(e.reviewer().id(), level, c.comment()));
            }
        }
        return new ReleasedResults(
                assessment,
                assessment.displayName(),
                assessment.weight(),
                GradeMath.display(ta.getGrade()),
                ta.getReleasedAt(),
                reviewerViews,
                byCriterion.values().stream()
                        .map(ReleasedCriterionBuilder::build)
                        .toList());
    }

    private static final class ReleasedCriterionBuilder {
        private final EvaluationClient.Criterion criterion;
        private final List<ReleasedLevel> levels = new ArrayList<>();

        ReleasedCriterionBuilder(EvaluationClient.Criterion criterion) {
            this.criterion = criterion;
        }

        ReleasedCriterion build() {
            return new ReleasedCriterion(
                    criterion.criterionId(), criterion.name(), criterion.weight(), List.copyOf(levels));
        }
    }
}
