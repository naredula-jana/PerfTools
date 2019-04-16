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

	@RequestMapping("/camelrequest")
	public String getCamelResult() {
		 return  template.requestBody("direct:sampleroute1",0, String.class);
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
		SpringApplication.run(Tomcat_Example.class, args);
	
	}

}
