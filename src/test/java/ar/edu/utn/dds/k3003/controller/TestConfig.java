package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.FachadaProcesadorPdI;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.repository.IPdiRepository;
import ar.edu.utn.dds.k3003.repository.InMemoryPdiRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {
    @Bean
    public IPdiRepository pdiRepository() {
        return new InMemoryPdiRepository();
    }

    @Bean
    public FachadaSolicitudes fachadaSolicitudes() {
        return mock(FachadaSolicitudes.class);
    }

    @Bean
    public FachadaProcesadorPdI fachadaProcesadorPdI(IPdiRepository pdiRepository, FachadaSolicitudes fachadaSolicitudes) {
        Fachada fachada = new Fachada(pdiRepository);
        fachada.setFachadaSolicitudes(fachadaSolicitudes);
        return fachada;
    }
}
