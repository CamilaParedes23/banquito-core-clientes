package com.banquito.core.customer.infrastructure.config;

import com.banquito.core.customer.domain.enums.*;
import com.banquito.core.customer.domain.model.*;
import com.banquito.core.customer.domain.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private final ClienteRepository clienteRepository;
    private final SubtipoClienteRepository subtipoClienteRepository;
    private final ClientePersonaNaturalRepository naturalRepository;
    private final ClientePersonaJuridicaRepository juridicaRepository;
    private final ClienteRelacionRepository relacionRepository;

    @Value("${banquito.demo.enabled:true}")
    private boolean demoEnabled;

    @Override
    @Transactional
    public void run(String... args) {
        if (!demoEnabled) return;
        Cliente representante = crearNaturalSiNoExiste("CEDULA", "1717171717", "NAT_ALTO_VALOR", "Ana María", "Torres Silva", "ana.torres@banquito.com");
        crearNaturalSiNoExiste("CEDULA", "1722222222", "NAT_MASIVO", "Carlos Eduardo", "Pérez Mora", "carlos.perez@banquito.com");
        Cliente empresa = crearJuridicoSiNoExiste("1799999999001", "JUR_CORPORATIVO", "Comercial Demo BanQuito S.A.", representante);
        boolean existeRelacion = relacionRepository.findByUuidClienteOrigenAndTipoRelacionAndEstadoOrderByFechaInicioDesc(
                empresa.getUuidCliente(), TipoRelacionClienteEnum.REPRESENTANTE_LEGAL, EstadoRelacionClienteEnum.ACTIVA).stream()
                .anyMatch(r -> r.getUuidClienteRelacionado().equals(representante.getUuidCliente()));
        if (!existeRelacion) {
            relacionRepository.save(ClienteRelacion.crear(
                    empresa.getUuidCliente(), representante.getUuidCliente(), TipoRelacionClienteEnum.REPRESENTANTE_LEGAL,
                    representante.getIdentificacion(), "Ana María Torres Silva", LocalDate.now().minusYears(1), "Representante legal demo"));
        }
    }

    private Cliente crearNaturalSiNoExiste(String tipoIdentificacion, String identificacion, String subtipoCodigo,
                                           String nombres, String apellidos, String email) {
        return clienteRepository.findByIdentificacion(identificacion).orElseGet(() -> {
            SubtipoCliente subtipo = subtipoClienteRepository.findByCodigoAndTipoClienteAndEstado(
                    subtipoCodigo, TipoClienteEnum.NATURAL, EstadoSubtipoClienteEnum.ACTIVO).orElseThrow();
            Cliente cliente = Cliente.crear(subtipo, TipoClienteEnum.NATURAL, TipoIdentificacionEnum.valueOf(tipoIdentificacion),
                    identificacion, email, "0999999999", "Quito - Ecuador", null, false);
            Cliente saved = clienteRepository.saveAndFlush(cliente);
            naturalRepository.save(ClientePersonaNatural.crear(saved, nombres, apellidos, LocalDate.of(1990, 1, 15), GeneroEnum.NO_DECLARA, "ECUATORIANA"));
            return saved;
        });
    }

    private Cliente crearJuridicoSiNoExiste(String ruc, String subtipoCodigo, String razonSocial, Cliente representante) {
        return clienteRepository.findByIdentificacion(ruc).orElseGet(() -> {
            SubtipoCliente subtipo = subtipoClienteRepository.findByCodigoAndTipoClienteAndEstado(
                    subtipoCodigo, TipoClienteEnum.JURIDICO, EstadoSubtipoClienteEnum.ACTIVO).orElseThrow();
            Cliente cliente = Cliente.crear(subtipo, TipoClienteEnum.JURIDICO, TipoIdentificacionEnum.RUC,
                    ruc, "pagos@demo-banquito.com", "022999999", "Quito - Matriz Demo", null, true);
            Cliente saved = clienteRepository.saveAndFlush(cliente);
            juridicaRepository.save(ClientePersonaJuridica.crear(saved, razonSocial, "Comercial Demo",
                    LocalDate.of(2015, 5, 20), "Servicios empresariales", representante.getUuidCliente(), representante.getIdentificacion()));
            return saved;
        });
    }
}
