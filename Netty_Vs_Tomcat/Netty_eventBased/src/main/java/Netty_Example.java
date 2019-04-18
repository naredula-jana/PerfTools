import org.apache.camel.CamelContext;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;



import reactor.core.publisher.Mono;
import rx.RxReactiveStreams;
import reactor.core.publisher.Flux;

import reactor.adapter.rxjava.RxJava2Adapter;
//import io.reactivex.BackpressureStrategy;
//import io.reactivex.Observable;

import org.reactivestreams.Publisher;

import org.reactivestreams.Subscriber;

import rx.Observable;


/*import org.springframework.web.reactive.function.client.ClientResponse;*/
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreams;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;



import org.apache.camel.ProducerTemplate;
import org.apache.camel.rx.ReactiveCamel;




@RestController
@EnableAutoConfiguration
public class Netty_Example {
	private WebClient client = WebClient.create("http://localhost:8081");
	
	/*@Value("com.example.value")
	private String value;*/
	
	static ApplicationContext appContext ;
	static CamelContext camelContext ;
	static ProducerTemplate template;
	static Observable<String> observable;
	static ReactiveCamel rx;
	static CamelReactiveStreamsService camel;
	
	private Mono<ClientResponse> webrequest = client.get()
			.uri("/echo")
			.accept(MediaType.ALL)
			.exchange();
	

	@RequestMapping("/")
	String home1() {
		return "Hello... World......";
	}
	
	@RequestMapping("/webrequest")
	public Mono<String> getNewResult() {
	
		return webrequest.flatMap(res -> res.bodyToMono(String.class));
	}

	@RequestMapping("/echo")
	 public Mono<String> echo()
    {
        return Mono.just("I am from ECHO api").delayElement( Duration.ofMillis( 50 ) );
    }

	
	@RequestMapping("/sleep")
	public Mono<Long> getNewResult1() 
	{
		return Mono.delay(Duration.ofMillis(100));
	}

	
	@RequestMapping("/camelrequest_multi")
	public Mono<String> getCamelResult1() {
		return  (Mono<String>)template.requestBody("direct:sampleroute1",0);
	}
	@RequestMapping("/camelrequest_seq")
	public Mono<String> getCamelResult2() {
		return  (Mono<String>)template.requestBody("direct:sampleroute2",0);
	}
	
	@RequestMapping("/camelrequest3")
	public Mono<String> getCamelResult3() {
		return  (Mono<String>)template.requestBody("direct:sampleroute3",0);
	}
	
	public static void main(String[] args) throws Exception  {

	    appContext = new ClassPathXmlApplicationContext("application-context.xml");
		camelContext = SpringCamelContext.springCamelContext(appContext, false);
		
		
		//rx = new ReactiveCamel(camelContext);
		//observable = rx.toObservable("direct:sampleroute3", String.class);
		
		try {
			template = camelContext.createProducerTemplate();
			camelContext.start();
			System.out.println("first");
			camel = CamelReactiveStreams.get(camelContext);
			
		} finally {
			//camelContext.stop();
		}
		System.out.println("Starting NETTY v1.0");
		SpringApplication.run(Netty_Example.class, args);
	
	}

}