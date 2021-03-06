package org.jvalue.commons.utils;


import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Check if a HTTP service is available.
 * If a HTTP connection cant't be established, it
 * will wait a and retry to connect to the service.
 */
public final class HttpServiceCheck {

	public static final int DEFAULT_RETRIES = 50;
	public static final int DEFAULT_SLEEP_TIME_BETWEEN_RETRIES = 3000;

	public static final String MONGODB_URL = "http://localhost:27017/";
	public static final String COUCHDB_URL = "http://localhost:5984/";

	/**
	 * Check if HTTP service is available.
	 * Default amount of retries are {@value #DEFAULT_RETRIES}.
	 * Default time to wait until next retry is {@value #DEFAULT_SLEEP_TIME_BETWEEN_RETRIES}.
	 * @param url URL of the HTTP service to check.
	 * @return true if the service is available and false if not.
	 */
	public static boolean check(String url) {
		return doCheck(url, DEFAULT_RETRIES, DEFAULT_SLEEP_TIME_BETWEEN_RETRIES);
	}

	/**
	 * Check if HTTP service is available.
	 * @param url URL of the HTTP service to check
	 * @param retries amount of retries until fail
	 * @param sleepTimeBetweenRetries time to wait until next retry in milliseconds
	 * @return true if the service is available and false if not.
	 */
	public static boolean check(String url, int retries, int sleepTimeBetweenRetries) {
		return doCheck(url, retries, sleepTimeBetweenRetries);
	}


	private static boolean doCheck(String url, int retries, int sleepTimeBetweenRetries) {
		int retryCounter = retries;

		do {
			if (isReachable(url)) {
				Log.info("[success] Service check for URL " + url);
				return true;
			} else {
				String msg = String.format("[%d/%d] Retry service check for URL %s", retryCounter, retries, url);
				Log.info(msg);
				doSleep(sleepTimeBetweenRetries);
			}
			retryCounter--;
		} while (retryCounter > 0);

		Log.error("[failure] Service check for URL  " + url);
		return false;
	}


	private static boolean isReachable(String url) {
		try {
			URL serviceUrl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) serviceUrl.openConnection();
			connection.connect();

			int responseCode = connection.getResponseCode();
			return isOk(responseCode);
		} catch (Exception e) {
		}

		return false;
	}


	private static void doSleep(int sleepTime) {
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			Log.error(e.getMessage());
		}
	}


	private static boolean isOk(int statusCode) {
		return statusCode == 200;
	}

}
