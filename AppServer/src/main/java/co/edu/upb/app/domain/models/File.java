package co.edu.upb.app.domain.models;

import java.io.Serial;
import java.io.Serializable;

public record File(String data, Double size, String timestamp, Integer fileTypeId, Integer userId) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
