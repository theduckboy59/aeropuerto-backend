package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.exception.BusinessException;
import com.aeropuertolosprimos.backend.model.*;
import com.aeropuertolosprimos.backend.repository.*;
import com.aeropuertolosprimos.backend.service.CatalogoEstadoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/catalogos")
public class CatalogoController {

    private static final String ESTADO_TRIPULACION_DISPONIBLE = "DISPONIBLE";

    private final CatalogoEstadoService catalogoEstadoService;

    private final StatusCatalogRepository statusCatalogRepository;

    private final AerolineaRepository aerolineaRepository;
    private final TipoEmpleadoRepository tipoEmpleadoRepository;
    private final TurnoRepository turnoRepository;
    private final NivelAccesoRepository nivelAccesoRepository;
    private final RolRepository rolRepository;
    private final AreaRepository areaRepository;
    private final LicenciaRepository licenciaRepository;
    private final AeropuertoRepository aeropuertoRepository;
    private final TripulacionRepository tripulacionRepository;
    private final DestinoAutorizadoRepository destinoAutorizadoRepository;

    private final EstadoAvionRepository estadoAvionRepository;
    private final AvionRepository avionRepository;

    private final ClaseVueloRepository claseVueloRepository;

    private final TipoAsientoRepository tipoAsientoRepository;
    private final EstadoTripulacionRepository estadoTripulacionRepository;

    private final EstadoVueloRepository estadoVueloRepository;

    private final EstadoAsientoRepository estadoAsientoRepository;

    private final TipoSegmentoVueloRepository tipoSegmentoVueloRepository;

    private final MetodoPagoRepository metodoPagoRepository;

    private final EstadoReservaRepository estadoReservaRepository;
    private final EstadoPagoRepository estadoPagoRepository;
    private final EstadoBoletoRepository estadoBoletoRepository;
    private final EstadoCheckInRepository estadoCheckInRepository;

    private final EstadoAbordajeVueloRepository estadoAbordajeVueloRepository;

    @GetMapping("/status")
    public List<StatusCatalog> listarStatus() {
        return statusCatalogRepository.findAll();
    }

    @GetMapping("/")
    public List<Aerolinea> listarAerolinea() {
        return aerolineaRepository.findAll();
    }

    @GetMapping("/tipo-empleado")
    public List<TipoEmpleado> listarTipoEmpleado() {
        return tipoEmpleadoRepository.findAll();
    }

    @GetMapping("/turno")
    public List<Turno> listarTurno() {
        return turnoRepository.findAll();
    }

    @GetMapping("/nivel-acceso")
    public List<NivelAcceso> listarNivelAcceso() {
        return nivelAccesoRepository.findAll();
    }

    @GetMapping("/rol")
    public List<Rol> listarRol() {
        return rolRepository.findAll();
    }

    @GetMapping("/area")
    public List<Area> listarArea() {
        return areaRepository.findAll();
    }

    @GetMapping("/licencia")
    public List<Licencia> listarLicencia() {
        return licenciaRepository.findAll();
    }

    @GetMapping("/aeropuerto")
    public List<Aeropuerto> listarAeropuertos() {

        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();

        return aeropuertoRepository.findByEstadoId(
                estadoActivoId
        );
    }

    @GetMapping("/tripulacion")
    public List<Tripulacion> listarTripulaciones() {

        EstadoTripulacion estadoDisponible = estadoTripulacionRepository
                .findByNombreIgnoreCase(ESTADO_TRIPULACION_DISPONIBLE)
                .orElseThrow(() ->
                        new BusinessException("Estado de tripulación DISPONIBLE no encontrado")
                );

        return tripulacionRepository.findByEstadoTripulacionId(
                estadoDisponible.getId()
        );
    }

    @GetMapping("/destinos-autorizados")
    public List<DestinoAutorizado> listarDestinosAutorizados() {

        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();

        return destinoAutorizadoRepository.findByEstadoId(
                estadoActivoId
        );
    }

    @GetMapping("/estado-avion")
    public List<EstadoAvion> listarEstadoAvion() {
        return estadoAvionRepository.findAll();
    }

    @GetMapping("/avion")
    public List<Avion> listarAviones() {

        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();

        return avionRepository.findByEstadoId(
                estadoActivoId
        );
    }

    @GetMapping("/clase-vuelo")
    public List<ClaseVuelo> listarClasesVuelo() {
        return claseVueloRepository.findAllByOrderByNombreAsc();
    }

    @GetMapping("/tipo-asiento")
    public List<TipoAsiento> listarTiposAsiento() {
        return tipoAsientoRepository.findAllByOrderByNombreAsc();
    }

    @GetMapping("/estado-tripulacion")
    public List<EstadoTripulacion> listarEstadoTripulacion() {
        return estadoTripulacionRepository.findAll();
    }

    @GetMapping("/estado-vuelo")
    public List<EstadoVuelo> listarEstadoVuelo() {
        return estadoVueloRepository.findAll();
    }

    @GetMapping("/estado-asiento")
    public List<EstadoAsiento> listarEstadoAsiento() {
        return estadoAsientoRepository.findAll();
    }

    @GetMapping("/tipo-segmento-vuelo")
    public List<TipoSegmentoVuelo> listarTipoSegmentoVuelo() {

        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();

        return tipoSegmentoVueloRepository.findByEstadoId(
                estadoActivoId
        );
    }

    @GetMapping("/metodo-pago")
    public List<MetodoPago> listarMetodoPago() {

        Integer estadoActivoId = catalogoEstadoService.obtenerActivoId();

        return metodoPagoRepository.findByEstadoIdOrderByNombreAsc(
                estadoActivoId
        );
    }

    @GetMapping("/estado-reserva")
    public List<EstadoReserva> listarEstadoReserva() {
        return estadoReservaRepository.findAll();
    }

    @GetMapping("/estado-pago")
    public List<EstadoPago> listarEstadoPago() {
        return estadoPagoRepository.findAll();
    }

    @GetMapping("/estado-boleto")
    public List<EstadoBoleto> listarEstadoBoleto() {
        return estadoBoletoRepository.findAll();
    }

    @GetMapping("/estado-checkin")
    public List<EstadoCheckIn> listarEstadoCheckIn() {
        return estadoCheckInRepository.findAll();
    }

    @GetMapping("/estado-abordaje-vuelo")
    public List<EstadoAbordajeVuelo> listarEstadoAbordajeVuelo() {
        return estadoAbordajeVueloRepository.findAll();
    }
}
