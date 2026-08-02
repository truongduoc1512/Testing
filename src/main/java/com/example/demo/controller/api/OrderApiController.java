package com.example.demo.controller.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.OrderDAO;
import com.example.demo.model.OrderDetailInfo;
import com.example.demo.model.OrderInfo;
import com.example.demo.model.OrderStatus;
import com.example.demo.pagination.PaginationResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "2. Order REST API", description = "RESTful APIs dành cho tra cứu và quản lý đơn hàng (JSON output)")
@RestController
@RequestMapping("/api/v1/orders")
public class OrderApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderApiController.class);

    @Autowired
    private OrderDAO orderDAO;

    private Authentication currentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private String currentRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(role -> "ROLE_ADMIN".equals(role) || "ROLE_USER".equals(role))
                .findFirst().orElse("");
    }

    private boolean canAccessOrder(String orderId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return false;
        }
        return orderDAO.canAccessOrder(orderId, auth.getName(), currentRole(auth));
    }

    @Operation(summary = "Lấy danh sách tất cả các đơn hàng có phân trang")
    @GetMapping
    public ResponseEntity<PaginationResult<OrderInfo>> getOrders(
            @RequestParam(value = "page", defaultValue = "1") int page) {
        Authentication auth = currentAuthentication();
        int maxResult = 10;
        int maxNavigationPage = 10;
        PaginationResult<OrderInfo> result = orderDAO.listOrderInfo(Math.max(page, 1), maxResult,
                maxNavigationPage, auth.getName(), currentRole(auth));
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Lấy chi tiết đơn hàng theo ID")
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable("orderId") String orderId) {
        OrderInfo orderInfo = orderDAO.getOrderInfo(orderId);
        if (orderInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy đơn hàng với ID: " + orderId));
        }
        if (!canAccessOrder(orderId, currentAuthentication())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bạn không có quyền xem đơn hàng này."));
        }
        Authentication auth = currentAuthentication();
        String role = currentRole(auth);
        List<OrderDetailInfo> details = orderDAO.listOrderDetailInfosForPrincipal(
                orderId, auth.getName(), role);
        orderInfo.setDetails(details);
        if ("ROLE_ADMIN".equals(role) && !orderDAO.isOrderCustomer(orderId, auth.getName())) {
            orderInfo.setAmount(details.stream().mapToDouble(OrderDetailInfo::getAmount).sum());
        }
        return ResponseEntity.ok(orderInfo);
    }

    @Operation(summary = "Cập nhật trạng thái đơn hàng (PENDING, APPROVED, SHIPPING, COMPLETED, CANCELLED)")
    @PutMapping("/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable("orderId") String orderId,
            @RequestBody Map<String, String> payload) {
        String status = payload == null ? null : payload.get("status");
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Trạng thái mới 'status' không được để trống!"));
        }
        String normalizedStatus = OrderStatus.normalize(status);
        if (!OrderStatus.isAdminStatus(normalizedStatus)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Trạng thái đơn hàng không hợp lệ!"));
        }
        OrderInfo orderInfo = orderDAO.getOrderInfo(orderId);
        if (orderInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy đơn hàng để cập nhật với ID: " + orderId));
        }
        if (!orderDAO.canManageOrder(orderId, currentAuthentication().getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bạn không có quyền cập nhật đơn hàng này."));
        }
        try {
            orderDAO.updateOrderStatus(orderId, normalizedStatus);
            OrderInfo updatedOrder = orderDAO.getOrderInfo(orderId);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Không thể cập nhật trạng thái đơn hàng {}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Không thể cập nhật trạng thái đơn hàng."));
        }
    }
}
