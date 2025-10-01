package ar.edu.utn.dds.k3003.analizadores;

import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;

public interface Analizador {
    String getQueAnalizadorSoy();
    ResultadoAnalisis realizarProcesamiento(String url_imagen);
}
