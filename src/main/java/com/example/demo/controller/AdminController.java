package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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

import com.example.demo.dao.AccountDAO;
import com.example.demo.dao.OrderDAO;
import com.example.demo.dao.ProductDAO;
import com.example.demo.entity.Account;
import com.example.demo.entity.Product;
import com.example.demo.form.ProductForm;
import com.example.demo.form.UserProfileForm;
import com.example.demo.model.OrderDetailInfo;
import com.example.demo.model.OrderInfo;
import com.example.demo.pagination.PaginationResult;
import com.example.demo.validator.ProductFormValidator;

@Controller
@Transactional
public class AdminController {

   @Value("${ai.service.url:http://localhost:8000}")
   private String aiServiceUrl;

   private final RestTemplate restTemplate = new RestTemplate();

   @Autowired
   private OrderDAO orderDAO;

   @Autowired
   private ProductDAO productDAO;

   @Autowired
   private AccountDAO accountDAO;

   @Autowired
   private BCryptPasswordEncoder passwordEncoder;

   @Autowired
   private ProductFormValidator productFormValidator;

   @InitBinder
   public void myInitBinder(WebDataBinder dataBinder) {
      Object target = dataBinder.getTarget();
      if (target == null) {
         return;
      }
      System.out.println("Target=" + target);

      if (target.getClass() == ProductForm.class) {
         dataBinder.setValidator(productFormValidator);
      }
   }

   // GET: Show Login Page
   @RequestMapping(value = { "/admin/login" }, method = RequestMethod.GET)
   public String login(Model model) {
      return "login";
   }

   @RequestMapping(value = { "/admin/accountInfo" }, method = RequestMethod.GET)
   public String accountInfo(Model model) {
      org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      Object principal = auth != null ? auth.getPrincipal() : null;

      String username = "";
      if (principal instanceof UserDetails) {
         username = ((UserDetails) principal).getUsername();
      } else if (principal instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
         org.springframework.security.oauth2.core.user.OAuth2User oauthUser = (org.springframework.security.oauth2.core.user.OAuth2User) principal;
         username = oauthUser.getAttribute("name") != null ? (String) oauthUser.getAttribute("name") : oauthUser.getName();
      } else if (auth != null) {
         username = auth.getName();
      }

      String role = (auth != null) ? auth.getAuthorities().stream()
              .map(org.springframework.security.core.GrantedAuthority::getAuthority)
              .filter(r -> r.equals("ROLE_ADMIN") || r.equals("ROLE_USER"))
              .findFirst().orElse("") : "";

      model.addAttribute("userName", username);
      model.addAttribute("userRole", role);
      model.addAttribute("totalOrders", orderDAO.getTotalOrdersCount(username, role));
      model.addAttribute("totalRevenue", orderDAO.getTotalRevenue(username, role));
      return "accountInfo";
   }

   @RequestMapping(value = { "/403" }, method = RequestMethod.GET)
   public String accessDenied(Model model) {
      return "403";
   }

   @RequestMapping(value = { "/admin/orderList" }, method = RequestMethod.GET)
   public String orderList(Model model,
         @RequestParam(value = "page", defaultValue = "1") String pageStr) {
      int page = 1;
      try {
         page = Integer.parseInt(pageStr);
      } catch (Exception e) {
      }
      final int MAX_RESULT = 5;
      final int MAX_NAVIGATION_PAGE = 10;

      org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      String username = auth.getName();
      String role = auth.getAuthorities().stream()
              .map(org.springframework.security.core.GrantedAuthority::getAuthority)
              .filter(r -> r.equals("ROLE_ADMIN") || r.equals("ROLE_USER"))
              .findFirst().orElse("");

      PaginationResult<OrderInfo> paginationResult
            = orderDAO.listOrderInfo(page, MAX_RESULT, MAX_NAVIGATION_PAGE, username, role);

      model.addAttribute("paginationResult", paginationResult);
      return "orderList";
   }

   // GET: Show product.
   @RequestMapping(value = { "/admin/product" }, method = RequestMethod.GET)
   public String product(Model model, @RequestParam(value = "code", defaultValue = "") String code,
         final RedirectAttributes redirectAttributes) {
      ProductForm productForm = null;

      if (code != null && code.length() > 0) {
         Product product = productDAO.findProduct(code);
         if (product != null) {
            String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
            if (!product.getOwnerUsername().equals(currentUsername)) {
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

   // POST: Save product
   @RequestMapping(value = { "/admin/product" }, method = RequestMethod.POST)
   public String productSave(Model model,
         @ModelAttribute("productForm") @Validated ProductForm productForm,
         BindingResult result,
         final RedirectAttributes redirectAttributes) {

      if (result.hasErrors()) {
         return "product";
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

            if (response.getBody() != null) {
               Map<?, ?> aiResult = response.getBody();
               Boolean approved = (Boolean) aiResult.get("approved");
               String reason = (String) aiResult.get("reason");

               if (approved != null && !approved) {
                  model.addAttribute("aiError", reason);
                  model.addAttribute("aiMetrics", aiResult.get("metrics"));
                  return "product";
               }
            }
         } catch (Exception aiEx) {
            System.err.println("[AI-QA] Cảnh báo: Không thể kết nối AI service: " + aiEx.getMessage());
            model.addAttribute("aiWarning", "AI service tạm thời không khả dụng. Ảnh sẽ được lưu mà không qua kiểm duyệt.");
         }
      }

      try {
         productDAO.save(productForm);
         redirectAttributes.addFlashAttribute("message", "Lưu sản phẩm thành công!");
      } catch (Exception e) {
         Throwable rootCause = ExceptionUtils.getRootCause(e);
         String message = rootCause != null ? rootCause.getMessage() : e.getMessage();
         model.addAttribute("errorMessage", message);
         return "product";
      }

      return "redirect:/productList";
   }

   @RequestMapping(value = { "/admin/order" }, method = RequestMethod.GET)
   public String orderView(Model model, @RequestParam("orderId") String orderId) {
      OrderInfo orderInfo = null;
      if (orderId != null) {
         orderInfo = this.orderDAO.getOrderInfo(orderId);
      }
      if (orderInfo == null) {
         return "redirect:/admin/orderList";
      }

      org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      String username = auth.getName();
      boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
      boolean isUser = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
      
      if (isUser) {
          com.example.demo.entity.Order orderEntity = orderDAO.findOrder(orderId);
          if (orderEntity == null || !username.equals(orderEntity.getCustomerUsername())) {
              return "redirect:/admin/orderList";
          }
      } else if (isAdmin) {
          boolean ownsAny = orderDAO.listOrderDetailInfos(orderId).stream()
                  .anyMatch(d -> {
                      Product p = productDAO.findProduct(d.getProductCode());
                      return p != null && username.equals(p.getOwnerUsername());
                  });
          if (!ownsAny) {
              return "redirect:/admin/orderList";
          }
      }

      List<OrderDetailInfo> details = this.orderDAO.listOrderDetailInfos(orderId);
      orderInfo.setDetails(details);

      model.addAttribute("orderInfo", orderInfo);

      return "order";
   }

   // GET: Delete product
   @RequestMapping(value = { "/admin/deleteProduct" }, method = RequestMethod.GET)
   public String deleteProduct(Model model, @RequestParam(value = "code", defaultValue = "") String code,
         final RedirectAttributes redirectAttributes) {
      if (code != null && code.length() > 0) {
         try {
            Product product = productDAO.findProduct(code);
            if (product != null) {
               String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
               if (!product.getOwnerUsername().equals(currentUsername)) {
                  redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền xóa sản phẩm của người khác!");
                  return "redirect:/productList";
               }
            }
            productDAO.deleteProduct(code);
            redirectAttributes.addFlashAttribute("message", "Xóa sản phẩm thành công!");
         } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sản phẩm: " + e.getMessage());
         }
      }
      return "redirect:/productList";
   }

   // POST: Update order status
   @RequestMapping(value = { "/admin/order/updateStatus" }, method = RequestMethod.POST)
   public String updateOrderStatus(Model model, 
         @RequestParam("orderId") String orderId,
         @RequestParam("status") String status,
         final RedirectAttributes redirectAttributes) {
      org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
      if (!isAdmin) {
         return "redirect:/403";
      }

      if (orderId != null && status != null) {
         try {
            orderDAO.updateOrderStatus(orderId, status);
            redirectAttributes.addFlashAttribute("message", "Cập nhật trạng thái đơn hàng thành công!");
         } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật trạng thái thất bại: " + e.getMessage());
         }
      }
      return "redirect:/admin/order?orderId=" + orderId;
   }

   // GET: Show User Profile (Customer & Admin)
   @RequestMapping(value = { "/admin/user/profile" }, method = RequestMethod.GET)
   public String userProfile(Model model) {
      org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth == null || !auth.isAuthenticated()) {
         return "redirect:/admin/login";
      }

      String username = auth.getName();
      Account account = accountDAO.findAccount(username);
      if (account == null && auth.getPrincipal() instanceof org.springframework.security.oauth2.core.user.OAuth2User) {
         org.springframework.security.oauth2.core.user.OAuth2User oauthUser = (org.springframework.security.oauth2.core.user.OAuth2User) auth.getPrincipal();
         String email = (String) oauthUser.getAttribute("email");
         if (email != null) {
            account = accountDAO.findAccountByEmail(email.toLowerCase());
         }
      }

      if (account == null) {
         return "redirect:/";
      }

      UserProfileForm form = new UserProfileForm();
      form.setUserName(account.getUserName());
      form.setFullName(account.getFullName() != null ? account.getFullName() : account.getUserName());
      form.setEmail(account.getEmail());
      form.setPhoneNumber(account.getPhoneNumber());
      form.setAvatarUrl(account.getAvatarUrl());
      form.setUserRole(account.getUserRole());
      form.setProvider(account.getProvider());
      form.setActive(account.isActive());
      form.setAccountNonLocked(account.isAccountNonLocked());

      model.addAttribute("profileForm", form);
      model.addAttribute("account", account);
      return "userProfile";
   }

   // POST: Save User Profile
   @RequestMapping(value = { "/admin/user/profile" }, method = RequestMethod.POST)
   public String userProfileSave(Model model,
         @ModelAttribute("profileForm") UserProfileForm profileForm,
         final RedirectAttributes redirectAttributes) {

      org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      if (auth == null || !auth.isAuthenticated()) {
         return "redirect:/admin/login";
      }

      Account account = accountDAO.findAccount(profileForm.getUserName());
      if (account == null) {
         redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy thông tin tài khoản!");
         return "redirect:/admin/user/profile";
      }

      account.setFullName(profileForm.getFullName());
      account.setEmail(profileForm.getEmail());
      account.setPhoneNumber(profileForm.getPhoneNumber());
      if (profileForm.getAvatarUrl() != null && !profileForm.getAvatarUrl().trim().isEmpty()) {
         account.setAvatarUrl(profileForm.getAvatarUrl().trim());
      }

      // Xử lý Đổi Mật Khẩu (Chỉ áp dụng cho tài khoản đăng nhập LOCAL)
      if (profileForm.getNewPassword() != null && !profileForm.getNewPassword().trim().isEmpty()) {
         if (!"GOOGLE".equalsIgnoreCase(account.getProvider())) {
            if (profileForm.getOldPassword() == null || !passwordEncoder.matches(profileForm.getOldPassword(), account.getEncrytedPassword())) {
               redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu cũ không chính xác!");
               return "redirect:/admin/user/profile";
            }
            if (!profileForm.getNewPassword().equals(profileForm.getConfirmPassword())) {
               redirectAttributes.addFlashAttribute("errorMessage", "Xác nhận mật khẩu mới không khớp!");
               return "redirect:/admin/user/profile";
            }
            account.setEncrytedPassword(passwordEncoder.encode(profileForm.getNewPassword()));
         }
      }

      accountDAO.saveAccount(account);
      redirectAttributes.addFlashAttribute("message", "Cập nhật hồ sơ cá nhân thành công!");
      return "redirect:/admin/user/profile";
   }

   // GET: Admin List All Users
   @RequestMapping(value = { "/admin/users" }, method = RequestMethod.GET)
   public String userList(Model model,
         @RequestParam(value = "page", defaultValue = "1") String pageStr) {
      org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
      if (!isAdmin) {
         return "redirect:/403";
      }

      int page = 1;
      try {
         page = Integer.parseInt(pageStr);
      } catch (Exception e) {}

      final int MAX_RESULT = 10;
      final int MAX_NAVIGATION_PAGE = 10;
      PaginationResult<Account> paginationResult = accountDAO.listAccounts(page, MAX_RESULT, MAX_NAVIGATION_PAGE);

      model.addAttribute("paginationResult", paginationResult);
      return "userList";
   }

   // GET: Admin Edit User
   @RequestMapping(value = { "/admin/user/edit" }, method = RequestMethod.GET)
   public String userEdit(Model model, @RequestParam("userName") String userName) {
      org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
      if (!isAdmin) {
         return "redirect:/403";
      }

      Account account = accountDAO.findAccount(userName);
      if (account == null) {
         return "redirect:/admin/users";
      }

      UserProfileForm form = new UserProfileForm();
      form.setUserName(account.getUserName());
      form.setFullName(account.getFullName() != null ? account.getFullName() : account.getUserName());
      form.setEmail(account.getEmail());
      form.setPhoneNumber(account.getPhoneNumber());
      form.setAvatarUrl(account.getAvatarUrl());
      form.setUserRole(account.getUserRole());
      form.setProvider(account.getProvider());
      form.setActive(account.isActive());
      form.setAccountNonLocked(account.isAccountNonLocked());

      model.addAttribute("profileForm", form);
      model.addAttribute("account", account);
      return "userEdit";
   }

   // POST: Admin Save User
   @RequestMapping(value = { "/admin/user/edit" }, method = RequestMethod.POST)
   public String userEditSave(Model model,
         @ModelAttribute("profileForm") UserProfileForm profileForm,
         final RedirectAttributes redirectAttributes) {

      org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
      if (!isAdmin) {
         return "redirect:/403";
      }

      Account account = accountDAO.findAccount(profileForm.getUserName());
      if (account == null) {
         redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng!");
         return "redirect:/admin/users";
      }

      account.setFullName(profileForm.getFullName());
      account.setEmail(profileForm.getEmail());
      account.setPhoneNumber(profileForm.getPhoneNumber());
      account.setUserRole(profileForm.getUserRole());
      account.setActive(profileForm.isActive());
      account.setAccountNonLocked(profileForm.isAccountNonLocked());

      accountDAO.saveAccount(account);
      redirectAttributes.addFlashAttribute("message", "Cập nhật người dùng thành công!");
      return "redirect:/admin/users";
   }
}
