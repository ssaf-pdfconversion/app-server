package co.edu.upb.node.domain.models;

import java.io.Serial;
import java.io.Serializable;

public record Conversion(Integer iterations, String fileContent, Boolean conversionSuccessful) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
