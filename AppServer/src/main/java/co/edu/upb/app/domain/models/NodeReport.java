package co.edu.upb.app.domain.models;

import java.io.Serial;
import java.io.Serializable;

public record NodeReport(Integer activeTasks, Double cpuUsage, Integer queueLength) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}

