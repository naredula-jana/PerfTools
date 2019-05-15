package com.NettyServer;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import org.asynchttpclient.AsyncCompletionHandler;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClientConfig;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Response;
import org.asynchttpclient.extras.guava.ListenableFutureAdapter;

import reactor.core.CoreSubscriber;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscription;

import io.netty.util.concurrent.*;
import io.netty.channel.EventLoopGroup;
import reactor.netty.http.HttpResources;


public class CamelBeanEvent {
	private WebClient client = WebClient.create("http://localhost:8081");
	private static int count = 0;
	static int fallback_count = 0;

	private io.netty.channel.nio.NioEventLoopGroup camelNettyEventLoopGroup;
	private DefaultAsyncHttpClientConfig clientBuilder;
	private AsyncHttpClient asyncClient;
	private int init_done = 0;
	private boolean debug=true;
	
	/*
	 * public class SubmissionPublisher<T> implements Publisher<T> {
	 * 
	 * }
	 */
	public static void displayStats() {
		EventLoopGroup elg = HttpResources.get().onClient(true);
	    int index = 0;
	    int total_events=0;
	    for (final EventExecutor eventExecutor : elg) {
	      if (eventExecutor instanceof SingleThreadEventExecutor) {
	    	  final SingleThreadEventExecutor singleExecutor = (SingleThreadEventExecutor)eventExecutor;
	    	  System.out.println("Thread id: "+index+" Total Events: "+singleExecutor.pendingTasks());
	    	  total_events=total_events+singleExecutor.pendingTasks();
	          index++;
	      }
	    }
	    System.out.println(" Total events on all Rest controller: "+total_events);
	  }
	
	public class Camelsubscriber<T> implements CoreSubscriber<T> {

		@Override
		public void onSubscribe(Subscription s) {
			s.request(Long.MAX_VALUE);
			//print_stack("OnSubscription");
		}
		
		@Override
		public void onNext(T t) {
			//print_stack("handle response from subscriber: "+t);	
			if (count % 1000 == 0) {
				System.out.println(
						"New handle response function: " + t + " count: " + count + " fallback count: " + fallback_count);
				displayStats();
			}
			count++;
		}
		
		@Override
		public void onError(Throwable t) {
			print_stack("InError");
		}
		@Override
		public void onComplete() {
			//print_stack("OnComplete");
		}
	}

	@PostConstruct
	public void init() {
		camelNettyEventLoopGroup = Netty_Example.appContext.getBean("sharedNIOPool",
				io.netty.channel.nio.NioEventLoopGroup.class);
		clientBuilder = clientBuilder = Dsl.config().setMaxConnections(5000).setPooledConnectionIdleTimeout(100)
				.setConnectionTtl(500).setRequestTimeout(5000).setThreadPoolName("My AHC ThreadPool").setKeepAlive(true)
				.setEventLoopGroup(camelNettyEventLoopGroup).build();
		asyncClient = Dsl.asyncHttpClient(clientBuilder);
		init_done = 1;
	}

	public Mono<String> appendSomeText100(String s) {
		if (init_done == 0) {
			init();
		}
		// System.out.println(" inside the bean-1");
		BoundRequestBuilder getRequest = asyncClient.prepareGet("http://localhost:8081/echo?foo=100");
		org.asynchttpclient.ListenableFuture<String> listenableFuture = getRequest
				.execute(new AsyncCompletionHandler<String>() {
					@Override
					public String onCompleted(Response response) throws Exception {
						String responseStr = response.getResponseBody();
						return responseStr;
					}
				});
		com.google.common.util.concurrent.ListenableFuture<String> guavaListenableFuture = ListenableFutureAdapter
				.asGuavaFuture(listenableFuture);
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

		// Mono<String> monoObj =asyncClient.get().uri( "/echo?foo=1"
		// ).retrieve().bodyToMono(String.class);
		monoObject.subscribe(CamelBeanEvent::handleResponseString);
		return monoObject;
	}

	public Mono<String> appendSomeText200(String s) {
		if (init_done == 0) {
			init();
		}
		// System.out.println(" inside the bean-2");
		BoundRequestBuilder getRequest = asyncClient.prepareGet("http://localhost:8081/echo?foo=200");
		org.asynchttpclient.ListenableFuture<String> listenableFuture = getRequest
				.execute(new AsyncCompletionHandler<String>() {
					@Override
					public String onCompleted(Response response) throws Exception {
						String responseStr = response.getResponseBody();
						return responseStr;
					}
				});
		com.google.common.util.concurrent.ListenableFuture<String> guavaListenableFuture = ListenableFutureAdapter
				.asGuavaFuture(listenableFuture);
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

		// Mono<String> monoObj =asyncClient.get().uri( "/echo?foo=1"
		// ).retrieve().bodyToMono(String.class);
		monoObject.subscribe(CamelBeanEvent::handleResponseString);
		return monoObject;
	}
	
	
	public Mono<Object> mainBean(String s) {
		
		//Mono<String> ret =  (Mono<String>)Netty_Example.template.requestBody("direct:multicastsmall",0);
		Mono<Object> ret = Mono.just(Netty_Example.template.requestBody("direct:multicastsmall",0));
		
		//ret.subscribe(CamelBeanEvent::handleResponseString);
		System.out.println(" About to Return Mono from MAIN Bean..... ");
		return ret;
	}
	

	public Mono<String> appendSomeText1(String s) {
		Camelsubscriber<String> camelsub = new Camelsubscriber() ;
		
		Mono<String> monoObj = client.get().uri("/echo?foo=1").retrieve().bodyToMono(String.class);
		//System.out.println(" inside camelbean1");
		monoObj.publishOn(Schedulers.immediate()).subscribe(camelsub);
		//monoObj.publishOn(Schedulers.immediate()).subscribe(CamelBeanEvent::handleResponseString);
		//print_stack("camel-bean1: before sleep 7sec ");
		try { 
		   //Thread.sleep(1000);
		}catch(Exception e){
			
		}
		//print_stack("after sleep camel-bean1:  ");
		return monoObj;
	}

	public Mono<String> appendSomeText2(String s) {
		
		Mono<String> monoObj = client.get().uri("/echo?foo=2").retrieve().bodyToMono(String.class);
		monoObj.subscribe(CamelBeanEvent::handleResponseString);
		//System.out.println(" inside camelbean2");
		//System.out.println("Camel-bean-2 Printing stack trace: "+Thread.currentThread().toString());
		return monoObj;
	}

	public Mono<String> appendSomeText3(String s) {
		Mono<String> monoObj = client.get().uri("/echo?foo=3").retrieve().bodyToMono(String.class);
		monoObj.subscribe(CamelBeanEvent::handleResponseString);
		return monoObj;
	}

	public Mono<String> appendSomeText4(String s) {
		Mono<String> monoObj = client.get().uri("/echo?foo=4").retrieve().bodyToMono(String.class);
		monoObj.subscribe(CamelBeanEvent::handleResponseString);
		return monoObj;
	}

	public Mono<String> appendSomeText5(String s) {
		Mono<String> monoObj = client.get().uri("/echo?foo=5").retrieve().bodyToMono(String.class);
		monoObj.subscribe(CamelBeanEvent::handleResponseString);
		return monoObj;
	}

	public Mono<String> appendSomeText6(String s) {
		//System.out.println(" inside camelbean6");
		Mono<String> monoObj = client.get().uri("/echo?foo=6").retrieve().bodyToMono(String.class);
		monoObj.subscribe(CamelBeanEvent::handleResponseString);
		return monoObj;
	}

	public Mono<String> appendSomeText7(String s) {
		//System.out.println(" inside camelbean7");
		Mono<String> monoObj = client.get().uri("/echo?foo=6").retrieve().bodyToMono(String.class);
		monoObj.subscribe(CamelBeanEvent::handleResponseString);
		return monoObj;
	}
	public CompletableFuture<String> appendSomeText71(String s) {
		//System.out.println(" inside camelbean71");
		Mono<String> monoObj = client.get().uri("/echo?foo=71").retrieve().bodyToMono(String.class);
		monoObj.subscribe(CamelBeanEvent::handleResponseString);
		return monoObj.toFuture();
	}

	private static void handleResponseString(String s) {
		//print_stack("handle response: ");
		// System.out.println("handle response: "+s);
		if (count % 1000 == 0) {
			System.out.println(
					"handle response function: " + s + " count: " + count + " fallback count: " + fallback_count);
		}
		count++; // System.out.println(s);
	}

	public String computeText() {
		return "test";
	}

	public String fallback() {
		fallback_count++;
		System.out.println("FALLBACK function called ");
		return "fallback function";
	}

	public static void print_stack(String point) {
		System.out.println(point+" Printing stack trace: "+Thread.currentThread().toString());
		/*StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		for (int i = 1; i < elements.length; i++) {
			StackTraceElement s = elements[i];
			System.out.println("\tat " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":"
					+ s.getLineNumber() + ")");
		}*/
	}
}
