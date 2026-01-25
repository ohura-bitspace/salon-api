package jp.bitspace.salon.repository;

import jp.bitspace.salon.model.SalonConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * サロン設定リポジトリ.
 */
@Repository
public interface SalonConfigRepository extends JpaRepository<SalonConfig, Long> {
    /**
     * サロンIDで設定を検索.
     *
     * @param salonId サロンID
     * @return サロン設定
     */
    Optional<SalonConfig> findBySalonId(Long salonId);
}
