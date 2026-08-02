package com.example.demo.controller.api;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.OrderDAO;
import com.example.demo.dao.ProductDAO;
import com.example.demo.entity.Product;
import com.example.demo.form.CustomerForm;
import com.example.demo.model.CartInfo;
import com.example.demo.model.CustomerInfo;
import com.example.demo.model.ProductInfo;
import com.example.demo.utils.Utils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "3. Cart REST API", description = "RESTful APIs dành cho giỏ hàng và đặt hàng (JSON output)")
@RestController
@RequestMapping("/api/v1/cart")
public class CartApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CartApiController.class);

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private OrderDAO orderDAO;

    private Integer readQuantity(Map<String, Object> payload, int defaultValue) {
        if (payload == null) {
            return null;
        }
        Object value = payload.get("quantity");
        if (value == null) {
            return defaultValue;
        }
        try {
            if (value instanceof Number) {
                double numericValue = ((Number) value).doubleValue();
                if (!Double.isFinite(numericValue) || numericValue != Math.rint(numericValue)
                        || numericValue < Integer.MIN_VALUE || numericValue > Integer.MAX_VALUE) {
                    return null;
                }
                return (int) numericValue;
            }
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String readProductCode(Map<String, Object> payload) {
        if (payload == null) {
            return null;
        }
        Object value = payload.get("code");
        return value instanceof String ? ((String) value).trim() : null;
    }

    @Operation(summary = "Xem thông tin giỏ hàng hiện tại trong Session")
    @GetMapping
    public ResponseEntity<CartInfo> getCart(HttpServletRequest request) {
        CartInfo cartInfo = Utils.getCartInSession(request);
        return ResponseEntity.ok(cartInfo);
    }

    @Operation(summary = "Thêm sản phẩm vào giỏ hàng (JSON payload: code, quantity)")
    @PostMapping("/items")
    public ResponseEntity<?> addCartItem(HttpServletRequest request, @RequestBody Map<String, Object> payload) {
        String code = readProductCode(payload);
        Integer quantity = readQuantity(payload, 1);

        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mã sản phẩm 'code' không được để trống!"));
        }
        if (quantity == null || quantity < 1) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Số lượng sản phẩm phải là số nguyên lớn hơn 0!"));
        }

        Product product = productDAO.findActiveProduct(code);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Sản phẩm không tồn tại!"));
        }

        if (product.getStockQuantity() <= 0) {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    "Sản phẩm \"" + product.getName() + "\" đã hết hàng trong kho!"));
        }

        CartInfo cartInfo = Utils.getCartInSession(request);
        ProductInfo productInfo = new ProductInfo(product);
        cartInfo.addProduct(productInfo, quantity);

        return ResponseEntity.ok(cartInfo);
    }

    @Operation(summary = "Cập nhật số lượng sản phẩm trong giỏ hàng")
    @PutMapping("/items")
    public ResponseEntity<?> updateCartItemQuantity(HttpServletRequest request, @RequestBody Map<String, Object> payload) {
        String code = readProductCode(payload);
        Integer quantity = readQuantity(payload, 1);

        if (code == null || code.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mã sản phẩm 'code' không được để trống!"));
        }
        if (quantity == null || quantity < 1) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Số lượng sản phẩm phải là số nguyên lớn hơn 0!"));
        }

        Product product = productDAO.findActiveProduct(code);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Sản phẩm không tồn tại!"));
        }
        if (product.getStockQuantity() < 1) {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    "Sản phẩm đã hết hàng; số lượng trong giỏ chưa được thay đổi!"));
        }

        CartInfo cartInfo = Utils.getCartInSession(request);
        int actualQty = Math.min(quantity, product.getStockQuantity());
        boolean capped = actualQty != quantity;
        cartInfo.updateProduct(code, actualQty);

        Map<String, Object> response = new HashMap<>();
        response.put("cart", cartInfo);
        response.put("actualQuantity", actualQty);
        response.put("capped", capped);
        if (capped) {
            response.put("message", "Chỉ còn " + product.getStockQuantity() + " sản phẩm trong kho!");
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Xóa một sản phẩm khỏi giỏ hàng theo mã Code")
    @DeleteMapping("/items/{code}")
    public ResponseEntity<?> removeCartItem(HttpServletRequest request, @PathVariable("code") String code) {
        Product product = productDAO.findProduct(code);
        CartInfo cartInfo = Utils.getCartInSession(request);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Sản phẩm không tồn tại!"));
        }
        ProductInfo productInfo = new ProductInfo(product);
        cartInfo.removeProduct(productInfo);
        return ResponseEntity.ok(cartInfo);
    }

    @Operation(summary = "Lưu thông tin giao hàng người mua (Name, Email, Phone, Address)")
    @PostMapping("/customer")
    public ResponseEntity<?> saveCustomerInfo(HttpServletRequest request, @RequestBody CustomerForm customerForm) {
        if (!isValidCustomer(customerForm)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Vui lòng nhập đầy đủ Name, Email, Phone, và Address!"));
        }

        CartInfo cartInfo = Utils.getCartInSession(request);
        customerForm.setValid(true);
        CustomerInfo customerInfo = new CustomerInfo(customerForm);
        cartInfo.setCustomerInfo(customerInfo);

        return ResponseEntity.ok(cartInfo);
    }

    private boolean isValidCustomer(CustomerForm form) {
        return form != null && !isBlank(form.getName()) && form.getName().trim().length() <= 255
                && !isBlank(form.getAddress()) && form.getAddress().trim().length() <= 255
                && !isBlank(form.getEmail()) && form.getEmail().trim().length() <= 128
                && org.apache.commons.validator.routines.EmailValidator.getInstance()
                        .isValid(form.getEmail().trim())
                && !isBlank(form.getPhone()) && form.getPhone().trim().length() <= 128;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Operation(summary = "Tạo đơn hàng từ giỏ hàng hiện tại (Checkout & Submit Order)")
    @PostMapping("/checkout")
    public ResponseEntity<?> checkoutOrder(HttpServletRequest request) {
        CartInfo cartInfo = Utils.getCartInSession(request);

        if (cartInfo.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Giỏ hàng đang trống, không thể đặt hàng!"));
        }

        if (!cartInfo.isValidCustomer()) {
            return ResponseEntity.badRequest().body(ApiResponse.error(
                    "Vui lòng cập nhật thông tin người mua (CustomerInfo) trước khi đặt hàng!"));
        }

        try {
            orderDAO.saveOrder(cartInfo);
            Utils.removeCartInSession(request);
            Utils.storeLastOrderedCartInSession(request, cartInfo);

            Map<String, Object> response = ApiResponse.success("Đặt hàng thành công!");
            response.put("orderedCart", cartInfo);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            LOGGER.error("Không thể tạo đơn hàng từ REST checkout", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(
                    "Không thể tạo đơn hàng. Vui lòng kiểm tra lại giỏ hàng và tồn kho."));
        }
    }
}
