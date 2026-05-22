package com.aeropuertolosprimos.backend.controller;

import com.aeropuertolosprimos.backend.model.*;
import com.aeropuertolosprimos.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/catalogos")
public class CatalogoController {

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

    @GetMapping("/status")
    public List<StatusCatalog> listarStatus() {
        return statusCatalogRepository.findAll();
    }

    @GetMapping("/" +
            "")
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

        return aeropuertoRepository.findByEstadoId(1);
    }
    @GetMapping("/tripulacion")
    public List<Tripulacion> listarTripulaciones() {

        return tripulacionRepository
                .findByEstadoTripulacionId(1);
    }

    @GetMapping("/destinos-autorizados")
    public List<DestinoAutorizado> listarDestinosAutorizados() {

        return destinoAutorizadoRepository
                .findByEstadoId(1);
    }

    @GetMapping("/estado-avion")
    public List<EstadoAvion> listarEstadoAvion() {
        return estadoAvionRepository.findAll();
    }

    @GetMapping("/avion")
    public List<Avion> listarAviones() {
        return avionRepository.findByEstadoId(1);
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
        return tipoSegmentoVueloRepository.findByEstadoId(1);
    }
}