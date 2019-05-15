package com.NettyServer;

import org.apache.camel.CamelContext;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.netflix.hystrix.*;

import reactor.core.publisher.Mono;
//import reactor.core.publisher.Flux;
import java.util.concurrent.CompletableFuture;


import rx.Observable;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;


import org.apache.camel.ProducerTemplate;
import org.apache.camel.rx.ReactiveCamel;


@ComponentScan("com.NettyServer")
@RestController
@EnableAutoConfiguration
public class Netty_Example {
	private WebClient client = WebClient.create("http://localhost:8081");
	
	static ApplicationContext appContext ;
	static CamelContext camelContext ;
	static ProducerTemplate template;
	static Observable<String> observable;
	static ReactiveCamel rx;
	
    //static private NettySharedHttpServer nettySharedHttpServer;
	
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
	 public Mono<String> echo(@RequestParam(value = "foo", required = false) String foo)
    {
		//System.out.println(" sleep for 50ms");
        return Mono.just("I am from ECHO api-50ms "+foo).delayElement( Duration.ofMillis( 50 ) );
    }

	@RequestMapping("/sleep")
	public Mono<Long> getNewResult1() {
		return Mono.delay(Duration.ofMillis(100));
	}

	@RequestMapping("/camelrequest_multi")
	public Mono<String> getCamelResult1() {
		return  (Mono<String>)template.requestBody("direct:sampleroute1",0);
	}
	
	@RequestMapping("/multicastlarge")
	public Mono<String> getCamelMulticastlarge() {
	
		return  (Mono<String>)template.requestBody("direct:multicastlarge",0);
	}
	@RequestMapping("/multicastlargehyst")
	public Mono<String> getCamelMulticastlargehyst() {
		return (Mono<String>)template.requestBody("direct:multicastlargehysterix",0);
	}
	
	@RequestMapping("/multicastlargehyst2")
	public Mono<Object> getCamelMulticastlargehyst2() {
		Mono<Object> ret;
		CompletableFuture<Object> ret1;
		
		ret1 =  template.asyncRequestBody("direct:multicastlargehysterix2",0);
		//System.out.println(" About to Return Mono ");
		return Mono.fromFuture(ret1);
	}
	@RequestMapping("/multicastsmall2")
	public Mono<String> getCamelMulticastsmall2() {
		Mono<String> ret =  (Mono<String>)template.requestBody("direct:mainSmall",0);
		System.out.println(" About to Return Mono SMALL2 ");
		return ret;
	}
	public void print_stack(String point) {
		System.out.println(point+" Printing stack trace: "+Thread.currentThread().toString());
		/*StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		for (int i = 1; i < elements.length; i++) {
			StackTraceElement s = elements[i];
			System.out.println("\tat " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":"
					+ s.getLineNumber() + ")");
		}*/
	}
	@RequestMapping("/multicastsmall")
	public Mono<String> getCamelMulticastsmall() {
		//print_stack(" Rest MulticastSmall");
		Mono<String> ret =  (Mono<String>)template.requestBody("direct:multicastsmall",0);
		//System.out.println(" About to Return Mono small");
		return ret;
	}
	@RequestMapping("/multicastsmallAsync")
	public Mono<String> getCamelMulticastsmallAsync() {
		
		return  (Mono<String>)template.requestBody("direct:multicastsmallAsyncWeb",0);
	}
	
	public static void main(String[] args) throws Exception  {
	    appContext = new ClassPathXmlApplicationContext("application-context.xml");
		camelContext = SpringCamelContext.springCamelContext(appContext, false);        
		
		try {
			template = camelContext.createProducerTemplate();
			camelContext.start();
			System.out.println("Camel Started");			
		} finally {
			//camelContext.stop();
		}
		//camelContext.addHystrixConfiguration(id, configuration);
		HystrixTimerThreadPoolProperties.Setter().withCoreSize(3);
		System.out.println("coresize: "+HystrixTimerThreadPoolProperties.Setter().getCoreSize());
		System.out.println("Starting NETTY V3.0");
		SpringApplication.run(Netty_Example.class, args);
	
	}

}