import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;


import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.annotation.JsonView;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
/*import rx.Observable;*/

/*import org.springframework.web.reactive.function.client.ClientResponse;*/
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;

/*
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
*/

@RestController
@EnableAutoConfiguration
public class Netty_Example {
	private WebClient client = WebClient.create("http://localhost:8081");
	private Mono<ClientResponse> webrequest = client.get()
			.uri("/echo")
			.accept(MediaType.ALL)
			.exchange();
	

/*
	
	@Autowired ReactiveRedisOperations<String, String> operations;
	
	@RequestMapping("/redis")
	public Mono<Boolean> get_redis() throws Exception{
		ReactiveValueOperations<String, String> valueOperations = operations.opsForValue();

		Mono<Boolean> cachedMono = valueOperations.set("test","abc");
		
		Mono<String> cachedMono = valueOperations.get(cacheKey) //
				.switchIfEmpty(cacheValue().flatMap(it -> {

					return valueOperations.set(cacheKey, it, Duration.ofSeconds(60)).then(Mono.just(it));
				}));
		
		return cachedMono;
	}*/
	
	@RequestMapping("/")
	String home1() {
		return "Hello... World......";
	}
	
	@RequestMapping("/webrequest")
	public Mono<String> getNewResult() {
		return webrequest.flatMap(res -> res.bodyToMono(String.class));
	}

	@RequestMapping("/echo")
	public Mono<Long> echo() {
		return Mono.delay(Duration.ofMillis(50));
	}
	
	@RequestMapping("/sleep")
	public Mono<Long> getNewResult1() {
		return Mono.delay(Duration.ofMillis(100));
	}


	public static void main(String[] args) {
		System.out.println("Starting ...");
		SpringApplication.run(Netty_Example.class, args);
	
	}

}