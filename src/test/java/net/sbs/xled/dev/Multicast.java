package net.sbs.xled.dev;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.logging.Logger;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;

import net.sbs.xled.discovery.Broadcast;
import net.sbs.xled.discovery.Discovery;
import net.sbs.xled.discovery.DiscoveryPublisher;
import net.sbs.xled.discovery.DiscoverySubscriber;
import net.sbs.xled.logging.LogFactory;

public class Multicast {

	public static final Logger log = LogFactory.instance.getLogger(Multicast.class.getName());

	@Test
	public void detectLights() throws Exception {
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

		Thread.sleep(25000);

		for (Discovery discovery : workers) {
			discovery.close();
		}
	}
}
