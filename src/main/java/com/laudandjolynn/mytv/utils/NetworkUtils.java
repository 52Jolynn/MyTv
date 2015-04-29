package com.laudandjolynn.mytv.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * @author: Laud
 * @email: htd0324@gmail.com
 * @date: 2015年4月29日 下午12:44:25
 * @copyright: www.laudandjolynn.com
 */
public class NetworkUtils {
	/**
	 * 判断是否可达
	 * 
	 * @param host
	 * @param port
	 * @param timeout
	 *            单位: ms
	 * @return
	 */
	public static boolean isReachable(String host, int port, int timeout) {
		Enumeration<NetworkInterface> nets;
		try {
			nets = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			return false;
		}
		InetSocketAddress remote = new InetSocketAddress(host, port);
		while (nets.hasMoreElements()) {
			NetworkInterface net = nets.nextElement();
			Enumeration<InetAddress> addresses = net.getInetAddresses();
			while (addresses.hasMoreElements()) {
				InetAddress address = addresses.nextElement();
				SocketAddress local = new InetSocketAddress(address, 0);
				Socket socket = new Socket();
				try {
					socket.bind(local);
					socket.connect(remote, timeout);
					return true;
				} catch (IOException e) {
					continue;
				}
			}
		}
		return false;
	}
}
