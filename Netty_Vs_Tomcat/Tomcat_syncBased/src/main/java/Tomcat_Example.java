import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@RestController
@EnableAutoConfiguration
public class Tomcat_Example {
	private WebClient client = WebClient.create("http://localhost:8081");
	private Mono<ClientResponse> result = client.get()
			.uri("/echo")
			.accept(MediaType.ALL)
			.exchange();
	
	@RequestMapping("/webrequest")
	public String getNewResult() {
		return ">> result = " + result.flatMap(res -> res.bodyToMono(String.class)).block();
	}
	
	
	@Autowired
	private StringRedisTemplate template;
	@RequestMapping("/redis")
	String get_redis() throws Exception{
		ValueOperations<String, String> ops = this.template.opsForValue();
		System.out.println("Before REdis ");
		String key = "spring.boot.redis.test";
		if (!this.template.hasKey(key)) {
			ops.set(key, "foo");
		}
		System.out.println("Found key " + key + ", value=" + ops.get(key));
		return ops.get(key);
	}

	@RequestMapping("/sleep")
	String home_sleep() throws Exception{
		Thread.sleep(100);
		return "Hello... World......";
	}
	
	@RequestMapping("/")
	String home1() {
		return "Hello... World......";
	}

	public static void main(String[] args) {
		SpringApplication.run(Tomcat_Example.class, args);
	
	}

}
