package jp.bitspace.salon.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStaffRequest {
    private String role;
    private Boolean isActive;

    @JsonProperty("isPractitioner")
    private Boolean isPractitioner;
}
