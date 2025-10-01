package ar.edu.utn.dds.k3003.mappers;

import ar.edu.utn.dds.k3003.dtos.PiezaDeInformacionDTO;
import ar.edu.utn.dds.k3003.model.PiezaDeInformacion;

public interface Mapper<ElementoPosta,DTO> {
    ElementoPosta DTOAElementoPosta( DTO dto);
    DTO elementoPostaADTO(ElementoPosta elemento_posta);
}
