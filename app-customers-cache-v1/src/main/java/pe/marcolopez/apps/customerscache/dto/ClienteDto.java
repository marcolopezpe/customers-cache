package pe.marcolopez.apps.customerscache.dto;

import java.util.UUID;

public record ClienteDto(
        UUID id,
        String nombre,
        String email,
        String telefono
) {
}