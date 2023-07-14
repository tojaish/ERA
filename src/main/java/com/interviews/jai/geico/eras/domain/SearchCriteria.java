package com.interviews.jai.geico.eras.domain;

import jakarta.validation.constraints.NotEmpty;

public record SearchCriteria(
    @NotEmpty(message="Latitude cannot be empty")    
    Double glat,
    @NotEmpty(message="Longitude cannot be empty")    
    Double glong,
    @NotEmpty(message="Search Radius cannot be empty")    
    int radius
) {}