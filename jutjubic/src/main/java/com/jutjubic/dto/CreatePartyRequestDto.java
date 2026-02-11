package com.jutjubic.dto;

import jakarta.validation.constraints.NotBlank;

public class CreatePartyRequestDto {
    @NotBlank
    public String name;

    public String description;
}
