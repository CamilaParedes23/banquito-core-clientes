package com.banquito.core.customer.domain.repository;

import com.banquito.core.customer.domain.model.ClientePersonaNatural;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientePersonaNaturalRepository extends JpaRepository<ClientePersonaNatural, Long> {
}
