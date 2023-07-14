package com.interviews.jai.geico.eras.domain;

import jakarta.validation.constraints.NotEmpty;

public record Assistant(
    @NotEmpty(message = "Provider name can not be empty")
    String name,
    Geolocation location,
    Double distance
) {}