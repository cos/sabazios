package edu.illinois.reLooper.sabazios.log;

public class Log {
	private static long time;

	public static void start() {
		time = System.currentTimeMillis();
	}

	public static void log(String s) {
		long t = System.currentTimeMillis() - time;
		System.out.println(t / 1000 + "." + (t % 1000) / 10 + " : " + s);
		time = t + time;
	}
}
