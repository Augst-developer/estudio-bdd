package com.estudiobdd.dto;

import jakarta.validation.constraints.Size;

public record AceiteRequest(
        @Size(max = 5000, message = "A justificativa deve ter no máximo 5000 caracteres.")
        String justificativa) {
}
