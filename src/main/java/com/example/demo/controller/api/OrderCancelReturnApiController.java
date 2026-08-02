package com.example.demo.controller.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.OrderReturnDAO;
import com.example.demo.dao.OrderDAO;
import com.example.demo.entity.OrderReturn;
import com.example.demo.form.OrderReturnForm;
import com.example.demo.form.ReturnStatusUpdateForm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "9. Order Cancellation & Returns REST API", description = "APIs xử lý hủy đơn hàng và yêu cầu trả hàng / hoàn tiền (JSON output)")
@RestController
@RequestMapping("/api/v1")
public class OrderCancelReturnApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderCancelReturnApiController.class);

    @Autowired
    private OrderReturnDAO orderReturnDAO;

    @Autowired
    private OrderDAO orderDAO;

    private String getCurrentUsername() {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return null;
    }

    private boolean isAdmin() {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        }
        return false;
    }

    @Operation(summary = "Hủy đơn hàng đang chờ xử lý (User)")
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(@PathVariable("orderId") String orderId) {
        String username = getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để thực hiện hủy đơn!"));
        }

        try {
            boolean success = orderReturnDAO.cancelOrder(username, orderId);
            Map<String, Object> response = ApiResponse.message(success, "Hủy đơn hàng #" + orderId
                    + " thành công! Số lượng sản phẩm đã được hoàn lại kho.");
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Không thể hủy đơn hàng {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Không thể xử lý yêu cầu hủy đơn."));
        }
    }

    @Operation(summary = "Gửi yêu cầu trả hàng / hoàn tiền cho đơn hàng (User)")
    @PostMapping("/orders/{orderId}/return")
    public ResponseEntity<?> createReturnRequest(@PathVariable("orderId") String orderId, @RequestBody OrderReturnForm form) {
        String username = getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để gửi yêu cầu trả hàng!"));
        }

        if (form == null || form.getReason() == null || form.getReason().trim().isEmpty()
                || form.getReason().trim().length() > 2000
                || (form.getImageUrls() != null && form.getImageUrls().trim().length() > 500)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Vui lòng nhập lý do trả hàng!"));
        }

        try {
            OrderReturn orderReturn = orderReturnDAO.createReturnRequest(username, orderId, form);
            Map<String, Object> response = ApiResponse.success(
                    "Gửi yêu cầu trả hàng thành công! Đang chờ Admin duyệt.");
            response.put("data", orderReturn);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Không thể tạo yêu cầu trả hàng cho đơn {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Không thể tạo yêu cầu trả hàng."));
        }
    }

    @Operation(summary = "Xem thông tin chi tiết yêu cầu trả hàng của một đơn (User/Admin)")
    @GetMapping("/orders/{orderId}/return")
    public ResponseEntity<?> getReturnRequest(@PathVariable("orderId") String orderId) {
        String username = getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để xem yêu cầu trả hàng!"));
        }
        OrderReturn orderReturn = orderReturnDAO.findReturnByOrderId(orderId);
        if (orderReturn == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Đơn hàng này chưa có yêu cầu trả hàng nào."));
        }
        if (isAdmin() && !orderDAO.canAccessOrder(orderId, username, "ROLE_ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bạn không có quyền xem yêu cầu trả hàng này!"));
        }
        if (!isAdmin() && !username.equals(orderReturn.getUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bạn không có quyền xem yêu cầu trả hàng này!"));
        }
        return ResponseEntity.ok(orderReturn);
    }

    @Operation(summary = "Duyệt hoặc từ chối yêu cầu trả hàng (Admin)")
    @PutMapping("/admin/orders/{orderId}/return-status")
    public ResponseEntity<?> updateReturnStatus(@PathVariable("orderId") String orderId, @RequestBody ReturnStatusUpdateForm form) {
        if (!isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bạn không có quyền thực hiện chức năng này!"));
        }
        if (form == null || form.getAction() == null
                || (!"APPROVE".equalsIgnoreCase(form.getAction()) && !"REJECT".equalsIgnoreCase(form.getAction()))
                || (form.getAdminNote() != null && form.getAdminNote().trim().length() > 255)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Dữ liệu xử lý yêu cầu trả hàng không hợp lệ."));
        }

        try {
            OrderReturn updated = orderReturnDAO.updateReturnStatus(getCurrentUsername(), orderId, form.getAction(), form.getAdminNote());
            Map<String, Object> response = ApiResponse.success(
                    "Cập nhật trạng thái trả hàng thành công: " + updated.getStatus());
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Không thể cập nhật trạng thái trả hàng của đơn {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Không thể cập nhật trạng thái trả hàng."));
        }
    }
}
