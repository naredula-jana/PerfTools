import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class CamelBean {
	/*private WebClient client = WebClient.create("http://localhost:8081");
	private Mono<ClientResponse> result = client.get()
			.uri("/echo")
			.accept(MediaType.ALL)
			.exchange();*/
	
	
	public String appendSomeText1(String msg) {
		
		//String ret =new String(  ">> result = " + result.flatMap(res -> res.bodyToMono(String.class)).block());
		//String finalMessage = msg + " text1111" +ret;

        String finalMessage = msg + " text1111" ;
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
        String finalMessage = msg + " text5555";
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
