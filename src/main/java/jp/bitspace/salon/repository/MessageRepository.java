package jp.bitspace.salon.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * サロンのメッセージスレッド一覧を取得（顧客ごとの最新メッセージ・未読数）.
     */
    @Query(value = """
        SELECT
            m.customer_id AS customerId,
            COALESCE(CONCAT(c.last_name, ' ', c.first_name), c.line_display_name, '') AS customerName,
            latest.text AS lastMessage,
            latest.created_at AS lastMessageAt,
            COALESCE(unread.cnt, 0) AS unreadCount
        FROM (
            SELECT customer_id, MAX(created_at) AS max_created_at
            FROM messages
            WHERE salon_id = :salonId
            GROUP BY customer_id
        ) m
        JOIN messages latest
            ON latest.customer_id = m.customer_id
            AND latest.salon_id = :salonId
            AND latest.created_at = m.max_created_at
        JOIN customers c ON c.id = m.customer_id
        LEFT JOIN (
            SELECT customer_id, COUNT(*) AS cnt
            FROM messages
            WHERE salon_id = :salonId AND is_read = false
            GROUP BY customer_id
        ) unread ON unread.customer_id = m.customer_id
        ORDER BY latest.created_at DESC
        """, nativeQuery = true)
    List<Object[]> findThreadsBySalonId(@Param("salonId") Long salonId);
}
