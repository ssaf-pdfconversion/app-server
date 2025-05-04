package co.edu.upb.app.application;

import java.io.*;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import co.edu.upb.app.application.ConversionManager;
import co.edu.upb.app.domain.models.OfficeFile;
import co.edu.upb.app.domain.interfaces.application.IMetricsManager;
import co.edu.upb.node.domain.interfaces.infrastructure.InterfaceNode;

public class WeightTuner {
    private final IMetricsManager metricsMgr;
    private final List<InterfaceNode> nodes;
    private final OfficeFile[] sampleFiles;
    private final Path resultsCsv = Paths.get("weight_tuning_results.csv");

    public WeightTuner(IMetricsManager metricsMgr,
                       List<InterfaceNode> nodes,
                       OfficeFile[] sampleFiles) {
        this.metricsMgr = metricsMgr;
        this.nodes      = nodes;
        this.sampleFiles= sampleFiles;
    }

    public void runGridSearch(double step) throws IOException {
        // Prepare CSV
        try (BufferedWriter w = Files.newBufferedWriter(resultsCsv,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            w.write("a,b,c,d,latencyMs\n");
        }

        // Enumerate simplex grid
        for (double a = 0; a <= 1.0; a += step) {
            for (double b = 0; b <= 1.0 - a; b += step) {
                for (double c = 0; c <= 1.0 - a - b; c += step) {
                    double d = 1.0 - a - b - c;
                    testWeights(a, b, c, d);
                }
            }
        }
    }

    public void runRandomSearch(int trials) throws IOException {
        // Prepare CSV
        try (BufferedWriter w = Files.newBufferedWriter(resultsCsv,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            w.write("a,b,c,d,latencyMs\n");
        }

        Random rnd = new Random();
        for (int i = 0; i < trials; i++) {
            // Sample 4 positive values, normalize
            double[] raw = { rnd.nextDouble(), rnd.nextDouble(),
                    rnd.nextDouble(), rnd.nextDouble() };
            double sum = raw[0]+raw[1]+raw[2]+raw[3];
            testWeights(raw[0]/sum, raw[1]/sum, raw[2]/sum, raw[3]/sum);
        }
    }

    private void testWeights(double a, double b, double c, double d) {
        try {
            // 1) Instantiate manager with these weights
            ConversionManager mgr = new ConversionManager(
                    metricsMgr, a, b, c, d
            );
            // 2) Subscribe your nodes
            for (InterfaceNode node : nodes) {
                mgr.subscribeNode(node);
            }
            // 3) Warm‐up (optional)
            mgr.queueOfficeConversion(sampleFiles, 0);

            // 4) Measure a real trial
            Instant start = Instant.now();
            mgr.queueOfficeConversion(sampleFiles, 0);
            long latencyMs = Duration.between(start, Instant.now()).toMillis();

            // 5) Append to CSV
            String line = String.format(
                    Locale.ROOT, "%f,%f,%f,%f,%d%n",
                    a, b, c, d, latencyMs
            );
            Files.write(resultsCsv, line.getBytes(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception ex) {
            System.err.println("Error testing weights: " + ex.getMessage());
        }
    }
}
