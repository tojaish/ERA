package com.interviews.jai.geico.eras.domain;

import jakarta.validation.constraints.NotEmpty;

public record UserDetails(
    @NotEmpty(message="Provider name cannot be empty")
    String name,
    @NotEmpty(message="Latitude cannot be empty")    
    Double glat,
    @NotEmpty(message="Longitude cannot be empty")    
    Double glong
) {}