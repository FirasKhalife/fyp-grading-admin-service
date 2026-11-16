package com.fypgrading.adminservice.teams;

import static org.assertj.core.api.Assertions.assertThat;

import com.fypgrading.adminservice.domain.ReviewerRole;
import com.fypgrading.adminservice.teams.Composition.Assignment;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class CompositionTest {

    static final List<UUID> PEOPLE =
            IntStream.range(0, 7).mapToObj(i -> UUID.randomUUID()).toList();
    static final Set<UUID> KNOWN = Set.copyOf(PEOPLE);

    static List<String> problems(List<Assignment> assignments) {
        return Composition.check(assignments, KNOWN).problems().stream()
                .map(Composition.Problem::code)
                .toList();
    }

    @Test
    void oneAdvisorAndOneJuryIsTheSmallestValidTeam() {
        var result = Composition.check(
                List.of(
                        new Assignment(PEOPLE.get(0), ReviewerRole.ADVISOR),
                        new Assignment(PEOPLE.get(1), ReviewerRole.JURY)),
                KNOWN);
        assertThat(result.valid()).isTrue();
        assertThat(result.juryCount()).isEqualTo(1);
        assertThat(result.advisorCount()).isEqualTo(1);
    }

    @Test
    void needsReviewersAJuryMemberAndAnAdvisor() {
        assertThat(problems(List.of())).containsExactlyInAnyOrder("NO_REVIEWERS", "NO_JURY", "NO_ADVISOR");
        assertThat(problems(List.of(new Assignment(PEOPLE.get(0), ReviewerRole.JURY))))
                .containsExactly("NO_ADVISOR");
        assertThat(problems(List.of(new Assignment(PEOPLE.get(0), ReviewerRole.ADVISOR))))
                .containsExactly("NO_JURY");
    }

    @Test
    void atMostFiveReviewersOneRoleEachAndOnlyKnownPeople() {
        List<Assignment> six = IntStream.range(0, 6)
                .mapToObj(i -> new Assignment(PEOPLE.get(i), i == 0 ? ReviewerRole.ADVISOR : ReviewerRole.JURY))
                .toList();
        assertThat(problems(six)).containsExactly("TOO_MANY_REVIEWERS");
        assertThat(problems(List.of(
                        new Assignment(PEOPLE.get(0), ReviewerRole.ADVISOR),
                        new Assignment(PEOPLE.get(0), ReviewerRole.JURY))))
                .containsExactly("DUPLICATE_REVIEWER");
        assertThat(problems(List.of(
                        new Assignment(PEOPLE.get(0), ReviewerRole.ADVISOR),
                        new Assignment(UUID.randomUUID(), ReviewerRole.JURY))))
                .containsExactly("UNKNOWN_REVIEWER");
    }
}
