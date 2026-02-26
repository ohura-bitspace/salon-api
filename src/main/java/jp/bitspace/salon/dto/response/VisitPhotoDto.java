package jp.bitspace.salon.dto.response;

/**
 * 施術写真レスポンスDTO.
 */
public record VisitPhotoDto(
        Long id,
        String imageUrl,
        int displayOrder
) {}
