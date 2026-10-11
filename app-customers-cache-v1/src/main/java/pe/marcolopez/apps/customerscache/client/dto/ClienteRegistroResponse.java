package pe.marcolopez.apps.customerscache.client.dto;

public record ClienteRegistroResponse(
        String nombre,
        String email,
        String telefono,
        boolean registrado
) {
}
