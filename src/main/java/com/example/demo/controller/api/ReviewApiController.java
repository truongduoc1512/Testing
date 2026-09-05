package com.example.demo.controller.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.ProductReviewDAO;
import com.example.demo.entity.ProductReview;
import com.example.demo.form.ProductReviewForm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "5. Review REST API", description = "RESTful APIs dành cho gửi và quản lý nhận xét/đánh giá sản phẩm (JSON output)")
@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewApiController.class);

    @Autowired
    private ProductReviewDAO productReviewDAO;

    @Operation(summary = "Lấy danh sách đánh giá của sản phẩm theo mã Code")
    @GetMapping("/product/{productCode}")
    public ResponseEntity<List<ProductReview>> getReviewsByProductCode(@PathVariable("productCode") String productCode) {
        List<ProductReview> reviews = productReviewDAO.getReviewsByProductCode(productCode);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "Gửi bài đánh giá mới cho sản phẩm (JSON Payload)")
    @PostMapping
    public ResponseEntity<?> saveReview(@RequestBody ProductReviewForm reviewForm) {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để gửi đánh giá!"));
        }
        String username = auth.getName();
        if (auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Tài khoản quản trị không thể tạo đánh giá sản phẩm!"));
        }

        if (reviewForm == null || reviewForm.getProductCode() == null || reviewForm.getProductCode().trim().isEmpty() ||
            reviewForm.getProductCode().trim().length() > 20 || reviewForm.getComment() == null
            || reviewForm.getComment().trim().isEmpty() || reviewForm.getComment().trim().length() > 2000) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Vui lòng nhập đầy đủ productCode và nội dung comment!"));
        }
        if (reviewForm.getRatingValue() < 1 || reviewForm.getRatingValue() > 5) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Số sao đánh giá phải từ 1 đến 5!"));
        }

        try {
            ProductReview review = new ProductReview(reviewForm.getProductCode().trim(), username, reviewForm.getRatingValue(), reviewForm.getComment().trim());
            productReviewDAO.saveReview(review);

            Map<String, Object> response = ApiResponse.success("Đã gửi đánh giá sản phẩm thành công!");
            response.put("review", review);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            LOGGER.error("Không thể lưu đánh giá cho sản phẩm {}", reviewForm.getProductCode(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Không thể gửi đánh giá."));
        }
    }

    @Operation(summary = "Chỉnh sửa nội dung đánh giá (trong vòng 24 giờ sau khi đăng)")
    @PutMapping("/{reviewId}")
    public ResponseEntity<?> updateReview(
            @PathVariable("reviewId") Long reviewId,
            @RequestBody Map<String, Object> payload) {
        
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để sửa đánh giá!"));
        }
        String username = auth.getName();

        Integer ratingValue = payload == null ? null : readRating(payload.get("ratingValue"));
        Object rawComment = payload == null ? null : payload.get("comment");
        String comment = rawComment instanceof String ? (String) rawComment : null;

        if (comment == null || comment.trim().isEmpty() || comment.trim().length() > 2000) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Nội dung nhận xét không được để trống!"));
        }
        if (ratingValue == null || ratingValue < 1 || ratingValue > 5) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Số sao đánh giá phải từ 1 đến 5!"));
        }

        ProductReview existingReview = productReviewDAO.findReview(reviewId);
        if (existingReview != null && existingReview.getCreatedAt() != null
                && existingReview.getUsername() != null && existingReview.getUsername().equalsIgnoreCase(username)) {
            long diff = System.currentTimeMillis() - existingReview.getCreatedAt().getTime();
            if (diff > 24 * 60 * 60 * 1000L) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Chỉ có thể chỉnh sửa đánh giá trong vòng 24 giờ"));
            }
        }

        boolean success = productReviewDAO.updateReview(reviewId, username, ratingValue, comment.trim());
        if (success) {
            ProductReview updatedReview = productReviewDAO.findReview(reviewId);
            return ResponseEntity.ok(updatedReview);
        } else {
            if (existingReview != null && existingReview.getCreatedAt() != null) {
                long diff = System.currentTimeMillis() - existingReview.getCreatedAt().getTime();
                if (diff > 24 * 60 * 60 * 1000L) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(ApiResponse.error("Chỉ có thể chỉnh sửa đánh giá trong vòng 24 giờ"));
                }
            }
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    "Không thể sửa bài đánh giá (đã quá 24 giờ kể từ lúc đăng hoặc không có quyền sửa)."));
        }
    }

    @Operation(summary = "Xóa bài đánh giá theo ID")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<?> deleteReview(
            @PathVariable("reviewId") Long reviewId) {
        
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để xóa đánh giá!"));
        }
        String username = auth.getName();

        boolean success = productReviewDAO.deleteReview(reviewId, username);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("Đã xóa bài đánh giá thành công!"));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    "Không thể xóa bài đánh giá (không tồn tại hoặc không phải là người sở hữu)."));
        }
    }

    private Integer readRating(Object value) {
        if (!(value instanceof Number)) {
            return null;
        }
        double rating = ((Number) value).doubleValue();
        if (!Double.isFinite(rating) || rating != Math.rint(rating)) {
            return null;
        }
        return (int) rating;
    }
}
