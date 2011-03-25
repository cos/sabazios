package edu.illinois.reLooper.sabazios.log;

public class Log {
	private static long time;

	public static void start() {
		time = System.currentTimeMillis();
	}

	public static void log(String s) {
		System.out.println((System.currentTimeMillis() - time) / 1000 + " : " + s);
		time = System.currentTimeMillis();
	}
}
