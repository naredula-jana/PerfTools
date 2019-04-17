import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class CamelBeanEvent {
	private WebClient client = WebClient.create("http://localhost:8081");
	

	public Mono<String> appendSomeText2()
    {
        Mono<String> monoObj =client.get().uri( "/echo" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBeanEvent::handleResponseString);
        return monoObj;
    }

	public Mono<String> appendSomeText3()
    {
        Mono<String> monoObj =client.get().uri( "/echo" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBeanEvent::handleResponseString);
        return monoObj;
    }
	public Mono<String> appendSomeText4()
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
	       // print_stack();
	        return  Mono.just(1234l).delayElement( Duration.ofMillis( 120000) );
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
}
