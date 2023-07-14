package com.interviews.jai.geico.eras.domain;

import jakarta.validation.constraints.NotEmpty;

public record Geolocation (
    @NotEmpty(message="Latitude cannot be empty")
    Double glat,
    @NotEmpty(message="longitude cannot be empty")
    Double glong
){}