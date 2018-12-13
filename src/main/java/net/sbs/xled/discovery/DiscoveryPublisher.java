package net.sbs.xled.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.Flow.Subscriber;

import com.google.common.base.MoreObjects;

import java.util.concurrent.SubmissionPublisher;

public class DiscoveryPublisher extends Discovery {
	protected SubmissionPublisher<InetAddress> publisher = new SubmissionPublisher<>();

	public DiscoveryPublisher(InetSocketAddress socketAddress) throws SocketException {
		super(socketAddress);
	}

	public void recieve(DatagramPacket packet) {
		super.recieve(packet);
		publisher.submit(packet.getAddress());
	}

	@Override
	public void close() throws IOException {
		publisher.close();
		super.close();
	}
	
	public void subscribe(Subscriber<? super InetAddress> subscriber) {
		publisher.subscribe(subscriber);
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.toString();
	}
}
