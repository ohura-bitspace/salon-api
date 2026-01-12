package jp.bitspace.salon.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse {
    private Long id;
    private Long salonId;
    private String salonName;
    private Long userId;
    private String email;
    private String userName;
    private String role;
    private Boolean isActive;

    @JsonProperty("isPractitioner")
    private Boolean isPractitioner;
}
