package com.example.projectmanagement.dto.testing;
    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.NotNull;

//    import static org.springframework.data.jpa.domain.AbstractAuditable_.createdBy;

public record TestStoryCreateRequest(
        @NotNull Long projectId,
        Long linkedStoryId,          // can be null
        @NotBlank String name,
        String description
) {}
