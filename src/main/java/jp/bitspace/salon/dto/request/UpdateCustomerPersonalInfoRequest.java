package jp.bitspace.salon.dto.request;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;

/**
 * 顧客個人情報更新リクエスト.
 */
public record UpdateCustomerPersonalInfoRequest(
        String lastName,

        String firstName,

        String lastNameKana,

        String firstNameKana,

        String phoneNumber,

        @Email
        String email,

        LocalDate birthday
) {}
