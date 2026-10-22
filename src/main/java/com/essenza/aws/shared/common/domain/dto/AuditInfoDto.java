package com.essenza.aws.shared.common.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class AuditInfoDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private boolean deleted;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer usrCrea;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer usrMod;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss",timezone = "UTC")
    private Instant createdAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss",timezone = "UTC")
    private Instant updatedAt;

    // @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd HH:mm:ss",timezone = "UTC")
    // private Instant deletedAt;
}
