package com.example.demo.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderDetail;
import com.example.demo.entity.OrderReturn;
import com.example.demo.entity.Product;
import com.example.demo.form.OrderReturnForm;
import com.example.demo.model.OrderStatus;

@Repository
@Transactional
public class OrderReturnDAO {

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private OrderDAO orderDAO;

    @Autowired
    private ProductDAO productDAO;

    public OrderReturn findReturnByOrderId(String orderId) {
        if (orderId == null) return null;
        Session session = this.sessionFactory.getCurrentSession();
        String hql = "Select r from " + OrderReturn.class.getName() + " r Where r.orderId = :orderId";
        Query<OrderReturn> query = session.createQuery(hql, OrderReturn.class);
        query.setParameter("orderId", orderId);
        List<OrderReturn> list = query.getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean cancelOrder(String username, String orderId) {
        Order order = orderDAO.findOrderForUpdate(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId);
        }

        // Ownership check
        if (username == null || !username.equals(order.getCustomerUsername())) {
            throw new IllegalStateException("Bạn không có quyền hủy đơn hàng này!");
        }

        // Rule: Only PENDING orders can be cancelled
        if (!OrderStatus.PENDING.equals(OrderStatus.normalize(order.getStatus()))) {
            throw new IllegalStateException("Chỉ có thể hủy đơn hàng ở trạng thái 'Đang chờ xử lý' (PENDING)!");
        }

        Session session = this.sessionFactory.getCurrentSession();

        // Restore inventory & adjust sales count inside transaction
        List<OrderDetail> details = getOrderDetails(orderId);
        for (OrderDetail detail : details) {
            Product product = productDAO.findProductForUpdate(detail.getProduct().getCode());
            if (product != null) {
                int restoredStock = product.getStockQuantity() + detail.getQuanity();
                int restoredSales = Math.max(0, product.getSalesCount() - detail.getQuanity());
                product.setStockQuantity(restoredStock);
                product.setSalesCount(restoredSales);
                session.update(product);
            }
        }

        // Update order status to CANCELLED
        order.setStatus(OrderStatus.CANCELLED);
        session.update(order);
        session.flush();

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderReturn createReturnRequest(String username, String orderId, OrderReturnForm form) {
        if (form == null || form.getReason() == null || form.getReason().trim().isEmpty()
                || form.getReason().trim().length() > 2000
                || (form.getImageUrls() != null && form.getImageUrls().trim().length() > 500)) {
            throw new IllegalArgumentException("Dữ liệu yêu cầu trả hàng không hợp lệ.");
        }
        Order order = orderDAO.findOrderForUpdate(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId);
        }

        // Ownership check
        if (username == null || !username.equals(order.getCustomerUsername())) {
            throw new IllegalStateException("Bạn không có quyền yêu cầu trả hàng cho đơn này!");
        }

        if (!OrderStatus.COMPLETED.equals(OrderStatus.normalize(order.getStatus()))) {
            throw new IllegalStateException("Chỉ có thể yêu cầu trả hàng với đơn đã hoàn thành!");
        }

        // Prevent duplicate return requests
        OrderReturn existing = findReturnByOrderId(orderId);
        if (existing != null) {
            throw new IllegalStateException("Đơn hàng này đã có yêu cầu trả hàng đang được xử lý!");
        }

        Session session = this.sessionFactory.getCurrentSession();

        OrderReturn orderReturn = new OrderReturn();
        orderReturn.setOrderId(orderId);
        orderReturn.setUsername(username);
        orderReturn.setReason(form.getReason().trim());
        orderReturn.setImageUrls(form.getImageUrls() == null ? null : form.getImageUrls().trim());
        orderReturn.setStatus("PENDING");
        orderReturn.setCreatedAt(new Date());
        orderReturn.setUpdatedAt(new Date());

        session.save(orderReturn);

        // Update Order status tag
        order.setStatus(OrderStatus.RETURN_PENDING);
        session.update(order);
        session.flush();

        return orderReturn;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderReturn updateReturnStatus(String adminUsername, String orderId, String action, String adminNote) {
        if (action == null || (!"APPROVE".equalsIgnoreCase(action) && !"REJECT".equalsIgnoreCase(action))
                || (adminNote != null && adminNote.trim().length() > 255)) {
            throw new IllegalArgumentException("Dữ liệu xử lý yêu cầu trả hàng không hợp lệ.");
        }
        Order order = orderDAO.findOrderForUpdate(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId);
        }
        if (!orderDAO.canManageOrder(orderId, adminUsername)) {
            throw new IllegalStateException("Bạn không có quyền xử lý yêu cầu trả hàng của đơn này!");
        }

        OrderReturn orderReturn = findReturnByOrderIdForUpdate(orderId);
        if (orderReturn == null) {
            throw new IllegalArgumentException("Không tìm thấy yêu cầu trả hàng cho đơn #" + orderId);
        }

        if (!"PENDING".equalsIgnoreCase(orderReturn.getStatus())) {
            throw new IllegalStateException("Yêu cầu trả hàng này đã được xử lý!");
        }
        if (!OrderStatus.RETURN_PENDING.equals(OrderStatus.normalize(order.getStatus()))) {
            throw new IllegalStateException("Đơn hàng không ở trạng thái chờ xử lý trả hàng!");
        }

        Session session = this.sessionFactory.getCurrentSession();

        if ("APPROVE".equalsIgnoreCase(action)) {
            orderReturn.setStatus("APPROVED");
            orderReturn.setAdminNote(adminNote == null ? null : adminNote.trim());
            orderReturn.setUpdatedAt(new Date());
            session.update(orderReturn);

            order.setStatus(OrderStatus.RETURNED);
            session.update(order);

            // Restore stock quantity upon approved return
            List<OrderDetail> details = getOrderDetails(orderId);
            for (OrderDetail detail : details) {
                Product product = productDAO.findProductForUpdate(detail.getProduct().getCode());
                if (product != null) {
                    product.setStockQuantity(product.getStockQuantity() + detail.getQuanity());
                    product.setSalesCount(Math.max(0, product.getSalesCount() - detail.getQuanity()));
                    session.update(product);
                }
            }
        } else if ("REJECT".equalsIgnoreCase(action)) {
            orderReturn.setStatus("REJECTED");
            orderReturn.setAdminNote(adminNote == null ? null : adminNote.trim());
            orderReturn.setUpdatedAt(new Date());
            session.update(orderReturn);

            order.setStatus(OrderStatus.COMPLETED);
            session.update(order);
        } else {
            throw new IllegalArgumentException("Hành động không hợp lệ! (Chỉ chấp nhận 'APPROVE' hoặc 'REJECT')");
        }

        session.flush();
        return orderReturn;
    }

    private List<OrderDetail> getOrderDetails(String orderId) {
        Session session = this.sessionFactory.getCurrentSession();
        String hql = "Select d from " + OrderDetail.class.getName()
                + " d Where d.order.id = :orderId Order by d.product.code";
        Query<OrderDetail> query = session.createQuery(hql, OrderDetail.class);
        query.setParameter("orderId", orderId);
        return query.getResultList();
    }

    private OrderReturn findReturnByOrderIdForUpdate(String orderId) {
        if (orderId == null) {
            return null;
        }
        Session session = this.sessionFactory.getCurrentSession();
        String hql = "Select r from " + OrderReturn.class.getName() + " r Where r.orderId = :orderId";
        Query<OrderReturn> query = session.createQuery(hql, OrderReturn.class);
        query.setParameter("orderId", orderId);
        query.setLockMode(javax.persistence.LockModeType.PESSIMISTIC_WRITE);
        List<OrderReturn> returns = query.getResultList();
        return returns.isEmpty() ? null : returns.get(0);
    }
}
