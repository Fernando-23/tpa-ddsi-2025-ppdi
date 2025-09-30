package ar.edu.utn.dds.k3003.fachadas;

import java.util.List;
import java.util.NoSuchElementException;

import ar.edu.utn.dds.k3003.dtos.PiezaDeInformacionDTO;
import ar.edu.utn.dds.k3003.facades.FachadaSolicitudes;

public interface FachadaProcesadorPdIPropia {
    PiezaDeInformacionDTO procesar(PiezaDeInformacionDTO pdiDto) throws IllegalStateException;
    PiezaDeInformacionDTO buscarPdIPorId(String pdiId) throws NoSuchElementException;
    List<PiezaDeInformacionDTO> buscarPorHecho(String hechoId) throws NoSuchElementException;
    void setFachadaSolicitudes(FachadaSolicitudes fachadaSolicitudes);
}
