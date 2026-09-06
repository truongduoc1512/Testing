package com.example.demo.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dao.ProductDAO;
import com.example.demo.entity.Product;
import com.example.demo.form.ProductForm;
import com.example.demo.model.ProductInfo;
import com.example.demo.pagination.PaginationResult;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "1. Product REST API", description = "RESTful APIs dành cho quản lý và tra cứu sản phẩm (JSON output)")
@RestController
@RequestMapping("/api/v1/products")
public class ProductApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductApiController.class);

    @Autowired
    private ProductDAO productDAO;

    @Operation(summary = "Lấy danh sách sản phẩm có phân trang và bộ lọc")
    @GetMapping
    public ResponseEntity<PaginationResult<ProductInfo>> getProducts(
            @RequestParam(value = "name", defaultValue = "") String likeName,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sort", defaultValue = "newest") String sort,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "isMall", required = false) Boolean isMall,
            @RequestParam(value = "isFavored", required = false) Boolean isFavored,
            @RequestParam(value = "rating", required = false) Integer rating,
            @RequestParam(value = "category", required = false) String category) {
        
        int maxResult = 12;
        int maxNavigationPage = 10;
        PaginationResult<ProductInfo> result = productDAO.queryProducts(Math.max(page, 1), maxResult, maxNavigationPage,
                likeName, null, sort, minPrice, maxPrice, location, brand, isMall, isFavored, rating, category);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Lấy thông tin chi tiết một sản phẩm theo mã Code")
    @GetMapping("/{code}")
    public ResponseEntity<?> getProductByCode(@PathVariable("code") String code) {
        ProductInfo productInfo = productDAO.findProductInfo(code);
        if (productInfo == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy sản phẩm với mã: " + code));
        }
        return ResponseEntity.ok(productInfo);
    }

    @Operation(summary = "Tạo mới hoặc cập nhật sản phẩm (JSON Payload)")
    @PostMapping
    public ResponseEntity<?> saveProduct(@RequestBody ProductForm productForm) {
        if (productForm == null || productForm.getCode() == null || productForm.getCode().trim().isEmpty() ||
            productForm.getName() == null || productForm.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Mã và tên sản phẩm không được để trống!"));
        }
        try {
            productForm.setCode(productForm.getCode().trim());
            productForm.setName(productForm.getName().trim());
            Product existingProduct = productDAO.findProduct(productForm.getCode());
            boolean isNew = existingProduct == null;
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!isNew && !username.equals(existingProduct.getOwnerUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(ApiResponse.error("Bạn không có quyền cập nhật sản phẩm này!"));
            }
            productDAO.save(productForm);
            ProductInfo savedProduct = productDAO.findProductInfo(productForm.getCode());
            if (isNew) {
                return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
            }
            return ResponseEntity.ok(savedProduct);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bạn không có quyền cập nhật sản phẩm này!"));
        } catch (Exception e) {
            LOGGER.error("Không thể lưu sản phẩm qua REST", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Không thể lưu sản phẩm."));
        }
    }

    @Operation(summary = "Xóa sản phẩm theo mã Code")
    @DeleteMapping("/{code}")
    public ResponseEntity<?> deleteProduct(@PathVariable("code") String code) {
        Product product = productDAO.findProduct(code);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy sản phẩm cần xóa với mã: " + code));
        }
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!username.equals(product.getOwnerUsername())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Bạn không có quyền xóa sản phẩm này!"));
        }
        try {
            productDAO.deleteProduct(code);
            return ResponseEntity.ok(ApiResponse.success("Đã vô hiệu hóa sản phẩm thành công!"));
        } catch (Exception e) {
            LOGGER.error("Không thể vô hiệu hóa sản phẩm {} qua REST", code, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Không thể xóa sản phẩm."));
        }
    }
}
