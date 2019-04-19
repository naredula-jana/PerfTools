import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

public class CamelBeanEvent {
	private WebClient client = WebClient.create("http://localhost:8081");
	private static int count=0;

	public Mono<String> appendSomeText1(String s){
        Mono<String> monoObj =client.get().uri( "/echo?foo=1" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBeanEvent::handleResponseString);
        return monoObj;
    }

	public Mono<String> appendSomeText2(String s){
        Mono<String> monoObj =client.get().uri( "/echo?foo=2" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBeanEvent::handleResponseString);
        return monoObj;
    }
	
	public Mono<String> appendSomeText3(String s){
        Mono<String> monoObj =client.get().uri( "/echo?foo=3" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBeanEvent::handleResponseString);
        return monoObj;
    }
	public Mono<String> appendSomeText4(String s){
        Mono<String> monoObj =client.get().uri( "/echo?foo=4" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBeanEvent::handleResponseString);
        return monoObj;
    }
	public Mono<String> appendSomeText5(String s){
        Mono<String> monoObj =client.get().uri( "/echo?foo=5" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBeanEvent::handleResponseString);
        return monoObj;
    }
	public Mono<String> appendSomeText6(String s){
        Mono<String> monoObj =client.get().uri( "/echo?foo=6" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBeanEvent::handleResponseString);
        return monoObj;
    }
	public Mono<String> appendSomeText7(String s){
        Mono<String> monoObj =client.get().uri( "/echo?foo=7" ).retrieve().bodyToMono(String.class);
        monoObj.subscribe(CamelBeanEvent::handleResponseString);
        return monoObj;
    }
	
	
	private static void handleResponseString(String s) {
       // System.out.println("handle response: "+s);		
        if (count%1000 ==0 ) {
            System.out.println("handle response function: "+s+" count: "+count);
  		}
  		count++;        //System.out.println(s);
    }

	
	public String computeText(){
        return "test";
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
