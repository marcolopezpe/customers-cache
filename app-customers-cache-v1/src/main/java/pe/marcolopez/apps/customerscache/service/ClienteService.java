package pe.marcolopez.apps.customerscache.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import pe.marcolopez.apps.customerscache.client.CustomerClient;
import pe.marcolopez.apps.customerscache.client.dto.ClienteRegistroRequest;
import pe.marcolopez.apps.customerscache.dto.ClienteDto;
import pe.marcolopez.apps.customerscache.entity.Cliente;
import pe.marcolopez.apps.customerscache.repository.ClienteRepository;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@ApplicationScoped
public class ClienteService {

    private static final String CACHE_KEY_PREFIX = "cliente:email:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    @Inject
    ClienteRepository clienteRepository;
    @Inject
    RedissonClient redissonClient;
    @Inject
    ManagedExecutor managedExecutor;
    @RestClient
    CustomerClient customerClient;

    @Transactional(Transactional.TxType.SUPPORTS)
    public Optional<ClienteDto> obtenerPorEmail(String email) {
        RBucket<ClienteDto> bucket = obtenerBucket(email);

        return obtenerDesdeCache(bucket, email)
                .or(() -> obtenerDesdeBaseDeDatos(bucket, email))
                .map(this::registrarClienteAsync);
    }

    private ClienteDto registrarClienteAsync(ClienteDto cliente) {
        var request = new ClienteRegistroRequest(
                cliente.nombre(),
                cliente.email(),
                cliente.telefono()
        );
        managedExecutor.runAsync(() -> registrarCliente(request));
        log.info("### Registro externo programado para el email={}", request.email());
        return cliente;
    }

    private void registrarCliente(ClienteRegistroRequest request) {
        var email = request.email();
        var inicio = System.currentTimeMillis();

        log.info("### Llamando al API externo para registrar cliente. email={}", email);

        try {
            var response = customerClient.registrarCliente(request);
            var duracion = System.currentTimeMillis() - inicio;
            log.info("### Finalizó el registro en el API externo para email={}, registrado={}, duracionMs={}",
                    response.email(),
                    response.registrado(),
                    duracion);
        } catch (ProcessingException | WebApplicationException exception) {
            var duracion = System.currentTimeMillis() - inicio;
            log.error("### Error al registrar el cliente en el servicio externo. email={}, duracionMs={}",
                    email,
                    duracion,
                    exception);
        }
    }

    private Optional<ClienteDto> obtenerDesdeCache(RBucket<ClienteDto> bucket, String email) {
        try {
            return Optional.ofNullable(bucket.get())
                    .map(cliente -> {
                        log.info("### Cliente encontrado en Redis: {}", email);
                        return cliente;
                    });
        } catch (RedisException exception) {
            log.error("### Error consultando Redis para el email: {}", email, exception);
            return Optional.empty();
        }
    }

    private Optional<ClienteDto> obtenerDesdeBaseDeDatos(RBucket<ClienteDto> bucket, String email) {
        return clienteRepository.findByEmail(email)
                .map(this::toDto)
                .map(cliente -> guardarEnCache(bucket, cliente, email));
    }

    private ClienteDto guardarEnCache(RBucket<ClienteDto> bucket, ClienteDto cliente, String email) {
        try {
            bucket.set(cliente, CACHE_TTL.toSeconds(), TimeUnit.SECONDS);
            log.info("### Cliente guardado en Redis: {}", email);
        } catch (RedisException exception) {
            log.error("### No fue posible guardar el cliente en Redis para el email: {}", email, exception);
        }
        return cliente;
    }

    private RBucket<ClienteDto> obtenerBucket(String email) {
        var cacheKey = CACHE_KEY_PREFIX + email;
        return redissonClient.getBucket(cacheKey);
    }

    private ClienteDto toDto(Cliente cliente) {
        return new ClienteDto(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getEmail(),
                cliente.getTelefono()
        );
    }
}