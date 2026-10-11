package pe.marcolopez.apps.customerscache.client.dto;

public record ClienteRegistroRequest(
        String nombre,
        String email,
        String telefono
) {
}