package jp.bitspace.salon.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/**
 * 顧客作成リクエスト（管理者による手動作成）.
 */
public record CreateCustomerRequest(
        @NotNull
        Long salonId,

        String lastName,

        String firstName,

        String lastNameKana,

        String firstNameKana,
        
        String lineDisplayName,

        String phoneNumber,

        @Email
        String email,

        LocalDate birthday,

        String adminMemo
) {}
