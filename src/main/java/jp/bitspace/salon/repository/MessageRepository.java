package jp.bitspace.salon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import jp.bitspace.salon.model.Message;

/**
 * メッセージリポジトリ.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * サロン・顧客別のメッセージ履歴を時系列で取得.
     */
    List<Message> findBySalonIdAndCustomerIdOrderByCreatedAtAsc(Long salonId, Long customerId);

    /**
     * サロンの未読メッセージ数を取得.
     */
    long countBySalonIdAndIsReadFalse(Long salonId);

    /**
     * サロン・顧客の未読メッセージ一覧を取得.
     */
    List<Message> findBySalonIdAndCustomerIdAndIsReadFalse(Long salonId, Long customerId);
}
