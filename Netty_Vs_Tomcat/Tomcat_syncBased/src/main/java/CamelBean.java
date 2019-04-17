import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class CamelBean {
	private WebClient client = WebClient.create("http://localhost:8081");
	private Mono<ClientResponse> result = client.get()
			.uri("/echo")
			.accept(MediaType.ALL)
			.exchange();
	
	public String appendSomeText2(String msg) {
		return "re: " + result.flatMap(res -> res.bodyToMono(String.class)).block();
    }

	public String appendSomeText3(String msg) {
		return "re: " + result.flatMap(res -> res.bodyToMono(String.class)).block();
    }
	public String appendSomeText4(String msg) {
		return "re: " + result.flatMap(res -> res.bodyToMono(String.class)).block();
    }

}
