package net.sbs.xled.discovery;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import com.google.common.base.MoreObjects;

import net.sbs.xled.logging.LogFactory;

public class Discovery implements AutoCloseable {
	
	public static final Logger log = LogFactory.instance.getLogger(Discovery.class.getName());

	protected static final String MAGIC_PACKET = "\u0001discover";
	protected static final long INTERVAL = 5000L;
	
	protected DatagramSocket socket;
	protected InetSocketAddress socketAddress;
	protected boolean closing = false;
	protected static final ScheduledExecutorService poller = Executors.newSingleThreadScheduledExecutor();
	protected ScheduledFuture<?> future = null;
	protected Receiver receiver;
	
	public Discovery(InetSocketAddress socketAddress) throws SocketException {
		this.socketAddress = socketAddress;
		this.socket = new DatagramSocket();
		this.receiver = new Receiver();
	}

	public void begin() {
		this.receiver.start();
		send(MAGIC_PACKET);
	}
	
	class Receiver extends Thread {
		public void run() {
			try {
				while (!closing) {
					byte[] buffer = new byte[256];
					DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
					socket.receive(packet);
					receive(packet);
				}
			} catch (IOException e) {
				if (!closing) {
					e.printStackTrace();
				}
			}
		}
	}
	
	protected void send(String multicastMessage) {
		this.future = poller.scheduleAtFixedRate(() -> {
			try {
				if (!closing && !socket.isClosed()) {
					byte[] buf = multicastMessage.getBytes();
					DatagramPacket packet = new DatagramPacket(buf, buf.length, socketAddress);
					socket.send(packet);
				} else {
					future.cancel(true);
				}
			} catch (IOException e) {
				if (!closing) {
					e.printStackTrace();
				}
			}
		}, 0, INTERVAL, TimeUnit.MILLISECONDS);
	}

	public void receive(DatagramPacket packet) {
		log.info(() -> String.format( "Received reply from %s of length %d", packet.getAddress().getHostAddress(), packet.getLength()));
		// String data = new String(packet.getData(), 0, packet.getLength());
		// int[] ipReverse = StringUtils.substring(data, 0, 4).chars().toArray();
		// String msg = StringUtils.substring(data, 4);
		// String deviceName = StringUtils.removeStartIgnoreCase(msg, "OK");
		log.info(() -> String.format("Found Twinkly Device: %s  IP address: %s",
				packet.getAddress().getHostName(), packet.getAddress().getHostAddress()));
	}

	@Override
	public void close() throws IOException {
		this.closing = true;
		future.cancel(true);
		receiver.interrupt();
		socket.close();
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("address", socketAddress)
				.toString();
	}
}
