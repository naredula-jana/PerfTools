import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class CamelBean {
	private WebClient client = WebClient.create("http://localhost:8081");
	private WebClient client_sync = WebClient.create("http://localhost:8081");
	static int count=0;
	/*private Mono<ClientResponse> result = client_sync.get()
			.uri("/echo")
			.accept(MediaType.ALL)
			.exchange();*/

	public String appendSomeText1(String msg) {
		Mono<ClientResponse> result = client_sync.get()
				.uri("/echo")
				.accept(MediaType.ALL)
				.exchange();
		String ret = "re1: " + msg+ result.flatMap(res -> res.bodyToMono(String.class)).block();
		handleResponseString(ret);
		return ret;
    }
	
	public String appendSomeText2(String msg) {
		Mono<ClientResponse> result = client_sync.get()
				.uri("/echo")
				.accept(MediaType.ALL)
				.exchange();
		String ret = "re2: " + msg+ result.flatMap(res -> res.bodyToMono(String.class)).block();
		handleResponseString(ret);
		return ret;
    }

	public String appendSomeText3(String msg) {
		Mono<ClientResponse> result = client_sync.get()
				.uri("/echo")
				.accept(MediaType.ALL)
				.exchange();
		String ret =  "re3: " + msg+ result.flatMap(res -> res.bodyToMono(String.class)).block();
		handleResponseString(ret);
		return ret;
    }
	public String appendSomeText4(String msg) {
		Mono<ClientResponse> result = client_sync.get()
				.uri("/echo")
				.accept(MediaType.ALL)
				.exchange();
		String ret =  "re4: " + msg+ result.flatMap(res -> res.bodyToMono(String.class)).block();
		handleResponseString(ret);
		return ret;
    }
	public String appendSomeText5(String msg) {
		Mono<ClientResponse> result = client_sync.get()
				.uri("/echo")
				.accept(MediaType.ALL)
				.exchange();
		String ret =  "re5: " + msg+ result.flatMap(res -> res.bodyToMono(String.class)).block();
		handleResponseString(ret);
		return ret;
    }
	public String appendSomeText6(String msg) {
		Mono<ClientResponse> result = client_sync.get()
				.uri("/echo")
				.accept(MediaType.ALL)
				.exchange();
		String ret =  "re6: " + msg+ result.flatMap(res -> res.bodyToMono(String.class)).block();
		handleResponseString(ret);
		return ret;
    }
	public String appendSomeText7(String msg) {
		Mono<ClientResponse> result = client_sync.get()
				.uri("/echo")
				.accept(MediaType.ALL)
				.exchange();
		String ret =  "re7: " + msg+ result.flatMap(res -> res.bodyToMono(String.class)).block();
		handleResponseString(ret);
		return ret;
    }
	
	public Mono<String> appendSomeText10(String msg){
        Mono<String> monoObj =client.get().uri( "/echo?foo=10" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBean::handleResponseString);
        
        return monoObj;
    }

	public Mono<String> appendSomeText20( String msg){
		 // String ret = msg.block(); System.out.println(" Test20 : "+ret);
		System.out.println(" Test20 : "+msg);
		
        Mono<String> monoObj =client.get().uri( "/echo?foo=20" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBean::handleResponseString);
        
        return monoObj;
    }
	
	public Mono<String> appendSomeText30( String msg){
		//String ret =  msg.block();
		//System.out.println(" Test30 : "+ret);
		
        Mono<String> monoObj =client.get().uri( "/echo?foo=30" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBean::handleResponseString);
        
        return monoObj;
    }
	public Mono<String> appendSomeText40(String msg){
		//String ret =  input.block();
		//System.out.println(" Test40 : "+ret);
		
        Mono<String> monoObj =client.get().uri( "/echo?foo=40" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBean::handleResponseString);
        return monoObj;
    }
	private static String handleResponseString(String s) {

		if (count%1000 ==0 ) {
          System.out.println("handle response function: "+s+" count: "+count);
		}
		count++;
        return s+ " :test" ;
    }
	public String computeText(){
        return "test";
    }

}
