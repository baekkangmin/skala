package com.sk.skala.stockapi.tools;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

public class HostInfo {
	private static String hostname;
	private static String os;
	private static String address;

	static {
		hostname = System.getenv("HOSTNAME");
		if (hostname == null) {
			hostname = System.getenv("COMPUTERNAME");
			if (hostname == null) {
				hostname = "localhost";
			}
		}
		os = System.getProperty("os.name");

		InetAddress inet = getLocalAddress();
		if (inet != null) {
			address = inet.getHostAddress();
		} else {
			address = "127.0.0.1";
		}
	}

	private static InetAddress getLocalAddress() {
		try {
			Enumeration<NetworkInterface> b = NetworkInterface.getNetworkInterfaces();
			while (b.hasMoreElements()) {
				for (InterfaceAddress f : b.nextElement().getInterfaceAddresses()) {
					if (f.getAddress().isSiteLocalAddress()) {
						return f.getAddress();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getHostnameAddress() {
		return hostname + "(" + address + ")";
	}

	public static String getHostname() {
		return hostname;
	}

	public static String getAddress() {
		return address;
	}

	public static String getOs() {
		return os;
	}
}
