package com.banquito.core.customer.application.security;

import com.banquito.core.customer.api.dto.internal.AuthenticatedActor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component("customerAccessPolicy")
public class CustomerAccessPolicy {
    private static final Set<String> BACKOFFICE_ROLES = Set.of("ADMIN_SEGURIDAD", "CAJERO", "OPERADOR_CONTABLE");
    private static final String SWITCH_CLIENT_ID = "switch-pagos-internos-service";
    private static final String SERVICE_ACTOR_TYPE = "SERVICIO";
    private static final String SERVICE_CLIENT_ROLE = "SERVICE_CLIENT";
    private static final String CUSTOMER_READ_SCOPE = "customer.read";

    public boolean canBackoffice(Authentication authentication) {
        AuthenticatedActor actor = actor(authentication);
        return actor != null && actor.roles().stream().anyMatch(BACKOFFICE_ROLES::contains);
    }

    public boolean canBackofficeOrSwitchCustomerRead(Authentication authentication) {
        AuthenticatedActor actor = actor(authentication);
        return canBackoffice(authentication)
                || (actor != null
                && SERVICE_ACTOR_TYPE.equals(actor.actorType())
                && SWITCH_CLIENT_ID.equals(actor.clientId())
                && actor.roles().contains(SERVICE_CLIENT_ROLE)
                && actor.scopes().contains(CUSTOMER_READ_SCOPE));
    }

    public boolean canReadCustomer(Authentication authentication, String customerUuid) {
        AuthenticatedActor actor = actor(authentication);
        if (actor == null || isBlank(customerUuid)) return false;
        return canBackoffice(authentication) || ownsCustomer(actor, customerUuid);
    }

    public boolean canUpdateContact(Authentication authentication, String customerUuid) {
        return canReadCustomer(authentication, customerUuid);
    }

    private boolean ownsCustomer(AuthenticatedActor actor, String customerUuid) {
        return !isBlank(actor.customerUuid()) && actor.customerUuid().trim().equals(customerUuid.trim());
    }

    private AuthenticatedActor actor(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedActor actor)) return null;
        return actor;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
