package com.example.demo.controller.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.example.demo.dao.UserAddressDAO;
import com.example.demo.entity.UserAddress;
import com.example.demo.form.UserAddressForm;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "8. Address Book REST API", description = "APIs quản lý sổ địa chỉ giao hàng của người dùng (JSON output)")
@RestController
@RequestMapping("/api/v1/users/addresses")
public class UserAddressApiController {

    @Autowired
    private UserAddressDAO userAddressDAO;

    private String getCurrentUsername() {
        org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return auth.getName();
        }
        return null;
    }

    @Operation(summary = "Lấy danh sách sổ địa chỉ giao hàng của người dùng đang đăng nhập")
    @GetMapping
    public ResponseEntity<?> getUserAddresses() {
        String username = getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để xem sổ địa chỉ!"));
        }
        List<UserAddress> addresses = userAddressDAO.getUserAddresses(username);
        return ResponseEntity.ok(addresses);
    }

    @Operation(summary = "Thêm một địa chỉ giao hàng mới")
    @PostMapping
    public ResponseEntity<?> createAddress(@RequestBody UserAddressForm form) {
        String username = getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập để thêm địa chỉ!"));
        }

        String validationError = validateAddress(form);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(validationError));
        }

        form.setId(null); // Ensure creation
        UserAddress address = userAddressDAO.saveAddress(username, form);

        Map<String, Object> response = ApiResponse.success("Thêm địa chỉ giao hàng mới thành công!");
        response.put("address", address);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Cập nhật thông tin địa chỉ giao hàng")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable("id") Long id, @RequestBody UserAddressForm form) {
        String username = getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập!"));
        }

        UserAddress existing = userAddressDAO.getAddressById(id);
        if (existing == null || !existing.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Không tìm thấy địa chỉ hoặc bạn không có quyền chỉnh sửa!"));
        }

        String validationError = validateAddress(form);
        if (validationError != null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(validationError));
        }

        form.setId(id);
        UserAddress updated = userAddressDAO.saveAddress(username, form);

        Map<String, Object> response = ApiResponse.success("Cập nhật địa chỉ thành công!");
        response.put("address", updated);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Xóa một địa chỉ giao hàng")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable("id") Long id) {
        String username = getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập!"));
        }

        boolean deleted = userAddressDAO.deleteAddress(username, id);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Không tìm thấy địa chỉ hoặc bạn không có quyền xóa!"));
        }
        return ResponseEntity.ok(ApiResponse.success("Đã xóa địa chỉ giao hàng thành công!"));
    }

    @Operation(summary = "Đặt một địa chỉ làm địa chỉ mặc định")
    @PutMapping("/{id}/set-default")
    public ResponseEntity<?> setDefaultAddress(@PathVariable("id") Long id) {
        String username = getCurrentUsername();
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Vui lòng đăng nhập!"));
        }

        boolean updated = userAddressDAO.setDefaultAddress(username, id);
        if (!updated) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy địa chỉ!"));
        }
        return ResponseEntity.ok(ApiResponse.success("Đã đặt làm địa chỉ mặc định!"));
    }

    private String validateAddress(UserAddressForm form) {
        if (form == null || isBlank(form.getReceiverName()) || isBlank(form.getPhone())
                || isBlank(form.getProvince()) || isBlank(form.getDistrict())
                || isBlank(form.getWard()) || isBlank(form.getStreetAddress())) {
            return "Vui lòng điền đầy đủ thông tin địa chỉ giao hàng!";
        }
        if (form.getReceiverName().trim().length() > 100 || form.getPhone().trim().length() > 20
                || form.getProvince().trim().length() > 100 || form.getDistrict().trim().length() > 100
                || form.getWard().trim().length() > 100 || form.getStreetAddress().trim().length() > 255
                || (form.getNote() != null && form.getNote().trim().length() > 255)) {
            return "Thông tin địa chỉ vượt quá độ dài cho phép!";
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
