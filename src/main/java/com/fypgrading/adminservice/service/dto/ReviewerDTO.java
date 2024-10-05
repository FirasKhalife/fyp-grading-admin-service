package com.fypgrading.adminservice.service.dto;

import com.fypgrading.adminservice.entity.Reviewer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewerDTO {

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private Boolean isAdmin;

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Reviewer reviewer))
            return false;
        return Objects.equals(id, reviewer.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
