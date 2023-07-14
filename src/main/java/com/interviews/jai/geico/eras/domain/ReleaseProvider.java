package com.interviews.jai.geico.eras.domain;

import jakarta.validation.constraints.NotEmpty;

public record ReleaseProvider(
    @NotEmpty(message="Assistant/Provider Name cannot be empty")
    String assistantName,
    @NotEmpty(message="Customer Name cannot be empty")
    String customerName
) {}