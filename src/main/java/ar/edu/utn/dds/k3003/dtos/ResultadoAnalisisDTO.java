package ar.edu.utn.dds.k3003.dtos;

public record ResultadoAnalisisDTO(String que_analizador_es, String resultado) {
    public ResultadoAnalisisDTO(String que_analizador_es,String resultado){
        this.que_analizador_es = que_analizador_es;
        this.resultado = resultado;
    }
}
