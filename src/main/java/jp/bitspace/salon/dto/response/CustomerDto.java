package jp.bitspace.salon.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 顧客情報DTO（顧客向け認証APIのレスポンス用）.
 */
@Data
@AllArgsConstructor
public class CustomerDto {
    private Long id;
    private Long salonId;

    private String lineUserId;
    private String lineDisplayName;
    private String linePictureUrl;

    private String lastName;
    private String firstName;

    private String email;
    private String phoneNumber;
}
