package ar.edu.utn.dds.k3003.analizadores;

import ar.edu.utn.dds.k3003.model.PiezaDeInformacion;
import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GestorAnalizadores {
    private final List<Analizador> analizadores;

    private static final Logger logger_gestor_analisis = LoggerFactory.getLogger(GestorAnalizadores.class);

    @Autowired
    public GestorAnalizadores(List<Analizador> analizadores) {
        this.analizadores = analizadores;
    }
    //TODO ocr le pegamos pero no devuelve nada (no procesa), mirar eso
    public void realizarAnalisis(PiezaDeInformacion pdi){
        for (Analizador analizador : analizadores){
            ResultadoAnalisis resultado = analizador.realizarProcesamiento(pdi.getUrl_imagen());

            if (!resultado.getEtiquetas_procesadas().equals("[]")){
                logger_gestor_analisis.
                        info("(realizarAnalisis) - Resultado valido obtenido, agregando resultado de analizador {} a la pieza de info.",resultado.getTipo_analizador());
                pdi.agregarResultado(resultado);
                continue;
            }
            resultado.setTipo_analizador(analizador.getQueAnalizadorSoy());
            resultado.setEtiquetas_procesadas("Sin resultados.");
            logger_gestor_analisis.warn("(realizarAnalisis) - El analizador {} no proceso ningun resultado en base a la imagen recibida, se procede a dejar el mensaje -Sin resultados.-"
                    ,resultado.getTipo_analizador());
        }

        logger_gestor_analisis.info("(realizarAnalisis) - Analisis completado.");
    }
}
