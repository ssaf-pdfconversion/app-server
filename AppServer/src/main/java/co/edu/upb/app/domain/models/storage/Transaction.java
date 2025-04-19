package co.edu.upb.app.domain.models.storage;

public record Transaction(String timestampQuery, Conversion[] conversions) {
}
