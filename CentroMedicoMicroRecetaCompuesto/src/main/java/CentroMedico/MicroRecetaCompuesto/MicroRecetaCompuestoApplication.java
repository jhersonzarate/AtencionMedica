package CentroMedico.MicroRecetaCompuesto;

import java.io.IOException;
import java.time.Duration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class MicroRecetaCompuestoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroRecetaCompuestoApplication.class, args);
	}

	// Llama a los micros por su nombre en Eureka (http://MicroCarritoReceta/...).
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
		f.setConnectTimeout(Duration.ofSeconds(2));
		f.setReadTimeout(Duration.ofSeconds(5));
		RestTemplate rt = new RestTemplate(f);
		// Sin instancias en Eureka el balanceador lanza IllegalStateException;
		// se convierte en error de conexión para responder 503 en vez de 500.
		rt.getInterceptors().add((request, body, execution) -> {
			try {
				return execution.execute(request, body);
			} catch (IllegalStateException ex) {
				throw new IOException(ex.getMessage(), ex);
			}
		});
		return rt;
	}
}