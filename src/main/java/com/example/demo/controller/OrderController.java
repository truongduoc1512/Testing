package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.dao.OrderDAO;
import com.example.demo.model.OrderDetailInfo;
import com.example.demo.model.OrderInfo;
import com.example.demo.model.OrderStatus;
import com.example.demo.pagination.PaginationResult;
import com.example.demo.utils.PageNumberParser;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Order Controller", description = "Các API quản lý, danh sách và chi tiết đơn hàng")
@Controller
public class OrderController {

   private static final Logger LOGGER = LoggerFactory.getLogger(OrderController.class);

   @Autowired
   private OrderDAO orderDAO;

   private String currentRole(org.springframework.security.core.Authentication auth) {
      return auth.getAuthorities().stream()
            .map(org.springframework.security.core.GrantedAuthority::getAuthority)
            .filter(role -> "ROLE_ADMIN".equals(role) || "ROLE_USER".equals(role))
            .findFirst().orElse("");
   }

   // GET: Admin/Seller Order List
   @RequestMapping(value = { "/admin/orderList" }, method = RequestMethod.GET)
   public String orderList(Model model,
         @RequestParam(value = "page", defaultValue = "1") String pageStr) {
      int page = PageNumberParser.parsePositivePage(pageStr);
      final int MAX_RESULT = 5;
      final int MAX_NAVIGATION_PAGE = 10;

      org.springframework.security.core.Authentication auth = SecurityContextHolder.getContext().getAuthentication();
      String username = auth.getName();
      String role = currentRole(auth);

      PaginationResult<OrderInfo> paginationResult
            = orderDAO.listOrderInfo(page, MAX_RESULT, MAX_NAVIGATION_PAGE, username, role);

      model.addAttribute("paginationResult", paginationResult);
      return "orderList";
   }

   // GET: View Order Detail
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
      if (!orderDAO.canAccessOrder(orderId, auth.getName(), currentRole(auth))) {
         return "redirect:/admin/orderList";
      }

      String role = currentRole(auth);
      List<OrderDetailInfo> details = this.orderDAO.listOrderDetailInfosForPrincipal(
            orderId, auth.getName(), role);
      orderInfo.setDetails(details);
      if ("ROLE_ADMIN".equals(role) && !orderDAO.isOrderCustomer(orderId, auth.getName())) {
         orderInfo.setAmount(details.stream().mapToDouble(OrderDetailInfo::getAmount).sum());
      }

      model.addAttribute("orderInfo", orderInfo);

      return "order";
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

      if (!orderDAO.canManageOrder(orderId, auth.getName())) {
         return "redirect:/403";
      }

      if (orderId != null && status != null) {
         String normalizedStatus = OrderStatus.normalize(status);
         if (!OrderStatus.isAdminStatus(normalizedStatus)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái đơn hàng không hợp lệ!");
            return "redirect:/admin/order?orderId=" + orderId;
         }
         try {
            orderDAO.updateOrderStatus(orderId, normalizedStatus);
            redirectAttributes.addFlashAttribute("message", "Cập nhật trạng thái đơn hàng thành công!");
         } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
         } catch (Exception e) {
            LOGGER.error("Không thể cập nhật trạng thái đơn hàng {}", orderId, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Cập nhật trạng thái đơn hàng thất bại.");
         }
      }
      return "redirect:/admin/order?orderId=" + orderId;
   }
}
