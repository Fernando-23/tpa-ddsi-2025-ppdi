package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.analizadores.GestorAnalizadores;
import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.clients.SolicitudesClient;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.fachadas.FachadaProcesadorPdIPropia;
import ar.edu.utn.dds.k3003.repository.PdiRepository;
import ar.edu.utn.dds.k3003.repository.InMemoryPdiRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestConfig {
    @Bean
    public PdiRepository pdiRepository() {
        return new InMemoryPdiRepository();
    }

    @Bean
    public SolicitudesClient solicitudesClient() {
        // mock para que no llame al servicio real en tests
        SolicitudesClient mock = mock(SolicitudesClient.class);
        when(mock.estaActivo(anyString())).thenReturn(true); // default
        return mock;
    }
    @Bean
    public FachadaProcesadorPdIPropia fachadaProcesadorPdI(PdiRepository repo, SolicitudesClient solicitudesClient, GestorAnalizadores gestor) {
        return new Fachada(repo, solicitudesClient,gestor);
    }
}
