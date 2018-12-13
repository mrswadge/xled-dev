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

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
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
		log.log(Level.SEVERE, throwable, () -> "Error encountered by subscriber.");
	}

	@Override
	public void onNext(InetAddress address) {
		log.info(() -> String.format("Notified subscriber: %s", address));
		
		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		try ( CloseableHttpClient httpClient = httpClientBuilder.build() ) {
			URI uri = new URIBuilder().setScheme("http").setHost(address.getHostAddress()).setPath("/xled/v1/gestalt").build();
			HttpGet httpGet = new HttpGet(uri);
			Map<String, Object> map = httpClient.execute(httpGet, new JsonToMapResponseHandler());
			log.info(String.valueOf(map));
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
