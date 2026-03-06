package jp.bitspace.salon.controller.admin;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jp.bitspace.salon.dto.request.SendMessageRequest;
import jp.bitspace.salon.dto.response.MessageResponse;
import jp.bitspace.salon.security.AdminRequestAuthUtil;
import jp.bitspace.salon.service.MessageService;

/**
 * 管理側LINEメッセージコントローラ.
 * <p>
 * 顧客とのLINEメッセージ履歴の閲覧・送信を行います。
 */
@RestController
@RequestMapping("/api/admin/messages")
public class AdminMessageController {

    private final MessageService messageService;
    private final AdminRequestAuthUtil adminRequestAuthUtil;

    public AdminMessageController(MessageService messageService, AdminRequestAuthUtil adminRequestAuthUtil) {
        this.messageService = messageService;
        this.adminRequestAuthUtil = adminRequestAuthUtil;
    }
    
    
    // TODO ページネーション（あとで）
    /**
     * 顧客とのメッセージ履歴を取得.
     *
     * @param httpServletRequest HTTPリクエスト
     * @param salonId サロンID
     * @param customerId 顧客ID
     * @return メッセージ一覧
     */
    @GetMapping
    public ResponseEntity<List<MessageResponse>> getMessages(
            HttpServletRequest httpServletRequest,
            @RequestParam(name = "salonId") Long salonId,
            @RequestParam(name = "customerId") Long customerId) {

        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);

        List<MessageResponse> responses = messageService.getMessagesBySalonAndCustomer(salonId, customerId);
        return ResponseEntity.ok(responses);
    }

    /**
     * 管理者からメッセージを送信.
     *
     * @param httpServletRequest HTTPリクエスト
     * @param request 送信リクエスト
     * @return 作成されたメッセージ
     */
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody SendMessageRequest request) {

        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, request.getSalonId());

        MessageResponse response = messageService.sendMessage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 顧客のメッセージを既読にする.
     *
     * @param httpServletRequest HTTPリクエスト
     * @param customerId 顧客ID
     * @param salonId サロンID
     * @return 204 No Content
     */
    @PutMapping("/read/{customerId}")
    public ResponseEntity<Void> markAsRead(
            HttpServletRequest httpServletRequest,
            @PathVariable Long customerId,
            @RequestParam(name = "salonId") Long salonId) {

        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);

        messageService.markAsRead(salonId, customerId);
        return ResponseEntity.noContent().build();
    }

    /**
     * サロンの未読メッセージ数を取得.
     *
     * @param httpServletRequest HTTPリクエスト
     * @param salonId サロンID
     * @return 未読数
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(
            HttpServletRequest httpServletRequest,
            @RequestParam(name = "salonId") Long salonId) {

        adminRequestAuthUtil.requireStaffAndSalonMatch(httpServletRequest, salonId);

        Long count = messageService.getUnreadCount(salonId);
        return ResponseEntity.ok(count);
    }
}
