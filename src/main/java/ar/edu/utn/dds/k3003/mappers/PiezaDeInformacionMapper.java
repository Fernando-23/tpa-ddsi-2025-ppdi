package ar.edu.utn.dds.k3003.mappers;

import ar.edu.utn.dds.k3003.dtos.PiezaDeInformacionDTO;
import ar.edu.utn.dds.k3003.model.PiezaDeInformacion;

public class PiezaDeInformacionMapper implements Mapper<PiezaDeInformacion,PiezaDeInformacionDTO>{

    public PiezaDeInformacion DTOAElementoPosta(PiezaDeInformacionDTO pdiDTO) {
        return new PiezaDeInformacion(
                pdiDTO.hechoId(),
                pdiDTO.descripcion(),
                pdiDTO.lugar(),
                pdiDTO.momento(),
                pdiDTO.contenido(),
                pdiDTO.url_imagen()
        );
    }

    public PiezaDeInformacionDTO elementoPostaADTO(PiezaDeInformacion pdi_posta){
        return new PiezaDeInformacionDTO(
                pdi_posta.getId() ,
                pdi_posta.getHechoId(),
                pdi_posta.getDescripcion(),
                pdi_posta.getLugar(),
                pdi_posta.getMomento(),
                pdi_posta.getContenido(),
                pdi_posta.getUrl_imagen());
    }
}
