package ar.edu.utn.dds.k3003.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OCRDTO {

    @JsonProperty("ParsedResults")
    private List<ParsedResult> parsedResults;

    @JsonProperty("OCRExitCode")
    private Integer ocrExitCode;

    @JsonProperty("IsErroredOnProcessing")
    private boolean isErroredOnProcessing;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ParsedResult {
        @JsonProperty("ParsedText")
        private String parsedText;

        // @JsonProperty("TextOrientation") private String textOrientation;
        // @JsonProperty("FileParseExitCode") private Integer fileParseExitCode;
        // @JsonProperty("ErrorMessage") private String errorMessage;
        // @JsonProperty("ErrorDetails") private String errorDetails;
        // @JsonProperty("TextOverlay") private Object textOverlay;
    }
}