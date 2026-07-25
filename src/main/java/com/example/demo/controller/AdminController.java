package com.example.demo.controller;

import java.util.List;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

import com.example.demo.dao.OrderDAO;
import com.example.demo.dao.ProductDAO;
import com.example.demo.entity.Product;
import com.example.demo.form.ProductForm;
import com.example.demo.model.OrderDetailInfo;
import com.example.demo.model.OrderInfo;
import com.example.demo.pagination.PaginationResult;
import com.example.demo.validator.ProductFormValidator;

@Controller
@Transactional
public class AdminController {
 
   @Autowired
   private OrderDAO orderDAO;
 
   @Autowired
   private ProductDAO productDAO;
 
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
      UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      String username = userDetails.getUsername();
      String role = userDetails.getAuthorities().stream()
              .map(org.springframework.security.core.GrantedAuthority::getAuthority)
              .filter(r -> r.equals("ROLE_ADMIN") || r.equals("ROLE_USER"))
              .findFirst().orElse("");
 
      model.addAttribute("userDetails", userDetails);
      model.addAttribute("totalOrders", orderDAO.getTotalOrdersCount(username, role));
      model.addAttribute("totalRevenue", orderDAO.getTotalRevenue(username, role));
      return "accountInfo";
   }
 
   @RequestMapping(value = { "/admin/orderList" }, method = RequestMethod.GET)
   public String orderList(Model model, //
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

      PaginationResult<OrderInfo> paginationResult //
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
   public String productSave(Model model, //
         @ModelAttribute("productForm") @Validated ProductForm productForm, //
         BindingResult result, //
         final RedirectAttributes redirectAttributes) {
 
      if (result.hasErrors()) {
         return "product";
      }
      try {
         productDAO.save(productForm);
         redirectAttributes.addFlashAttribute("message", "Lưu sản phẩm thành công!");
      } catch (Exception e) {
         Throwable rootCause = ExceptionUtils.getRootCause(e);
         String message = rootCause.getMessage();
         model.addAttribute("errorMessage", message);
         // Show product form.
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
          // Buyer can only view their own orders
          com.example.demo.entity.Order orderEntity = orderDAO.findOrder(orderId);
          if (orderEntity == null || !username.equals(orderEntity.getCustomerUsername())) {
              return "redirect:/admin/orderList";
          }
      } else if (isAdmin) {
          // Seller should only view if order contains at least one of their products
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
      org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
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
 
}
