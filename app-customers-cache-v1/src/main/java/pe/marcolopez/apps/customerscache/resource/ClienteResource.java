package pe.marcolopez.apps.customerscache.resource;

import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.marcolopez.apps.customerscache.service.ClienteService;

@Slf4j
@Path("/clientes")
@Produces(MediaType.APPLICATION_JSON)
@AllArgsConstructor
public class ClienteResource {

    private final ClienteService clienteService;

    @GET
    public Response obtenerPorEmail(@QueryParam("email") String email) {
        if (email == null || email.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("El parámetro 'email' es obligatorio"))
                    .build();
        }

        try {
            return clienteService.obtenerPorEmail(email)
                    .map(cliente -> Response.ok(cliente).build())
                    .orElseGet(() -> Response.noContent().build());
        } catch (PersistenceException exception) {
            log.error("Error consultando cliente por email: {}", email, exception);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("No fue posible consultar el cliente"))
                    .build();
        }
    }

    public record ErrorResponse(String mensaje) {
    }
}