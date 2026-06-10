package com.banquito.core.customer.api.dto.internal;

import java.util.List;

public record AuthenticatedActor(
        String subject,
        String username,
        String clientId,
        String actorType,
        List<String> roles,
        List<String> scopes
) {}
