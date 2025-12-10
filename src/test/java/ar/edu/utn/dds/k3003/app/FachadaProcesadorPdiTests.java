package ar.edu.utn.dds.k3003.app;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import ar.edu.utn.dds.k3003.analizadores.GestorAnalizadores;
import ar.edu.utn.dds.k3003.clients.SolicitudesClient;
import ar.edu.utn.dds.k3003.dtos.PiezaDeInformacionDTO;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.fachadas.FachadaProcesadorPdIPropia;
import ar.edu.utn.dds.k3003.model.PiezaDeInformacion;
import ar.edu.utn.dds.k3003.repository.PdiRepository;
import ar.edu.utn.dds.k3003.repository.InMemoryPdiRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@ExtendWith(MockitoExtension.class)
public class FachadaProcesadorPdiTests {

    FachadaProcesadorPdIPropia target;
    PdiRepository pdiRepository;

    @Mock
    SolicitudesClient solicitudesClient;

    @Mock
    GestorAnalizadores gestor_analisis;

    @BeforeEach
    void setUp() {
        pdiRepository = new InMemoryPdiRepository();
        when(solicitudesClient.estaActivo(anyString())).thenReturn(true);
        target = new Fachada(pdiRepository,solicitudesClient,gestor_analisis);
    }

    @Test
    void testProcesar_ok() throws InterruptedException {
        when(solicitudesClient.estaActivo("hechoId")).thenReturn(true);

        var input = new PiezaDeInformacionDTO(1, "hechoId", "descripcion",
                "", LocalDateTime.now(), "", "");

        var result = target.procesar(input);
        assertEquals(1, result.id());
        assertEquals("hechoId", result.hechoId());
    }

    @Test
    void testProcesar_inactivo() {
        when(solicitudesClient.estaActivo("hechoId")).thenReturn(false);

        var input = new PiezaDeInformacionDTO(1, "hechoId", "descripcion",
                "", LocalDateTime.now(), "", "");

        var exceptionResult = assertThrows(IllegalStateException.class,
                () -> target.procesar(input));

        assertEquals("El hecho no esta activo", exceptionResult.getLocalizedMessage());
    }

    @Test
    void testBuscarPdIPorId_ok(){
        pdiRepository = new InMemoryPdiRepository();
        var testPdi = new PiezaDeInformacion("hechoId", "desc",
                "lugar", LocalDateTime.now(), "cont", "https://www.example.com/imagen.jpg");
        var testPdi2 = new PiezaDeInformacion("hechoId", "desc",
                "lugar", LocalDateTime.now(), "cont", "https://www.example.com/imagen.jpg");
        pdiRepository.save(testPdi);
        pdiRepository.save(testPdi2);

        target = new Fachada(pdiRepository,solicitudesClient, gestor_analisis);

        var result = target.buscarPdIPorId("1");

        assertNotNull(result);
        assertEquals(1, result.id());
        assertEquals("hechoId", result.hechoId());
    }

    @Test
    void testBuscarPdIPorId_noExiste(){
        pdiRepository = new InMemoryPdiRepository();
        var testPdi = new PiezaDeInformacion("hechoId", "desc",
                "lugar", LocalDateTime.now(), "cont",  "https://www.example.com/imagen.jpg");
        var testPdi2 = new PiezaDeInformacion("hechoId", "desc",
                "lugar", LocalDateTime.now(), "cont", "https://www.example.com/imagen.jpg");
        pdiRepository.save(testPdi);
        pdiRepository.save(testPdi2);

        target = new Fachada(pdiRepository,solicitudesClient, gestor_analisis);

        var exceptionResult = assertThrows(NoSuchElementException.class,
                () -> target.buscarPdIPorId("10"));

        assertNotNull(exceptionResult);
    }

    @Test
    void testBuscarPdIPorHechoId_ok(){
        pdiRepository = new InMemoryPdiRepository();
        var testPdi = new PiezaDeInformacion("hechoId", "desc",
                "lugar", LocalDateTime.now(), "cont", "https://www.example.com/imagen.jpg");
        var testPdi2 = new PiezaDeInformacion("hechoId", "desc",
                "lugar", LocalDateTime.now(), "cont", "https://www.example.com/imagen.jpg");
        var testPdi3 = new PiezaDeInformacion("hechoIdX", "desc",
                "lugar", LocalDateTime.now(), "cont", "https://www.example.com/imagen.jpg");
        pdiRepository.save(testPdi);
        pdiRepository.save(testPdi2);
        pdiRepository.save(testPdi3);

        target = new Fachada(pdiRepository,solicitudesClient,gestor_analisis);

        var result = target.buscarPorHecho("hechoId");

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals(1, result.get(0).id());
        assertEquals("hechoId", result.get(0).hechoId());

        assertEquals(1, result.get(1).id());
        assertEquals("hechoId", result.get(1).hechoId());

    }

    @Test
    void testBuscarPdIPorHechoId_noExiste(){
        String hechoId = "1234";

        pdiRepository = new InMemoryPdiRepository();
        var testPdi = new PiezaDeInformacion("hechoId", "desc",
                "lugar", LocalDateTime.now(), "cont", "https://www.example.com/imagen.jpg");
        var testPdi2 = new PiezaDeInformacion("hechoId", "desc",
                "lugar", LocalDateTime.now(), "cont", "https://www.example.com/imagen.jpg");
        var testPdi3 = new PiezaDeInformacion("hechoIdX", "desc",
                "lugar", LocalDateTime.now(), "cont", "https://www.example.com/imagen.jpg");
        pdiRepository.save(testPdi);
        pdiRepository.save(testPdi2);
        pdiRepository.save(testPdi3);

        target = new Fachada(pdiRepository,solicitudesClient,gestor_analisis);
        var exceptionResult = assertThrows(NoSuchElementException.class,
                () -> target.buscarPorHecho(hechoId));

        assertEquals("No se encontro PDI linkeado al hecho 1234", exceptionResult.getLocalizedMessage());
    }
}
