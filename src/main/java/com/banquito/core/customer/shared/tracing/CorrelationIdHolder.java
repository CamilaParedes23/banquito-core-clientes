package com.banquito.core.customer.shared.tracing;

import java.util.UUID;

public final class CorrelationIdHolder {

    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    private CorrelationIdHolder() {}

    public static String get() {
        String value = HOLDER.get();
        if (value == null || value.isBlank()) {
            value = UUID.randomUUID().toString();
            HOLDER.set(value);
        }
        return value;
    }

    public static void set(String correlationId) {
        HOLDER.set(correlationId);
    }

    public static void clear() {
        HOLDER.remove();
    }
}
