package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dao.OrderDAO;
import com.example.demo.model.CartInfo;

@Service
public class OrderCheckoutService {

    @Autowired
    private OrderDAO orderDAO;

    @Transactional(rollbackFor = Exception.class)
    public void checkout(CartInfo cartInfo) {
        orderDAO.saveOrder(cartInfo);
    }
}
