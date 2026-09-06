package com.example.demo.dao;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.persistence.LockModeType;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderDetail;
import com.example.demo.entity.Product;

import com.example.demo.model.CartInfo;
import com.example.demo.model.CartLineInfo;
import com.example.demo.model.CustomerInfo;
import com.example.demo.model.OrderDetailInfo;
import com.example.demo.model.OrderInfo;
import com.example.demo.model.OrderStatus;
import com.example.demo.model.ProductInfo;
import com.example.demo.model.VoucherApplyResult;
import com.example.demo.pagination.PaginationResult;

@Transactional
@Repository
public class OrderDAO {
 
    @Autowired
    private SessionFactory sessionFactory;
 
    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private VoucherDAO voucherDAO;
 
    private int getMaxOrderNum() {
        Session session = this.sessionFactory.getCurrentSession();
        Number value = (Number) session.createNativeQuery(
                "SELECT COALESCE(MAX(Order_Num), 0) FROM Orders FOR UPDATE").getSingleResult();
        return value == null ? 0 : value.intValue();
    }
 
    @Transactional(rollbackFor = Exception.class)
    public void saveOrder(CartInfo cartInfo) {
        Session session = this.sessionFactory.getCurrentSession();

        if (cartInfo == null || cartInfo.isEmpty() || !cartInfo.isValidCustomer()) {
            throw new IllegalArgumentException("Giỏ hàng hoặc thông tin người mua không hợp lệ.");
        }

        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String customerUsername = null;
        if (auth != null && auth.isAuthenticated()
                && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            customerUsername = auth.getName();
        }

        List<CartLineInfo> lines = new ArrayList<>(cartInfo.getCartLines());
        lines.sort(Comparator.comparing(line -> line.getProductInfo().getCode()));

        for (CartLineInfo line : lines) {
            validateAndRefreshCartLine(line);
        }

        refreshVoucherDiscount(cartInfo, customerUsername);

        int orderNum = this.getMaxOrderNum() + 1;
        Order order = new Order();

        order.setId(UUID.randomUUID().toString());
        order.setOrderNum(orderNum);
        order.setOrderDate(new Date());
        order.setAmount(cartInfo.getFinalAmount());
        order.setStatus(OrderStatus.PENDING);
        order.setCustomerUsername(customerUsername);

        CustomerInfo customerInfo = cartInfo.getCustomerInfo();
        order.setCustomerName(customerInfo.getName());
        order.setCustomerEmail(customerInfo.getEmail());
        order.setCustomerPhone(customerInfo.getPhone());
        order.setCustomerAddress(customerInfo.getAddress());
 
        session.persist(order);
 
        for (CartLineInfo line : lines) {
            String code = line.getProductInfo().getCode();
            Product product = this.productDAO.findProductForUpdate(code);

            // Deduct inventory & increase sales count
            product.setStockQuantity(product.getStockQuantity() - line.getQuantity());
            product.setSalesCount(product.getSalesCount() + line.getQuantity());
            session.update(product);

            OrderDetail detail = new OrderDetail();
            detail.setId(UUID.randomUUID().toString());
            detail.setOrder(order);
            detail.setAmount(line.getAmount());
            detail.setPrice(line.getProductInfo().getPrice());
            detail.setQuanity(line.getQuantity());
            detail.setProduct(product);

            session.persist(detail);
        }

        if (cartInfo.getVoucherCode() != null && !cartInfo.getVoucherCode().trim().isEmpty()) {
            voucherDAO.recordVoucherUsage(cartInfo.getVoucherCode(), order.getCustomerUsername(), order.getId());
        }
 
        // Order Number!
        cartInfo.setOrderNum(orderNum);
        // Flush
        session.flush();
    }

    private void validateAndRefreshCartLine(CartLineInfo line) {
        if (line == null || line.getProductInfo() == null || line.getProductInfo().getCode() == null
                || line.getQuantity() < 1) {
            throw new IllegalArgumentException("Dòng sản phẩm trong giỏ hàng không hợp lệ.");
        }

        Product product = productDAO.findProductForUpdate(line.getProductInfo().getCode());
        if (product == null || !"ACTIVE".equalsIgnoreCase(product.getStatus())) {
            throw new IllegalStateException("Sản phẩm không còn được bán.");
        }
        if (product.getStockQuantity() < line.getQuantity()) {
            throw new IllegalStateException("Sản phẩm '" + product.getName()
                    + "' không đủ số lượng trong kho! (Chỉ còn " + product.getStockQuantity() + " sản phẩm)");
        }

        ProductInfo productInfo = line.getProductInfo();
        productInfo.setName(product.getName());
        productInfo.setOriginalPrice(product.getPrice());
        productInfo.setDiscountPercent(product.getDiscountPercent());
        productInfo.setPrice(product.getPrice() * (100 - product.getDiscountPercent()) / 100.0);
        productInfo.setStockQuantity(product.getStockQuantity());
    }

    private void refreshVoucherDiscount(CartInfo cartInfo, String customerUsername) {
        String voucherCode = cartInfo.getVoucherCode();
        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            cartInfo.setDiscountAmount(0);
            return;
        }

        VoucherApplyResult result = voucherDAO.validateAndApplyVoucherForCheckout(
                voucherCode, cartInfo.getAmountTotal(), customerUsername);
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());
        }
        cartInfo.setVoucherCode(result.getVoucherCode());
        cartInfo.setDiscountAmount(result.getDiscountAmount());
    }
 
    // @page = 1, 2, ...
    public PaginationResult<OrderInfo> listOrderInfo(int page, int maxResult, int maxNavigationPage, String username, String role) {
        String sql = "Select new " + OrderInfo.class.getName()//
                + "(ord.id, ord.orderDate, ord.orderNum, ord.amount, "
                + " ord.customerName, ord.customerAddress, ord.customerEmail, ord.customerPhone, ord.status) " + " from "
                + Order.class.getName() + " ord ";
        
        boolean isUser = "ROLE_USER".equals(role) || "ROLE_CUSTOMER".equals(role);
        boolean isAdmin = "ROLE_ADMIN".equals(role) || "ROLE_MANAGER".equals(role);
        
        if (isUser && username != null && !username.trim().isEmpty()) {
            sql += " where ord.customerUsername = :username ";
        } else if (isAdmin && username != null && !username.trim().isEmpty()) {
            sql += " where (exists (select 1 from com.example.demo.entity.OrderDetail od where od.order.id = ord.id and od.product.ownerUsername = :username) or ord.customerUsername = :username) ";
        }
        
        sql += " order by ord.orderNum desc";

        Session session = this.sessionFactory.getCurrentSession();
        Query<OrderInfo> query = session.createQuery(sql, OrderInfo.class);
        
        if (sql.contains(":username")) {
            query.setParameter("username", username != null ? username : "");
        }
        return new PaginationResult<OrderInfo>(query, page, maxResult, maxNavigationPage);
    }
 
    public PaginationResult<OrderInfo> listOrderInfo(int page, int maxResult, int maxNavigationPage) {
        return listOrderInfo(page, maxResult, maxNavigationPage, null, null);
    }

    public Order findOrder(String orderId) {
        Session session = this.sessionFactory.getCurrentSession();
        return session.find(Order.class, orderId);
    }

    public Order findOrderForUpdate(String orderId) {
        Session session = this.sessionFactory.getCurrentSession();
        return session.find(Order.class, orderId, LockModeType.PESSIMISTIC_WRITE);
    }

    public boolean canAccessOrder(String orderId, String username, String role) {
        if (orderId == null || username == null || username.trim().isEmpty()) {
            return false;
        }

        Session session = this.sessionFactory.getCurrentSession();
        if ("ROLE_USER".equals(role) || "ROLE_CUSTOMER".equals(role)) {
            String hql = "Select count(o.id) from " + Order.class.getName()
                    + " o Where o.id = :orderId and o.customerUsername = :username";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("orderId", orderId);
            query.setParameter("username", username);
            return query.getSingleResult() > 0;
        }

        if ("ROLE_ADMIN".equals(role) || "ROLE_MANAGER".equals(role)) {
            String hql = "Select count(o.id) from " + Order.class.getName()
                    + " o Where o.id = :orderId and (o.customerUsername = :username or exists ("
                    + "select 1 from " + OrderDetail.class.getName()
                    + " d where d.order.id = o.id and d.product.ownerUsername = :username))";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("orderId", orderId);
            query.setParameter("username", username);
            return query.getSingleResult() > 0;
        }

        return false;
    }

    /**
     * Whole-order mutations are safe only when the seller owns every line.
     * Read access remains broader so customers can still view their orders.
     */
    public boolean canManageOrder(String orderId, String username) {
        if (orderId == null || username == null || username.trim().isEmpty()) {
            return false;
        }
        Session session = sessionFactory.getCurrentSession();
        String totalHql = "Select count(d.id) from " + OrderDetail.class.getName()
                + " d Where d.order.id = :orderId";
        Query<Long> totalQuery = session.createQuery(totalHql, Long.class);
        totalQuery.setParameter("orderId", orderId);
        long totalLines = totalQuery.getSingleResult();
        if (totalLines == 0) {
            return false;
        }

        String ownedHql = totalHql + " and d.product.ownerUsername = :username";
        Query<Long> ownedQuery = session.createQuery(ownedHql, Long.class);
        ownedQuery.setParameter("orderId", orderId);
        ownedQuery.setParameter("username", username);
        return ownedQuery.getSingleResult() == totalLines;
    }

    public boolean isOrderCustomer(String orderId, String username) {
        if (orderId == null || username == null || username.trim().isEmpty()) {
            return false;
        }
        String hql = "Select count(o.id) from " + Order.class.getName()
                + " o Where o.id = :orderId and o.customerUsername = :username";
        Query<Long> query = sessionFactory.getCurrentSession().createQuery(hql, Long.class);
        query.setParameter("orderId", orderId);
        query.setParameter("username", username);
        return query.getSingleResult() > 0;
    }
 
    public OrderInfo getOrderInfo(String orderId) {
        Order order = this.findOrder(orderId);
        if (order == null) {
            return null;
        }
        return new OrderInfo(order.getId(), order.getOrderDate(), //
                order.getOrderNum(), order.getAmount(), order.getCustomerName(), //
                order.getCustomerAddress(), order.getCustomerEmail(), order.getCustomerPhone(), order.getStatus());
    }
 
    public List<OrderDetailInfo> listOrderDetailInfos(String orderId) {
        return listOrderDetailInfos(orderId, null);
    }

    private List<OrderDetailInfo> listOrderDetailInfos(String orderId, String ownerUsername) {
        String sql = "Select new " + OrderDetailInfo.class.getName() //
                + "(d.id, d.product.code, d.product.name , d.quanity,d.price,d.amount) "//
                + " from " + OrderDetail.class.getName() + " d "//
                + " where d.order.id = :orderId ";
        if (ownerUsername != null) {
            sql += " and d.product.ownerUsername = :ownerUsername ";
        }

        Session session = this.sessionFactory.getCurrentSession();
        Query<OrderDetailInfo> query = session.createQuery(sql, OrderDetailInfo.class);
        query.setParameter("orderId", orderId);
        if (ownerUsername != null) {
            query.setParameter("ownerUsername", ownerUsername);
        }

        return query.getResultList();
    }

    public List<OrderDetailInfo> listOrderDetailInfosForPrincipal(
            String orderId, String username, String role) {
        boolean sellerOnly = ("ROLE_ADMIN".equals(role) || "ROLE_MANAGER".equals(role))
                && !isOrderCustomer(orderId, username);
        return listOrderDetailInfos(orderId, sellerOnly ? username : null);
    }
 
    @Transactional(rollbackFor = Exception.class)
    public void updateOrderStatus(String orderId, String status) {
        Order order = this.findOrderForUpdate(orderId);
        if (order != null) {
            String normalizedStatus = OrderStatus.normalize(status);
            if (!OrderStatus.canTransition(order.getStatus(), normalizedStatus)) {
                throw new IllegalStateException("Không thể chuyển trạng thái đơn hàng từ "
                        + order.getStatus() + " sang " + normalizedStatus + ".");
            }
            order.setStatus(normalizedStatus);
            this.sessionFactory.getCurrentSession().flush();
        }
    }
 
    public long getTotalOrdersCount(String username, String role) {
        String sql = "Select count(o.id) from " + Order.class.getName() + " o ";
        
        boolean isUser = "ROLE_USER".equals(role) || "ROLE_CUSTOMER".equals(role);
        boolean isAdmin = "ROLE_ADMIN".equals(role) || "ROLE_MANAGER".equals(role);
        
        if (isUser && username != null && !username.trim().isEmpty()) {
            sql = "Select count(o.id) from " + Order.class.getName() + " o Where o.customerUsername = :username ";
        } else if (isAdmin && username != null && !username.trim().isEmpty()) {
            sql = "Select count(distinct o.id) from " + Order.class.getName() + " o join com.example.demo.entity.OrderDetail od on o.id = od.order.id join com.example.demo.entity.Product p on od.product.code = p.code Where (p.ownerUsername = :username or o.customerUsername = :username) ";
        }
        
        Session session = this.sessionFactory.getCurrentSession();
        Query<Long> query = session.createQuery(sql, Long.class);
        if (sql.contains(":username")) {
            query.setParameter("username", username != null ? username : "");
        }
        Long val = query.getSingleResult();
        return val != null ? val : 0L;
    }

    public long getTotalOrdersCount() {
        return getTotalOrdersCount(null, null);
    }
 
    public double getTotalRevenue(String username, String role) {
        String sql = "Select sum(o.amount) from " + Order.class.getName() + " o ";
        
        boolean isUser = "ROLE_USER".equals(role) || "ROLE_CUSTOMER".equals(role);
        boolean isAdmin = "ROLE_ADMIN".equals(role) || "ROLE_MANAGER".equals(role);
        
        if (isUser && username != null && !username.trim().isEmpty()) {
            sql = "Select sum(o.amount) from " + Order.class.getName() + " o Where o.customerUsername = :username ";
        } else if (isAdmin && username != null && !username.trim().isEmpty()) {
            sql = "Select sum(od.amount) from com.example.demo.entity.OrderDetail od join com.example.demo.entity.Product p on od.product.code = p.code Where (p.ownerUsername = :username or od.order.customerUsername = :username) ";
        }
        
        Session session = this.sessionFactory.getCurrentSession();
        Query<Double> query = session.createQuery(sql, Double.class);
        if (sql.contains(":username")) {
            query.setParameter("username", username != null ? username : "");
        }
        Double val = query.getSingleResult();
        return val != null ? val : 0.0;
    }

    public double getTotalRevenue() {
        return getTotalRevenue(null, null);
    }
}
