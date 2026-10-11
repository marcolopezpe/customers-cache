package pe.marcolopez.apps.customerscache.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import pe.marcolopez.apps.customerscache.client.dto.ClienteRegistroRequest;
import pe.marcolopez.apps.customerscache.client.dto.ClienteRegistroResponse;
import pe.marcolopez.apps.customerscache.client.dto.CustomerValidationResponse;

@Path("/customers")
@RegisterRestClient(configKey = "customer-api")
@Produces(MediaType.APPLICATION_JSON)
public interface CustomerClient {

    @GET
    CustomerValidationResponse validarCliente(@QueryParam("email") String email);

    @POST
    ClienteRegistroResponse registrarCliente(ClienteRegistroRequest request);
}
