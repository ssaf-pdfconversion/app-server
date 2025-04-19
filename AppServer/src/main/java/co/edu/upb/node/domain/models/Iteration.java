package co.edu.upb.node.domain.models;

import java.io.Serial;
import java.io.Serializable;

//nodeID is BIOS UUID
public record Iteration(String startTimestamp, String endTimestamp, String nodeId) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
