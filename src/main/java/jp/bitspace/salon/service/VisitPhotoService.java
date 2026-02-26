package jp.bitspace.salon.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jp.bitspace.salon.dto.response.VisitPhotoDto;
import jp.bitspace.salon.model.Reservation;
import jp.bitspace.salon.model.VisitPhoto;
import jp.bitspace.salon.repository.ReservationRepository;
import jp.bitspace.salon.repository.VisitPhotoRepository;

/**
 * 施術写真サービス.
 */
@Service
public class VisitPhotoService {

    private final VisitPhotoRepository visitPhotoRepository;
    private final ReservationRepository reservationRepository;
    private final ImageUploadService imageUploadService;

    public VisitPhotoService(
            VisitPhotoRepository visitPhotoRepository,
            ReservationRepository reservationRepository,
            ImageUploadService imageUploadService) {
        this.visitPhotoRepository = visitPhotoRepository;
        this.reservationRepository = reservationRepository;
        this.imageUploadService = imageUploadService;
    }

    /**
     * 施術写真をアップロードしてDBに登録する.
     */
    public VisitPhotoDto uploadPhoto(Long visitId, Long salonId, MultipartFile file) {
        Reservation reservation = findAndVerify(visitId, salonId);

        String imageUrl = imageUploadService.save(file, salonId);

        VisitPhoto photo = new VisitPhoto();
        photo.setReservationId(reservation.getId());
        photo.setImageUrl(imageUrl);
        // 既存枚数を display_order に使い、末尾に追加する
        photo.setDisplayOrder(visitPhotoRepository.countByReservationId(visitId));

        VisitPhoto saved = visitPhotoRepository.save(photo);
        return toDto(saved);
    }

    /**
     * 来店履歴に紐づく施術写真一覧を取得する.
     */
    public List<VisitPhotoDto> getPhotos(Long visitId, Long salonId) {
        findAndVerify(visitId, salonId);
        return visitPhotoRepository.findByReservationIdOrderByDisplayOrderAsc(visitId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * 施術写真を削除する.
     */
    public void deletePhoto(Long visitId, Long photoId, Long salonId) {
        findAndVerify(visitId, salonId);

        VisitPhoto photo = visitPhotoRepository.findById(photoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "写真が見つかりません"));

        if (!photo.getReservationId().equals(visitId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "指定した来店履歴に属する写真ではありません");
        }

        visitPhotoRepository.deleteById(photoId);
    }

    // ---- private ----

    private Reservation findAndVerify(Long visitId, Long salonId) {
        Reservation reservation = reservationRepository.findById(visitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "来店履歴が見つかりません"));
        if (!reservation.getSalonId().equals(salonId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        return reservation;
    }

    private VisitPhotoDto toDto(VisitPhoto photo) {
        return new VisitPhotoDto(photo.getId(), photo.getImageUrl(), photo.getDisplayOrder());
    }
}
