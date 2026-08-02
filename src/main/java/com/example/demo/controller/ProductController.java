package com.example.demo.controller;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dao.ProductDAO;
import com.example.demo.dao.ProductReviewDAO;
import com.example.demo.entity.Product;
import com.example.demo.entity.ProductReview;
import com.example.demo.form.ProductForm;
import com.example.demo.form.ProductReviewForm;
import com.example.demo.model.ProductInfo;
import com.example.demo.pagination.PaginationResult;
import com.example.demo.validator.ProductFormValidator;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product Controller", description = "Các API quản lý, danh sách và chi tiết sản phẩm")
@Controller
public class ProductController {

   private static final Logger LOGGER = LoggerFactory.getLogger(ProductController.class);

   @Value("${ai.service.url:http://localhost:8000}")
   private String aiServiceUrl;

   private final RestTemplate restTemplate = new RestTemplate();

   @Autowired
   private ProductDAO productDAO;

   @Autowired
   private ProductReviewDAO productReviewDAO;

   @Autowired
   private ProductFormValidator productFormValidator;

   @InitBinder
   public void myInitBinder(WebDataBinder dataBinder) {
      Object target = dataBinder.getTarget();
      if (target == null) {
         return;
      }
      if (target.getClass() == ProductForm.class) {
         dataBinder.setValidator(productFormValidator);
      }
   }

   // GET: Danh sách sản phẩm.
   @RequestMapping({ "/productList" })
   public String listProductHandler(HttpServletRequest request, Model model,
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

      String ownerUsername = null;
      org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
          boolean isManager = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
          if (isManager) {
              ownerUsername = auth.getName();
          }
      }

      PaginationResult<ProductInfo> result = productDAO.queryProducts(Math.max(page, 1),
            maxResult, maxNavigationPage, likeName, ownerUsername, sort, minPrice, maxPrice,
            location, brand, isMall, isFavored, rating, category);

      model.addAttribute("paginationProducts", result);
      model.addAttribute("likeName", likeName);
      model.addAttribute("sort", sort);
      model.addAttribute("minPrice", minPrice);
      model.addAttribute("maxPrice", maxPrice);
      model.addAttribute("location", location);
      model.addAttribute("brand", brand);
      model.addAttribute("isMall", isMall);
      model.addAttribute("isFavored", isFavored);
      model.addAttribute("rating", rating);
      model.addAttribute("category", category);
      return "productList";
   }

   // GET: Chi tiết sản phẩm, đánh giá và tồn kho.
   @RequestMapping(value = { "/productDetail" }, method = RequestMethod.GET)
   public String productDetail(Model model, @RequestParam("code") String code) {
      ProductInfo productInfo = productDAO.findProductInfo(code);
      if (productInfo == null) {
         return "redirect:/productList";
      }

      List<ProductReview> reviews = productReviewDAO.getReviewsByProductCode(code);
      ProductReviewForm reviewForm = new ProductReviewForm(code);

      model.addAttribute("productInfo", productInfo);
      model.addAttribute("reviewsList", reviews);
      model.addAttribute("productReviewForm", reviewForm);

      return "productDetail";
   }

   // GET: Ảnh sản phẩm.
   @RequestMapping(value = { "/productImage" }, method = RequestMethod.GET)
   public void productImage(HttpServletRequest request, HttpServletResponse response, Model model,
         @RequestParam("code") String code) throws IOException {
      Product product = null;
      if (code != null) {
         product = this.productDAO.findProduct(code);
      }
      if (product != null && product.getImage() != null) {
         byte[] image = product.getImage();
         String contentType = java.net.URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(image));
         response.setContentType(contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM_VALUE);
         response.getOutputStream().write(image);
      }
      response.getOutputStream().close();
   }

   // GET: Hiển thị biểu mẫu chỉnh sửa sản phẩm cho quản trị viên.
   @RequestMapping(value = { "/admin/product" }, method = RequestMethod.GET)
   public String product(Model model, @RequestParam(value = "code", defaultValue = "") String code,
         final RedirectAttributes redirectAttributes) {
      ProductForm productForm = null;

      if (code != null && code.length() > 0) {
         Product product = productDAO.findProduct(code);
         if (product != null) {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!currentUsername.equals(product.getOwnerUsername())) {
               redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền chỉnh sửa sản phẩm của người khác!");
               return "redirect:/productList";
            }
            productForm = new ProductForm(product);
         }
      }
      if (productForm == null) {
         productForm = new ProductForm();
         productForm.setNewProduct(true);
      }
      model.addAttribute("productForm", productForm);
      return "product";
   }

   // POST: Lưu sản phẩm.
   @RequestMapping(value = { "/admin/product" }, method = RequestMethod.POST)
   public String productSave(Model model,
         @ModelAttribute("productForm") @Validated ProductForm productForm,
         BindingResult result,
         final RedirectAttributes redirectAttributes) {

      if (result.hasErrors()) {
         return "product";
      }

      productForm.setCode(productForm.getCode().trim());
      productForm.setName(productForm.getName().trim());

      Product existingProduct = productDAO.findProduct(productForm.getCode());
      String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
      if (existingProduct != null && !currentUsername.equals(existingProduct.getOwnerUsername())) {
         redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền cập nhật sản phẩm của người khác!");
         return "redirect:/productList";
      }

      // ── AI Quality Gate: Kiểm duyệt ảnh trước khi lưu ──────────────────────
      if (productForm.getFileData() != null && !productForm.getFileData().isEmpty()) {
         try {
            byte[] imageBytes = productForm.getFileData().getBytes();
            String originalFilename = productForm.getFileData().getOriginalFilename();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ByteArrayResource imageResource = new ByteArrayResource(imageBytes) {
               @Override
               public String getFilename() {
                  return originalFilename != null ? originalFilename : "product.jpg";
               }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", imageResource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            String analyzeUrl = aiServiceUrl + "/api/v1/analyze";
            ResponseEntity<Map> response = restTemplate.postForEntity(analyzeUrl, requestEntity, Map.class);

            Map<?, ?> aiResult = response.getBody();
            if (aiResult != null) {
               Boolean approved = (Boolean) aiResult.get("approved");
               String reason = (String) aiResult.get("reason");

               if (approved != null && !approved) {
                  model.addAttribute("aiError", reason);
                  model.addAttribute("aiMetrics", aiResult.get("metrics"));
                  return "product";
               }
            }
         } catch (Exception aiEx) {
            LOGGER.warn("AI quality service không khả dụng", aiEx);
            model.addAttribute("aiWarning", "AI service tạm thời không khả dụng. Ảnh sẽ được lưu mà không qua kiểm duyệt.");
         }
      }

      try {
         productDAO.save(productForm);
         redirectAttributes.addFlashAttribute("message", "Lưu sản phẩm thành công!");
      } catch (IllegalArgumentException e) {
         model.addAttribute("errorMessage", e.getMessage());
         return "product";
      } catch (AccessDeniedException e) {
         redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền cập nhật sản phẩm này.");
         return "redirect:/403";
      } catch (Exception e) {
         LOGGER.error("Không thể lưu sản phẩm {}", productForm.getCode(), e);
         model.addAttribute("errorMessage", "Không thể lưu sản phẩm. Vui lòng kiểm tra lại dữ liệu.");
         return "product";
      }

      return "redirect:/productList";
   }

   // POST: Vô hiệu hóa sản phẩm, giữ nguyên lịch sử đơn hàng.
   @RequestMapping(value = { "/admin/deleteProduct" }, method = RequestMethod.POST)
   public String deleteProduct(Model model, @RequestParam(value = "code", defaultValue = "") String code,
         final RedirectAttributes redirectAttributes) {
      if (code != null && code.length() > 0) {
         try {
            Product product = productDAO.findProduct(code);
            if (product != null) {
               String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
               if (!currentUsername.equals(product.getOwnerUsername())) {
                  redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xóa sản phẩm của người khác!");
                  return "redirect:/productList";
               }
            }
            productDAO.deleteProduct(code);
            redirectAttributes.addFlashAttribute("message", "Đã vô hiệu hóa sản phẩm thành công!");
         } catch (Exception e) {
            LOGGER.error("Không thể vô hiệu hóa sản phẩm {}", code, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể vô hiệu hóa sản phẩm.");
         }
      }
      return "redirect:/productList";
   }
}
