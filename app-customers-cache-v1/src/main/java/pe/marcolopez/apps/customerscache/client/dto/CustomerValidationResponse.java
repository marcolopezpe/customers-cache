package pe.marcolopez.apps.customerscache.client.dto;

public record CustomerValidationResponse(
        String email,
        boolean estado
) {
}