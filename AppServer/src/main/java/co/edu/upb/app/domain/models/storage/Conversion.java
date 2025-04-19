package co.edu.upb.app.domain.models.storage;

import java.util.List;

public record Conversion(Integer userId, Long size, Integer fileTypeId, String conversionTimestamp, Boolean conversionStatus, TransactionIteration[] iterations){
}
