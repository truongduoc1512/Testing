package com.example.demo.controller;

import java.io.IOException;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.dao.OrderDAO;
import com.example.demo.dao.ProductDAO;
import com.example.demo.entity.Product;
import com.example.demo.form.CustomerForm;
import com.example.demo.model.CartInfo;
import com.example.demo.model.CartLineInfo;
import com.example.demo.model.CustomerInfo;
import com.example.demo.model.ProductInfo;
import com.example.demo.pagination.PaginationResult;
import com.example.demo.utils.Utils;
import com.example.demo.validator.CustomerFormValidator;

@Controller
@Transactional
public class MainController {
 
   @Autowired
   private OrderDAO orderDAO;
 
   @Autowired
   private ProductDAO productDAO;
 
   @Autowired
   private CustomerFormValidator customerFormValidator;
 
   @InitBinder
   public void myInitBinder(WebDataBinder dataBinder) {
      Object target = dataBinder.getTarget();
      if (target == null) {
         return;
      }
      System.out.println("Target=" + target);
 
      // Case update quantity in cart
      // (@ModelAttribute("cartForm") @Validated CartInfo cartForm)
      if (target.getClass() == CartInfo.class) {
 
      }
 
      // Case save customer information.
      // (@ModelAttribute @Validated CustomerInfo customerForm)
      else if (target.getClass() == CustomerForm.class) {
         dataBinder.setValidator(customerFormValidator);
      }
 
   }
 
   @RequestMapping("/403")
   public String accessDenied() {
      return "/403";
   }
 
   @RequestMapping("/")
   public String home(Model model, @RequestParam(value = "keyword", defaultValue = "") String keyword) {
      // LỖ HỔNG: Nhận tham số keyword từ URL và đẩy thẳng ra view
      if (keyword != null && !keyword.isEmpty()) {
          model.addAttribute("keyword", keyword);
      }
      return "index";
   }
 
   // Product List
   @RequestMapping({ "/productList" })
   public String listProductHandler(HttpServletRequest request, Model model, //
         @RequestParam(value = "name", defaultValue = "") String likeName,
         @RequestParam(value = "page", defaultValue = "1") int page,
         @RequestParam(value = "sort", defaultValue = "newest") String sort,
         @RequestParam(value = "minPrice", required = false) Double minPrice,
         @RequestParam(value = "maxPrice", required = false) Double maxPrice,
         @RequestParam(value = "location", required = false) String location,
         @RequestParam(value = "brand", required = false) String brand,
         @RequestParam(value = "isMall", required = false) Boolean isMall,
         @RequestParam(value = "isFavored", required = false) Boolean isFavored,
         @RequestParam(value = "rating", required = false) Integer rating) {
      int maxResult = 12;
      int maxNavigationPage = 10;
 
      String ownerUsername = null;
      org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
      if (auth != null && auth.isAuthenticated() && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
          boolean isManager = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
          if (isManager) {
              ownerUsername = auth.getName();
          }
      }
 
      PaginationResult<ProductInfo> result = productDAO.queryProducts(page, 
            maxResult, maxNavigationPage, likeName, ownerUsername, sort, minPrice, maxPrice, 
            location, brand, isMall, isFavored, rating);
 
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
      return "productList";
   }
 
   @RequestMapping({ "/buyProduct" })
   public String listProductHandler(HttpServletRequest request, Model model, //
         @RequestParam(value = "code", defaultValue = "") String code) {
 
      Product product = null;
      if (code != null && code.length() > 0) {
         product = productDAO.findProduct(code);
      }
      if (product != null) {
 
         //
         CartInfo cartInfo = Utils.getCartInSession(request);
 
         ProductInfo productInfo = new ProductInfo(product);
 
         cartInfo.addProduct(productInfo, 1);
      }
 
      return "redirect:/shoppingCart";
   }

   @RequestMapping({ "/addToCart" })
   public String addToCartHandler(HttpServletRequest request, Model model, //
         @RequestParam(value = "code", defaultValue = "") String code,
         final org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
 
      Product product = null;
      if (code != null && code.length() > 0) {
         product = productDAO.findProduct(code);
      }
      if (product != null) {
         CartInfo cartInfo = Utils.getCartInSession(request);
         ProductInfo productInfo = new ProductInfo(product);
         cartInfo.addProduct(productInfo, 1);
         redirectAttributes.addFlashAttribute("message", "Đã thêm sản phẩm \"" + product.getName() + "\" vào giỏ hàng!");
      }
 
      return "redirect:/productList";
   }
 
   @RequestMapping({ "/shoppingCartRemoveProduct" })
   public String removeProductHandler(HttpServletRequest request, Model model, //
         @RequestParam(value = "code", defaultValue = "") String code) {
      Product product = null;
      if (code != null && code.length() > 0) {
         product = productDAO.findProduct(code);
      }
      if (product != null) {
 
         CartInfo cartInfo = Utils.getCartInSession(request);
 
         ProductInfo productInfo = new ProductInfo(product);
 
         cartInfo.removeProduct(productInfo);
 
      }
 
      return "redirect:/shoppingCart";
   }
 
   // POST: Update quantity for product in cart
   @RequestMapping(value = { "/shoppingCart" }, method = RequestMethod.POST)
   public String shoppingCartUpdateQty(HttpServletRequest request, //
         Model model, //
         @ModelAttribute("cartForm") CartInfo cartForm) {
 
      CartInfo cartInfo = Utils.getCartInSession(request);
      cartInfo.updateQuantity(cartForm);
 
      return "redirect:/shoppingCart";
   }
 
    // GET: Show cart.
    @RequestMapping(value = { "/shoppingCart" }, method = RequestMethod.GET)
    public String shoppingCartHandler(HttpServletRequest request, Model model) {
       CartInfo myCart = Utils.getCartInSession(request);
       CartInfo cartInfo = Utils.getCartInSession(request);
 
       model.addAttribute("cartForm", myCart);
       model.addAttribute("myCart", cartInfo);
       
       // Query 4 recommended products for the "You May Also Like" section
       PaginationResult<ProductInfo> recommendedProducts = productDAO.queryProducts(1, 4, 5, null, null, null, null, null, null, null, null, null, null);
       if (recommendedProducts != null) {
          model.addAttribute("recommendedProducts", recommendedProducts.getList());
       }
       
       return "shoppingCart";
    }
 
   // GET: Enter customer information.
   @RequestMapping(value = { "/shoppingCartCustomer" }, method = RequestMethod.GET)
   public String shoppingCartCustomerForm(HttpServletRequest request, Model model) {
 
      CartInfo cartInfo = Utils.getCartInSession(request);
 
      if (cartInfo.isEmpty()) {
 
         return "redirect:/shoppingCart";
      }
      CustomerInfo customerInfo = cartInfo.getCustomerInfo();
 
      CustomerForm customerForm = new CustomerForm(customerInfo);
 
      model.addAttribute("customerForm", customerForm);
 
      return "shoppingCartCustomer";
   }
 
   // POST: Save customer information.
   @RequestMapping(value = { "/shoppingCartCustomer" }, method = RequestMethod.POST)
   public String shoppingCartCustomerSave(HttpServletRequest request, //
         Model model, //
         @ModelAttribute("customerForm") @Validated CustomerForm customerForm, //
         BindingResult result, //
         final RedirectAttributes redirectAttributes) {
 
      if (result.hasErrors()) {
         customerForm.setValid(false);
         // Forward to reenter customer info.
         return "shoppingCartCustomer";
      }
 
      customerForm.setValid(true);
      CartInfo cartInfo = Utils.getCartInSession(request);
      CustomerInfo customerInfo = new CustomerInfo(customerForm);
      cartInfo.setCustomerInfo(customerInfo);
 
      return "redirect:/shoppingCartConfirmation";
   }
 
   // GET: Show information to confirm.
   @RequestMapping(value = { "/shoppingCartConfirmation" }, method = RequestMethod.GET)
   public String shoppingCartConfirmationReview(HttpServletRequest request, Model model) {
      CartInfo cartInfo = Utils.getCartInSession(request);
 
      if (cartInfo == null || cartInfo.isEmpty()) {
 
         return "redirect:/shoppingCart";
      } else if (!cartInfo.isValidCustomer()) {
 
         return "redirect:/shoppingCartCustomer";
      }
      model.addAttribute("myCart", cartInfo);
 
      return "shoppingCartConfirmation";
   }
 
   // POST: Submit Cart (Save)
   @RequestMapping(value = { "/shoppingCartConfirmation" }, method = RequestMethod.POST)
 
   public String shoppingCartConfirmationSave(HttpServletRequest request, Model model) {
      CartInfo cartInfo = Utils.getCartInSession(request);
 
      if (cartInfo.isEmpty()) {
 
         return "redirect:/shoppingCart";
      } else if (!cartInfo.isValidCustomer()) {
 
         return "redirect:/shoppingCartCustomer";
      }
      try {
         orderDAO.saveOrder(cartInfo);
      } catch (Exception e) {
         e.printStackTrace();
         return "shoppingCartConfirmation";
      }
 
      // Remove Cart from Session.
      Utils.removeCartInSession(request);
 
      // Store last cart.
      Utils.storeLastOrderedCartInSession(request, cartInfo);
 
      return "redirect:/shoppingCartFinalize";
   }
 
   @RequestMapping(value = { "/shoppingCartFinalize" }, method = RequestMethod.GET)
   public String shoppingCartFinalize(HttpServletRequest request, Model model) {
 
      CartInfo lastOrderedCart = Utils.getLastOrderedCartInSession(request);
 
      if (lastOrderedCart == null) {
         return "redirect:/shoppingCart";
      }
      model.addAttribute("lastOrderedCart", lastOrderedCart);
      return "shoppingCartFinalize";
   }
 
    @ResponseBody
    @RequestMapping(value = { "/api/updateCartQuantity" }, method = RequestMethod.POST)
    public java.util.Map<String, Object> updateCartQuantityAjax(HttpServletRequest request,
            @RequestParam("code") String code,
            @RequestParam("quantity") int quantity) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        CartInfo cartInfo = Utils.getCartInSession(request);
        Product product = productDAO.findProduct(code);
        if (product != null) {
            cartInfo.updateProduct(code, quantity);
            double lineAmount = 0;
            for (CartLineInfo line : cartInfo.getCartLines()) {
                if (line.getProductInfo().getCode().equals(code)) {
                    lineAmount = line.getAmount();
                    break;
                }
            }
            response.put("success", true);
            response.put("lineAmount", lineAmount);
            response.put("quantityTotal", cartInfo.getQuantityTotal());
            response.put("amountTotal", cartInfo.getAmountTotal());
        } else {
            response.put("success", false);
            response.put("message", "Sản phẩm không tồn tại!");
        }
        return response;
    }

    @ResponseBody
    @RequestMapping(value = { "/api/removeCartProduct" }, method = RequestMethod.POST)
    public java.util.Map<String, Object> removeCartProductAjax(HttpServletRequest request,
            @RequestParam("code") String code) {
        java.util.Map<String, Object> response = new java.util.HashMap<>();
        CartInfo cartInfo = Utils.getCartInSession(request);
        Product product = productDAO.findProduct(code);
        if (product != null) {
            ProductInfo productInfo = new ProductInfo(product);
            cartInfo.removeProduct(productInfo);
            response.put("success", true);
            response.put("quantityTotal", cartInfo.getQuantityTotal());
            response.put("amountTotal", cartInfo.getAmountTotal());
        } else {
            response.put("success", false);
            response.put("message", "Sản phẩm không tồn tại!");
        }
        return response;
    }

    @RequestMapping(value = { "/productImage" }, method = RequestMethod.GET)
   public void productImage(HttpServletRequest request, HttpServletResponse response, Model model,
         @RequestParam("code") String code) throws IOException {
      Product product = null;
      if (code != null) {
         product = this.productDAO.findProduct(code);
      }
      if (product != null && product.getImage() != null) {
         response.setContentType("image/jpeg, image/jpg, image/png, image/gif");
         response.getOutputStream().write(product.getImage());
      }
      response.getOutputStream().close();
   }
   
   // --- DEMO PATH TRAVERSAL ---
   // Giả lập tính năng xem file log hoặc file ảnh
   @RequestMapping(value = "/viewFile", method = RequestMethod.GET)
   @ResponseBody
   public String viewFile(@RequestParam("filename") String filename) {
      try {
         // LỖ HỔNG: Hacker có thể nhập filename là "../../../etc/passwd"
         // Giả sử ứng dụng quy định chỉ được đọc trong thư mục "static"
         String basePath = "src/main/resources/static/";
         
         File file = new File(basePath + filename);
         
         // Đọc nội dung file và trả về màn hình
         if (file.exists()) {
             return new String(Files.readAllBytes(file.toPath()));
         } else {
             // Thử đọc file hệ thống nếu file trong static không có (Mô phỏng Hacker thoát ra ngoài)
             // Lưu ý: Trong Docker container, đường dẫn gốc là /app
             File systemFile = new File(filename); // Nguy hiểm nhất là dòng này
             if (systemFile.exists()) {
                 return new String(Files.readAllBytes(systemFile.toPath()));
             }
         }
         return "File not found: " + filename;
      } catch (Exception e) {
         return "Error: " + e.getMessage();
      }
   }
}
