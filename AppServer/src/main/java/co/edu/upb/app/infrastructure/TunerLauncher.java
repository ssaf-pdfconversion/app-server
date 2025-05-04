package co.edu.upb.app.infrastructure;
import co.edu.upb.app.application.ConversionManager;
import co.edu.upb.app.application.NoOpMetricsManager;
import co.edu.upb.app.application.WeightTuner;
import co.edu.upb.app.config.Environment;
import co.edu.upb.app.domain.models.OfficeFile;
import co.edu.upb.node.domain.interfaces.infrastructure.InterfaceNode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TunerLauncher {

    public static void main(String[] args) throws Exception {

        String port = Environment.getInstance().getDotenv().get("REGISTRY_PORT");
        String serverIp = Environment.getInstance().getDotenv().get("REGISTRY_IP");

        // 1) Use No-Op stub
        NoOpMetricsManager stubMetrics = new NoOpMetricsManager();

        // 2) Instantiate and bind ConversionManager for RMI subscriptions
        ConversionManager mgr = new ConversionManager(stubMetrics);
        try {
            System.setProperty("java.rmi.server.hostname", serverIp);

            LocateRegistry.createRegistry(Integer.parseInt(port));
            try {
                Naming.rebind("//"+serverIp+":"+port+"/registry", mgr);
            } catch (RemoteException | MalformedURLException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 3) Wait for nodes to subscribe (existing logic)
        Thread.sleep(10000);

        // 4) Grab subscribed nodes via your new accessor
        List<InterfaceNode> nodes = new ArrayList<>(mgr.getSubscribedNodes());

        OfficeFile[] sampleFiles = loadSampleFiles();

        // 6) Run weight tuning
        new WeightTuner(stubMetrics, nodes, sampleFiles).runGridSearch(0.1);
    }

    private static OfficeFile[] loadSampleFiles() throws IOException {
        // put your .docx/.xlsx test files under a directory “samples/”
        Path samplesDir = Paths.get("samples");
        try (Stream<Path> paths = Files.list(samplesDir)) {
            List<OfficeFile> files = paths
                    .filter(p -> !Files.isDirectory(p))
                    .map(p -> {
                        try {
                            byte[] bytes = Files.readAllBytes(p);
                            String b64 = Base64.getEncoder().encodeToString(bytes);
                            return new OfficeFile(b64, p.getFileName().toString());
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .collect(Collectors.toList());
            return files.toArray(new OfficeFile[0]);
        }
    }
}
