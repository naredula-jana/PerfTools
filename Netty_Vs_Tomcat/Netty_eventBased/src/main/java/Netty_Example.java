import org.apache.camel.CamelContext;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/*import com.fasterxml.jackson.annotation.JsonView;
import reactor.core.publisher.Flux;*/
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import reactor.adapter.rxjava.RxJava2Adapter;
import io.reactivex.BackpressureStrategy;

/*import org.springframework.web.reactive.function.client.ClientResponse;*/
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreams;
import org.apache.camel.component.reactive.streams.api.CamelReactiveStreamsService;

import org.reactivestreams.Publisher;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import org.apache.camel.rx.ReactiveCamel;
import rx.Observable;



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
	public Mono<Long> echo() {
		return Mono.delay(Duration.ofMillis(50));
	}
	
	@RequestMapping("/sleep")
	public Mono<Long> getNewResult1() {
		return Mono.delay(Duration.ofMillis(100));
	}
	
	@RequestMapping("/camelrequest1")
	public Mono<String> getCamelResult1() {
		 //return Mono.just(template.requestBody("direct:sampleroute2",0, String.class));
		 //return Mono.fromSupplier(()->template.requestBody("direct:sampleroute2",0, String.class));
		 return Mono.fromSupplier(()->template.requestBody("direct:sampleroute2",0, String.class));
	}
	
	@RequestMapping("/camelrequest")
	public Mono<String> getCamelResult() {
	
		 return Mono.just(template.requestBody("direct:sampleroute1",0, String.class));

	}

	@RequestMapping("/camelrequest2")
	public Flux<String> getCamelResult2() {
		//Flux<String> flux = Flux.just("red", "white", "blue");
		//return flux;
		
		//rx.sendTo(observable, "direct:sampleroute3");
		//return Mono.just(template.requestBody("direct:sampleroute1",0, String.class));
		
		return RxJava2Adapter.observableToFlux(observable, BackpressureStrategy.BUFFER);
		
		return Flux.from(observable.toFlowable(BackpressureStrategy.BUFFER));
	}
	
	public static void main(String[] args) throws Exception  {
		System.out.println("Starting ...");
	    appContext = new ClassPathXmlApplicationContext("application-context.xml");
		camelContext = SpringCamelContext.springCamelContext(appContext, false);
		
		rx = new ReactiveCamel(camelContext);
		observable = rx.toObservable("direct:sampleroute3", String.class);
		try {
			template = camelContext.createProducerTemplate();
			camelContext.start();
			System.out.println("first");
			
		} finally {
			//camelContext.stop();
		}
		
		SpringApplication.run(Netty_Example.class, args);
	
	}

}