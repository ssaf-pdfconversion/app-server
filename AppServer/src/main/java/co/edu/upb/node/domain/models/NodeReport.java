package co.edu.upb.node.domain.models;

import java.io.Serial;
import java.io.Serializable;

public record NodeReport(Integer activeTasks, Double cpuUsage, Integer queueLength, String nodeId) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}

