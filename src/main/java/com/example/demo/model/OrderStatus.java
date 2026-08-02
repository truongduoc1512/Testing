package com.example.demo.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class OrderStatus {

    public static final String PENDING = "PENDING";
    public static final String APPROVED = "APPROVED";
    public static final String SHIPPING = "SHIPPING";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";
    public static final String RETURN_PENDING = "RETURN_PENDING";
    public static final String RETURNED = "RETURNED";

    private static final Set<String> ADMIN_STATUSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            PENDING, APPROVED, SHIPPING, COMPLETED, CANCELLED)));

    private OrderStatus() {
    }

    public static String normalize(String status) {
        if (status == null) {
            return null;
        }

        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if ("SHIPPED".equals(normalized)) {
            return SHIPPING;
        }
        if ("DELIVERED".equals(normalized)) {
            return COMPLETED;
        }
        if ("1".equals(normalized)) {
            return PENDING;
        }
        return normalized;
    }

    public static boolean isAdminStatus(String status) {
        return ADMIN_STATUSES.contains(normalize(status));
    }

    public static boolean canTransition(String currentStatus, String nextStatus) {
        String current = normalize(currentStatus);
        String next = normalize(nextStatus);
        if (current == null || next == null || !ADMIN_STATUSES.contains(next)) {
            return false;
        }
        if (current.equals(next)) {
            return true;
        }

        switch (current) {
        case PENDING:
            return APPROVED.equals(next) || SHIPPING.equals(next)
                    || COMPLETED.equals(next) || CANCELLED.equals(next);
        case APPROVED:
            return SHIPPING.equals(next) || COMPLETED.equals(next) || CANCELLED.equals(next);
        case SHIPPING:
            return COMPLETED.equals(next) || CANCELLED.equals(next);
        default:
            return false;
        }
    }
}
