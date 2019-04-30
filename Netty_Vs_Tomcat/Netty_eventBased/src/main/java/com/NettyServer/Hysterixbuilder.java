
package com.NettyServer;
import org.springframework.stereotype.Component;
import org.apache.camel.builder.RouteBuilder;

@Component()
public class Hysterixbuilder extends RouteBuilder {
	
    @Override
    public void configure() throws Exception {
    	System.out.println("INSIDE the HYSTERIX builder");
    	/*
    	from("bean:myBean?method=appendSomeText1")
        .hystrix()
        .id("direct:multicastlarge")
        .hystrixConfiguration()
        	.end()
        .onFallbackViaNetwork()
        	.transform().constant("Fallback message");
        */
    	
    }
	
}

