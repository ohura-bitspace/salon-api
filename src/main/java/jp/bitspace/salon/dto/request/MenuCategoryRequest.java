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
public class MenuCategoryRequest {
    @NotNull
    private Long salonId;

    @NotBlank
    private String name;

    private Integer displayOrder;
}
