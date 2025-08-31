package ar.edu.utn.dds.k3003.controller;

import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;
import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProcesadorPdiControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FachadaSolicitudes fachadaSolicitudes;

    @Test
    @Order(1)
    public void procesar_nuevoPdi_Ok() throws Exception {
        when(fachadaSolicitudes.estaActivo("hecho1")).thenReturn(true);

        PdIDTO nuevoPdi1 = new PdIDTO("", "hecho1");
        PdIDTO nuevoPdi2 = new PdIDTO("", "hecho1");

        mockMvc.perform(get("/api/pdis/1"))
            .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/pdis/2"))
            .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/pdis")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(nuevoPdi1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("1"));

        mockMvc.perform(post("/api/pdis")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(nuevoPdi2)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("2"));

        mockMvc.perform(get("/api/pdis/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("1"));

        mockMvc.perform(get("/api/pdis/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("2"));

        mockMvc.perform(get("/api/pdis?hecho=hecho1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("1"))
            .andExpect(jsonPath("$[1].id").value("2"));
    }

    @Test
    @Order(2)
    public void procesar_ActualizarEtiquetas_Ok() throws Exception {
        when(fachadaSolicitudes.estaActivo("hecho1")).thenReturn(true);

        PdIDTO nuevoPdi = new PdIDTO("", "hecho1",
                "descripcion", "lugar", null,
                "contenido", List.of("etiqueta1", "etiqueta2"));

        mockMvc.perform(post("/api/pdis")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(nuevoPdi)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("3"))
            .andExpect(jsonPath("$.etiquetas").isArray())
            .andExpect(jsonPath("$.etiquetas[0]").value("etiqueta1"))
            .andExpect(jsonPath("$.etiquetas[1]").value("etiqueta2"));

        mockMvc.perform(get("/api/pdis/3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("3"));

        PdIDTO pdi1Actualizado = new PdIDTO("3", "hecho1",
            "descripcion", "lugar", null,
            "contenido", List.of("etiqueta3", "etiqueta4"));

        mockMvc.perform(post("/api/pdis")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(pdi1Actualizado)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.etiquetas[0]").value("etiqueta3"))
            .andExpect(jsonPath("$.etiquetas[1]").value("etiqueta4"));

        mockMvc.perform(get("/api/pdis/3"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("3"));
    }

    @Test
    @Order(3)
    public void buscarPorHecho_ListPdis_Ok() throws Exception {
        when(fachadaSolicitudes.estaActivo("hecho1")).thenReturn(true);
        when(fachadaSolicitudes.estaActivo("hecho2")).thenReturn(true);

        PdIDTO nuevoPdi = new PdIDTO("", "hecho2");

        mockMvc.perform(post("/api/pdis")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(nuevoPdi)))
            .andExpect(status().isOk());

        mockMvc.perform(post("/api/pdis")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(nuevoPdi)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/pdis?hecho=hecho1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("1"))
            .andExpect(jsonPath("$[1].id").value("2"))
            .andExpect(jsonPath("$[2].id").value("3"));

        mockMvc.perform(get("/api/pdis?hecho=hecho2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("4"))
            .andExpect(jsonPath("$[1].id").value("5"));
    }
}