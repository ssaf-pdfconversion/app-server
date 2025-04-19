package co.edu.upb.app.domain.models.storage;

public record TransactionIteration(String initialTimestamp, Boolean conversionSuccess, String message, String returnTimestamp, String nodeId) {
}
