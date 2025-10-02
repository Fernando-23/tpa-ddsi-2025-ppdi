package ar.edu.utn.dds.k3003.mappers;

import ar.edu.utn.dds.k3003.dtos.ResultadoAnalisisDTO;
import ar.edu.utn.dds.k3003.model.ResultadoAnalisis;

import java.util.ArrayList;
import java.util.List;

public class ResultadoAnalisisMapper implements Mapper<ResultadoAnalisis, ResultadoAnalisisDTO>{

    public ResultadoAnalisisDTO elementoPostaADTO(ResultadoAnalisis resultado){
        return new ResultadoAnalisisDTO(
                resultado.getTipo_analizador(),
                resultado.getResultado_procesamiento());
    }

    //de momeeento no lo uso (y no tendria sentido que lo use)
    public ResultadoAnalisis DTOAElementoPosta(ResultadoAnalisisDTO resultado_dto){
        return null;
    }

    public List<ResultadoAnalisisDTO> resultadosPostaAResultadosDTO(List<ResultadoAnalisis> resultados_a_mapear){
        List<ResultadoAnalisisDTO> resultados_a_devolver = new ArrayList<>();
        for (ResultadoAnalisis resultado : resultados_a_mapear){
            var resultado_mapeado = this.elementoPostaADTO(resultado);
            resultados_a_devolver.add(resultado_mapeado);
        }

        return resultados_a_devolver;
    }

}
