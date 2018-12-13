package net.sbs.xled.discovery;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.common.base.MoreObjects;

import net.sbs.xled.logging.LogFactory;
import net.sbs.xled.response.JsonToMapResponseHandler;

public class DiscoverySubscriber implements Subscriber<InetAddress> {
	public final Logger log = LogFactory.instance.getLogger(DiscoverySubscriber.class.getName());

	private Subscription subscription;
	
	@Override
	public void onComplete() {
		log.info("Done");
	}

	@Override
	public void onError(Throwable throwable) {
		throwable.printStackTrace();
		log.log(Level.SEVERE, throwable, () -> "Error encountered by subscriber.");
	}

	@Override
	public void onNext(InetAddress address) {
		log.info(() -> String.format("Notified subscriber: %s", address));
		
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		try ( CloseableHttpClient httpClient = httpClientBuilder.build() ) {
			log.info(() -> String.format("Using REST API to: %s", address));
			URI uri = new URIBuilder().setScheme("http").setHost(address.getHostAddress()).setPath("/xled/v1/gestalt").build();
			HttpGet httpGet = new HttpGet(uri);
			Map<String, Object> map = httpClient.execute(httpGet, new JsonToMapResponseHandler());
			log.info(String.valueOf(map));
			
			// login
			uri = new URIBuilder().setScheme("http").setHost(address.getHostAddress()).setPath("/xled/v1/login").build();
			HttpPost httpPost = new HttpPost(uri);
			httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
			// https://xled-docs.readthedocs.io/en/latest/rest_api.html#login
			// Random 32 byte string encoded with base64
			httpPost.setEntity(new StringEntity("{\"challenge\": \"AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=\"}")); 
			Map<String, Object> loginMap = httpClient.execute(httpPost, new JsonToMapResponseHandler());
			log.info(String.valueOf(loginMap));
			
			String authentication_token = (String) loginMap.get("authentication_token");
			String challenge_response = (String) loginMap.get("challenge-response");
			
			// verify
			uri = new URIBuilder().setScheme("http").setHost(address.getHostAddress()).setPath("/xled/v1/verify").build();
			httpPost = new HttpPost(uri);
			httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
			httpPost.addHeader("X-Auth-Token", authentication_token);
			httpPost.setEntity(new StringEntity(String.format("{\"challenge-response\": \"%s\"}", challenge_response)));
			Map<String, Object> verifyMap = httpClient.execute(httpPost, new JsonToMapResponseHandler());
			log.info(String.valueOf(verifyMap));

			// change mode
			uri = new URIBuilder().setScheme("http").setHost(address.getHostAddress()).setPath("/xled/v1/led/mode").build();
			httpPost = new HttpPost(uri);
			httpPost.addHeader(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
			httpPost.addHeader("X-Auth-Token", authentication_token);
			// https://xled-docs.readthedocs.io/en/latest/rest_api.html#change-led-operation-mode
			// Available modes: off, movie, demo, rt 
			httpPost.setEntity(new StringEntity("{\"mode\": \"movie\"}")); 
			Map<String, Object> modeMap = httpClient.execute(httpPost, new JsonToMapResponseHandler());
			log.info(String.valueOf(modeMap));
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		long updates = Long.MAX_VALUE;
		log.info(() -> String.format("Subscribed to: %s with %d updates.", subscription, updates));
		this.subscription = subscription;
		subscription.request(updates);
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("subscription", subscription).toString();
	}
}
