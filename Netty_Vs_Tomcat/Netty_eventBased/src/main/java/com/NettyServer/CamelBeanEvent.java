package com.NettyServer;
import java.time.Duration;

import javax.annotation.PostConstruct;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import reactor.core.publisher.Mono;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.asynchttpclient.extras.guava.ListenableFutureAdapter;

//import Netty_Example;

public class CamelBeanEvent {
	private WebClient client = WebClient.create("http://localhost:8081");
	private static int count=0;
		
	private io.netty.channel.nio.NioEventLoopGroup camelNettyEventLoopGroup ;
	private DefaultAsyncHttpClientConfig clientBuilder;
	private AsyncHttpClient asyncClient;
	private int init_done=0;
	 
	@PostConstruct
    public void init() {
	    	 camelNettyEventLoopGroup = Netty_Example.appContext.getBean("sharedNIOPool",io.netty.channel.nio.NioEventLoopGroup.class);
	    	 clientBuilder = clientBuilder = Dsl.config()
	    	            .setMaxConnections(5000)
	    	            .setPooledConnectionIdleTimeout(100)
	    	            .setConnectionTtl(500)
	    	            .setRequestTimeout(5000)
	    	            .setThreadPoolName( "My AHC ThreadPool" )
	    	            .setKeepAlive( true )
	    	            .setEventLoopGroup(camelNettyEventLoopGroup)
	    	            .build();
	    	  asyncClient = Dsl.asyncHttpClient(clientBuilder);
	    	  init_done=1;
	    }

		public Mono<String> appendSomeText100(String s){
			if (init_done==0) {
				init();
			}
			//System.out.println(" inside the bean-1");
			BoundRequestBuilder getRequest = asyncClient.prepareGet("http://localhost:8081/echo?foo=100");
			org.asynchttpclient.ListenableFuture<String> listenableFuture = getRequest.execute(new AsyncCompletionHandler<String>() {
	              @Override
	              public String onCompleted(Response response) throws Exception {
	                String responseStr = response.getResponseBody();
	                return responseStr;
	              }
	            });
	            com.google.common.util.concurrent.ListenableFuture<String> guavaListenableFuture = ListenableFutureAdapter.asGuavaFuture(listenableFuture);
	            Mono<String> monoObject = Mono.create(callback -> {
	              Futures.addCallback(guavaListenableFuture, new FutureCallback<String>() {

	                @Override
	                public void onSuccess(String result) {
	                  callback.success(result);
	                }

	                @Override
	                public void onFailure(Throwable t) {
	                  t.printStackTrace();
	                  callback.error(new Exception(t.getMessage()));
	                }
	              });
	            });
		    
	            
	       // Mono<String> monoObj =asyncClient.get().uri( "/echo?foo=1" ).retrieve().bodyToMono(String.class);
	        monoObject.subscribe(CamelBeanEvent::handleResponseString);
	        return monoObject;
	    }
		public Mono<String> appendSomeText200(String s){
			if (init_done==0) {
				init();
			}
			//System.out.println(" inside the bean-2");
			BoundRequestBuilder getRequest = asyncClient.prepareGet("http://localhost:8081/echo?foo=200");
			org.asynchttpclient.ListenableFuture<String> listenableFuture = getRequest.execute(new AsyncCompletionHandler<String>() {
	              @Override
	              public String onCompleted(Response response) throws Exception {
	                String responseStr = response.getResponseBody();
	                return responseStr;
	              }
	            });
	            com.google.common.util.concurrent.ListenableFuture<String> guavaListenableFuture = ListenableFutureAdapter.asGuavaFuture(listenableFuture);
	            Mono<String> monoObject = Mono.create(callback -> {
	              Futures.addCallback(guavaListenableFuture, new FutureCallback<String>() {

	                @Override
	                public void onSuccess(String result) {
	                  callback.success(result);
	                }

	                @Override
	                public void onFailure(Throwable t) {
	                  t.printStackTrace();
	                  callback.error(new Exception(t.getMessage()));
	                }
	              });
	            });
		    
	       // Mono<String> monoObj =asyncClient.get().uri( "/echo?foo=1" ).retrieve().bodyToMono(String.class);
	        monoObject.subscribe(CamelBeanEvent::handleResponseString);
	        return monoObject;
	    }
		
		
		
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
