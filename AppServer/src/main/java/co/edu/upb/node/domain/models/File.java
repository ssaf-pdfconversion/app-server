package co.edu.upb.node.domain.models;

import java.io.Serial;
import java.io.Serializable;

public record File(String data, Long size, String timestamp, Integer fileTypeId, String originalFileName) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    //Empty factory
    public static File empty() {
        return new File("", 0L, "", 0, "");
    }
}
