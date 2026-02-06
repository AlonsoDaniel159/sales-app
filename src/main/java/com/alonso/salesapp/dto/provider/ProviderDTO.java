package com.alonso.salesapp.dto.provider;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;

public record ProviderDTO(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        Integer idProvider,
        @NotNull @NotEmpty String name,
        @NotNull @NotEmpty String address,
        Boolean enabled
) {}