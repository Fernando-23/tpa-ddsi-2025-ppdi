package ar.edu.utn.dds.k3003.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OCRDTO {
    private List<OcrParsedResult> ParsedResults;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OcrParsedResult {
        private String ParsedText;
    }
}
