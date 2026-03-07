package jp.bitspace.salon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.bitspace.salon.model.MailWebhookLog;

/**
 * メール Webhook 受信ログリポジトリ.
 */
@Repository
public interface MailWebhookLogRepository extends JpaRepository<MailWebhookLog, Long> {

    List<MailWebhookLog> findBySalonIdOrderByCreatedAtDesc(Long salonId);
}
