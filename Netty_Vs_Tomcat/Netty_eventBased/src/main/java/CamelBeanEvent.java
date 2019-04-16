import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class CamelBeanEvent {
	private WebClient client = WebClient.create("http://localhost:8081");
	/*private Mono<ClientResponse> webrequest = client.get()
			.uri("/echo")
			.accept(MediaType.ALL)
			.exchange();*/

	
	public Mono<String> appendSomeText3()
    {
        Mono<String> monoObj =client.get().uri( "/echo" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBeanEvent::handleResponseString);
        return monoObj;
    }
	
	private static void handleResponseString(String s) {
        //System.out.println("handle response");
        //System.out.println(s);
    }
	 public Mono<Long> appendSomeText6(String str)
	    {
	        System.out.println( "System.out parallel  long 2" );
	        print_stack();
	        return  Mono.just(1234l).delayElement( Duration.ofMillis( 120000) );
	    }
	 

	
	public String appendSomeText7(String msg) {
		return "";
    }
	
	
	public void print_stack() {
		System.out.println("Printing stack trace:");
		  StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		  for (int i = 1; i < elements.length; i++) {
		    StackTraceElement s = elements[i];
		    System.out.println("\tat " + s.getClassName() + "." + s.getMethodName()
		        + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
		  }
	}
	
	public String appendSomeText1(String msg) {
        String finalMessage = msg + " text1...." ;
        //System.out.println(finalMessage);
        return finalMessage;
    }
	public String appendSomeText2(String msg) {
		print_stack();
		
        String finalMessage = msg + " text2222";
        //System.out.println(finalMessage);
        return finalMessage;
    }

	public Mono<String> appendSomeText4()
    {
        Mono<String> monoObj =client.get().uri( "/echo" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBeanEvent::handleResponseString);
        return monoObj;
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
