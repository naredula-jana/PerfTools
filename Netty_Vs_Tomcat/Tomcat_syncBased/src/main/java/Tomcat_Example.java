import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

@RestController
@EnableAutoConfiguration
public class Tomcat_Example {
	private WebClient client = WebClient.create("http://localhost:8081");
	private Mono<ClientResponse> result = client.get()
			.uri("/echo")
			.accept(MediaType.ALL)
			.exchange();
	
	static ApplicationContext appContext ;
	static CamelContext camelContext ;
	static ProducerTemplate template;
	
	@RequestMapping("/webrequest")
	public String getNewResult() {
		return ">> result = " + result.flatMap(res -> res.bodyToMono(String.class)).block();
	}

	@RequestMapping("/camelrequest_multi")
	public String getCamelResult_multi() {
		 return  template.requestBody("direct:camelsync",0, String.class);
	}
	
	@RequestMapping("/camelrequest_seq")
	public String getCamelResult_seq() {
		 return  template.requestBody("direct:sampleroute2",0, String.class);
	}
	@RequestMapping("/multicastlargehyst")
	public String getCamelResult_multicastlargehyst() {
		 return  template.requestBody("direct:multicastlargehyst",0, String.class);
	}
	
	@RequestMapping("/multicastlarge")
	public String getCamelResult_multicastlarge() {
		 return  template.requestBody("direct:multicastlarge",0, String.class);
	}
	@RequestMapping("/multicastsmall")
	public String getCamelResult_multicastsmall() {
		 return  template.requestBody("direct:multicastsmall",0, String.class);
	}
	
	@RequestMapping("/camelrequest_hybrid")
	public String getCamelResult_hybrid() {
		  Mono<String> result =   (Mono<String>)template.requestBody("direct:hybrid",0);
		 return result.block();
		 
	}

	@RequestMapping("/sleep")
	String home_sleep() throws Exception{
		Thread.sleep(100);
		return "Hello... World......";
	}
	
	@RequestMapping("/")
	String home1() {
		return "Hello... World......";
	}

	public static void main(String[] args) throws Exception  {
		
	    appContext = new ClassPathXmlApplicationContext("application-context.xml");
		CamelContext camelContext = SpringCamelContext.springCamelContext(appContext, false);
		try {
			template = camelContext.createProducerTemplate();
			//camelContext.start();
			System.out.println("first");
		
			//template.sendBody("direct:sampleroute1", "Hello did i Print... ??");
			
		} finally {
			//camelContext.stop();
		}
		System.out.println("Starting TOMCAT v2.0  ");
		SpringApplication.run(Tomcat_Example.class, args);
	
	}

}
