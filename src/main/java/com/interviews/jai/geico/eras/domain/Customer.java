package com.interviews.jai.geico.eras.domain;

import jakarta.validation.constraints.NotNull;

public record Customer(
    @NotNull(message = "Customer Name cannot be null")
    String name, 
    @NotNull(message = "Customer Location cannot be null")
    Geolocation location) {}
