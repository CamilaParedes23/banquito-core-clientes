package com.banquito.core.customer.domain.repository;

import com.banquito.core.customer.domain.model.ClientePersonaJuridica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientePersonaJuridicaRepository extends JpaRepository<ClientePersonaJuridica, Long> {
}
