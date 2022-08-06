package dilian.li.loloracleserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class LolOracleServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LolOracleServerApplication.class, args);
	}
	@Bean
	public WebClient.Builder getWebClientBuilder(){
		return WebClient.builder();
	}

	@Bean
	public WebMvcConfigurer corsConfig(){
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry){
				registry.addMapping("/**").allowedMethods("GET", "POST", "PUT", "DELETE")
						.allowedHeaders("*")
						.allowedOrigins("http://localhost:3000");
			}
		};
	}
}
