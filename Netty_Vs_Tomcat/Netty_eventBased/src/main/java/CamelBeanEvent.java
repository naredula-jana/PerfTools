import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class CamelBeanEvent {
	private WebClient client = WebClient.create("http://localhost:8081");
	private Mono<ClientResponse> webrequest = client.get()
			.uri("/echo")
			.accept(MediaType.ALL)
			.exchange();

	
	public Mono<String> appendSomeText6(String msg) {
		return webrequest.flatMap(res -> res.bodyToMono(String.class));
    }
	
	public String appendSomeText1(String msg) {
        String finalMessage = msg + " text1...." ;
        //System.out.println(finalMessage);
        return finalMessage;
    }
	public String appendSomeText2(String msg) {
		//System.out.println("Inside the route.. final message is ..");
        String finalMessage = msg + " text2222";
        //System.out.println(finalMessage);
        return finalMessage;
    }
	public String appendSomeText3(String msg) {
		//System.out.println("Inside the route.. final message is ..");
        String finalMessage = msg + " text3333";
        //System.out.println(finalMessage);
        return finalMessage;
    }
	public String appendSomeText4(String msg) {
		//System.out.println("Inside the route.. final message is ..");
        String finalMessage = msg + " text4444";
        //System.out.println(finalMessage);
        return finalMessage;
    }
	public String appendSomeText5(String msg) {
		//System.out.println("Inside the route.. final message is ..");
        String finalMessage = msg + " text5....";
        //System.out.println(finalMessage);
        return finalMessage;
    }
	public String multicast1(String msg) {
		//System.out.println("Inside the route.. final message is ..");
        String finalMessage = msg + " multicast111";
        //System.out.println(finalMessage);
        return finalMessage;
    }
}
