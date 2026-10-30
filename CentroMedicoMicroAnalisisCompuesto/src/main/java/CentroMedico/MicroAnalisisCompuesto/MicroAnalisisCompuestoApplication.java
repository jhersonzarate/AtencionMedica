package CentroMedico.MicroAnalisisCompuesto;

import java.io.IOException;
import java.time.Duration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class MicroAnalisisCompuestoApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroAnalisisCompuestoApplication.class, args);
	}

	// Llama a los micros por su nombre en Eureka (http://MicroCarritoAnalisis/...).
	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		SimpleClientHttpRequestFactory f = new SimpleClientHttpRequestFactory();
		f.setConnectTimeout(Duration.ofSeconds(2));
		f.setReadTimeout(Duration.ofSeconds(5));
		RestTemplate rt = new RestTemplate(f);
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