import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class CamelBean {
	private WebClient client = WebClient.create("http://localhost:8081");
	private WebClient client_sync = WebClient.create("http://localhost:8081");
	/*private Mono<ClientResponse> result = client_sync.get()
			.uri("/echo")
			.accept(MediaType.ALL)
			.exchange();*/

	public String appendSomeText1(String msg) {
		Mono<ClientResponse> result = client_sync.get()
				.uri("/echo")
				.accept(MediaType.ALL)
				.exchange();
		return "re1: " + msg+ result.flatMap(res -> res.bodyToMono(String.class)).block();
    }
	
	public String appendSomeText2(String msg) {
		Mono<ClientResponse> result = client_sync.get()
				.uri("/echo")
				.accept(MediaType.ALL)
				.exchange();
		return "re2: " + msg+ result.flatMap(res -> res.bodyToMono(String.class)).block();
    }

	public String appendSomeText3(String msg) {
		Mono<ClientResponse> result = client_sync.get()
				.uri("/echo")
				.accept(MediaType.ALL)
				.exchange();
		return "re3: " + msg+ result.flatMap(res -> res.bodyToMono(String.class)).block()+ " :end33";
    }
	public String appendSomeText4(String msg) {
		Mono<ClientResponse> result = client_sync.get()
				.uri("/echo")
				.accept(MediaType.ALL)
				.exchange();
		return "re4: " + msg+ result.flatMap(res -> res.bodyToMono(String.class)).block()+ " :end44";
    }
	
	public Mono<String> appendSomeText10(String msg){
        Mono<String> monoObj =client.get().uri( "/echo" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBean::handleResponseString);
        return monoObj;
    }

	public Mono<String> appendSomeText20( Mono<String> msg){
		
		String ret =  msg.block();
		System.out.println(" Test20 : "+ret);
        Mono<String> monoObj =client.get().uri( "/echo" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBean::handleResponseString);
        return monoObj;
    }
	
	public Mono<String> appendSomeText30(String msg){
        Mono<String> monoObj =client.get().uri( "/echo" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBean::handleResponseString);
        
        return monoObj;
    }
	public Mono<String> appendSomeText40(String msg){
        Mono<String> monoObj =client.get().uri( "/echo" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBean::handleResponseString);
        return monoObj;
    }
	private static String handleResponseString(String s) {
        System.out.println("handle response");
        System.out.println(s);
        return s+ " :test" ;
    }
	public String computeText(){
        return "test";
    }

}
