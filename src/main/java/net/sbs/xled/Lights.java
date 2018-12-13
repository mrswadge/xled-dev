package net.sbs.xled;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;

import net.sbs.xled.discovery.Broadcast;
import net.sbs.xled.discovery.Discovery;
import net.sbs.xled.discovery.DiscoveryPublisher;
import net.sbs.xled.discovery.DiscoverySubscriber;
import net.sbs.xled.logging.LogFactory;

/**
 * Refer to unofficial, but excellent documentation for Twinkly lights here: <a href="https://xled-docs.readthedocs.io/en/latest/rest_api.html">REST API</a>.
 * @author sbs
 *
 */
public class Lights {
	public final Logger log = LogFactory.instance.getLogger(Lights.class.getName());

	private final DiscoverySubscriber subscriber = new DiscoverySubscriber();
	
	public static void main(String[] args) throws Exception {
		List<InetAddress> broadcastAddresses = new Broadcast().addresses();
		DiscoverySubscriber subscriber = new DiscoverySubscriber();
		List<Discovery> workers = Lists.newArrayList();

		int port = 5555;
		broadcastAddresses.stream().forEach(address -> {
			try {
				DiscoveryPublisher discovery = new DiscoveryPublisher(new InetSocketAddress(address, port));
				discovery.subscribe(subscriber);
				discovery.begin();
				workers.add(discovery);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		Thread.sleep(10000);

		for (Discovery discovery : workers) {
			discovery.close();
		}

	}
}
