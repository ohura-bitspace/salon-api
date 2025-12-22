package jp.bitspace.salon.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuSectionRequest {
    @NotNull
    private Long salonId;

    @NotNull
    private Long categoryId;

    @NotBlank
    private String name;

    private Integer displayOrder;
}
