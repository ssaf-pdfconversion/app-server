package co.edu.upb.app.domain.models;

import co.edu.upb.node.domain.models.File;
import co.edu.upb.node.domain.models.Iteration;

import java.util.List;

public class FileResult {
    public final AppResponse<File> fileResponse;
    public final List<AppResponse<Iteration>> iterations;
    public FileResult(AppResponse<File> fr, List<AppResponse<Iteration>> its) {
        this.fileResponse = fr;
        this.iterations = its;
    }
}