package ar.edu.utn.dds.k3003.model.mappers;

import ar.edu.utn.dds.k3003.facades.dtos.PdIDTO;
import ar.edu.utn.dds.k3003.model.PiezaDeInformacion;

import java.util.List;

public class PiezaDeInformacionMapper {

    public static List<PdIDTO> toDto(List<PiezaDeInformacion> pdis) {
        return pdis.stream().map(pdi -> toDto(pdi, List.of())).toList();
    }

    public static PdIDTO toDto(PiezaDeInformacion pdi) {
        return toDto(pdi, List.of());
    }

    public static PdIDTO toDto(PiezaDeInformacion pdi, List<String> etiquetas) {
        var result = new PdIDTO(
            String.valueOf(pdi.getId()),
            pdi.getHechoId(),
            pdi.getDescripcion(),
            pdi.getLugar(),
            pdi.getMomento(),
            pdi.getContenido(),
            etiquetas
        );
        return result;
    }
}
