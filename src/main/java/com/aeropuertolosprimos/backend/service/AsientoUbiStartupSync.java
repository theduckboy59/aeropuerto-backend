package com.aeropuertolosprimos.backend.service;

import com.aeropuertolosprimos.backend.model.Avion;
import com.aeropuertolosprimos.backend.repository.AsientoUbiRepository;
import com.aeropuertolosprimos.backend.repository.AvionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AsientoUbiStartupSync implements ApplicationRunner {

    private final AvionRepository avionRepository;
    private final AsientoUbiRepository asientoUbiRepository;
    private final AsientoUbiSyncService asientoUbiSyncService;

    /*
     * Este proceso corre automáticamente cuando inicia el backend.
     *
     * Objetivo:
     * Crear asiento_ubi para aviones antiguos que ya existían antes
     * de implementar la generación automática de asientos.
     *
     * Regla:
     * - Si el avión ya tiene asientos, NO se toca.
     * - Si el avión tiene 0 asientos, se sincroniza.
     *
     * Esto evita duplicados y respeta los UNIQUE de asiento_ubi.
     */
    @Override
    public void run(ApplicationArguments args) {

        List<Avion> aviones = avionRepository.findAll();

        if (aviones.isEmpty()) {
            log.info("Sincronización inicial asiento_ubi: no hay aviones registrados.");
            return;
        }

        log.info("Sincronización inicial asiento_ubi: revisando {} aviones.", aviones.size());

        for (Avion avion : aviones) {

            try {

                if (avion.getId() == null) {
                    continue;
                }

                long totalAsientos = asientoUbiRepository.countByAvionId(avion.getId());

                if (totalAsientos > 0) {
                    log.info(
                            "Avión {} ya tiene {} asientos. No se sincroniza.",
                            avion.getId(),
                            totalAsientos
                    );
                    continue;
                }

                log.info(
                        "Avión {} no tiene asientos. Se inicia sincronización automática.",
                        avion.getId()
                );

                asientoUbiSyncService.sincronizarPorAvion(avion.getId());

                long totalGenerados = asientoUbiRepository.countByAvionId(avion.getId());

                log.info(
                        "Avión {} sincronizado correctamente. Asientos generados: {}.",
                        avion.getId(),
                        totalGenerados
                );

            } catch (Exception e) {

                /*
                 * No detenemos el arranque del backend si un avión falla.
                 * Puede fallar por:
                 * - modelo inactivo
                 * - modelo mal configurado
                 * - tipo_asiento faltante
                 * - avión en estado ASIGNADO
                 */
                log.warn(
                        "No se pudo sincronizar asiento_ubi para el avión {}. Motivo: {}",
                        avion.getId(),
                        e.getMessage()
                );
            }
        }

        log.info("Sincronización inicial asiento_ubi finalizada.");
    }
}