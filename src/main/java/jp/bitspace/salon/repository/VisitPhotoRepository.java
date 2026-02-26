package jp.bitspace.salon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.bitspace.salon.model.VisitPhoto;

@Repository
public interface VisitPhotoRepository extends JpaRepository<VisitPhoto, Long> {

    List<VisitPhoto> findByReservationIdOrderByDisplayOrderAsc(Long reservationId);

    int countByReservationId(Long reservationId);
}
