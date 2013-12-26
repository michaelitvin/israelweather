package com.litvin.israelweather;

import java.net.*;
import java.io.*;
import java.util.*;
import java.text.*;

import android.util.Log;

/**
 * CookieManager is a simple utilty for handling cookies when working with
 * java.net.URL and java.net.URLConnection objects.
 * 
 * 
 * Cookiemanager cm = new CookieManager(); URL url = new
 * URL("http://www.hccp.org/test/cookieTest.jsp");
 * 
 * . . .
 * 
 * // getting cookies: URLConnection conn = url.openConnection();
 * conn.connect();
 * 
 * // setting cookies cm.storeCookies(conn);
 * cm.setCookies(url.openConnection());
 * 
 * @author Ian Brown
 * 
 **/
@SuppressWarnings({"rawtypes", "unchecked"})
public class CookieManager {

	private Map store;

	private static final String SET_COOKIE = "Set-Cookie";
	private static final String COOKIE_VALUE_DELIMITER = ";";
	private static final String PATH = "path";
	private static final String EXPIRES = "expires";
	private static final String DATE_FORMAT = "EEE, dd-MMM-yyyy hh:mm:ss z";
	private static final String SET_COOKIE_SEPARATOR = "; ";
	private static final String COOKIE = "Cookie";

	private static final char NAME_VALUE_SEPARATOR = '=';
	private static final char DOT = '.';

	private DateFormat dateFormat;

	public CookieManager() {
		store = new HashMap();
		dateFormat = new SimpleDateFormat(DATE_FORMAT, new Locale("iw"));
	}

	public synchronized void storeCookie(URLConnection conn, String cookie) {
		Map domainStore = getDomainStore(conn);
		
		// OK, now we are ready to get the cookies out of the URLConnection
		addCookieToStore(domainStore, cookie);
		
		Log.i("litvin", "Added cookie [" + cookie + "]");
	}
	
	/**
	 * Retrieves and stores cookies returned by the host on the other side of
	 * the the open java.net.URLConnection.
	 * 
	 * The connection MUST have been opened using the connect() method or a
	 * IOException will be thrown.
	 * 
	 * @param conn
	 *            a java.net.URLConnection - must be open, or IOException will
	 *            be thrown
	 * @throws java.io.IOException
	 *             Thrown if conn is not open.
	 */
	public synchronized void storeCookies(URLConnection conn) throws IOException {
		Map domainStore = getDomainStore(conn);
		
		// OK, now we are ready to get the cookies out of the URLConnection

		String headerName = null;
		for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
			if (headerName.equalsIgnoreCase(SET_COOKIE)) {
				addCookieToStore(domainStore, conn.getHeaderField(i));
			}
		}
	}

	private Map getDomainStore(URLConnection conn) {
		// let's determine the domain from where these cookies are being sent
		String domain = getDomainFromHost(conn.getURL().getHost());

		Map domainStore = null; // this is where we will store cookies for this domain

		// now let's check the store to see if we have an entry for this domain
		if (store.containsKey(domain)) {
			// we do, so lets retrieve it from the store
			domainStore = (Map) store.get(domain);
		} else {
			// we don't, so let's create it and put it in the store
			domainStore = new HashMap();
			store.put(domain, domainStore);
		}
		
		return domainStore;
	}
	
	private void addCookieToStore(Map domainStore, String field) {
		Map cookie = new HashMap();
		StringTokenizer st = new StringTokenizer(
				field, COOKIE_VALUE_DELIMITER);

		// the specification dictates that the first name/value pair
		// in the string is the cookie name and value, so let's handle
		// them as a special case:

		boolean isFirst = true;
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			String name, value;
			int sep = token.indexOf(NAME_VALUE_SEPARATOR);
			if (sep < 0) {
				name = token;
				value = "";
			} else {
				name = token.substring(0, sep);
				value = token.substring(sep + 1, token.length());
			}
			cookie.put(name, value);
			if (isFirst) {
				domainStore.put(name, cookie);
				isFirst = false;
			}
		}

	}
	
	
	/**
	 * Prior to opening a URLConnection, calling this method will set all
	 * unexpired cookies that match the path or subpaths for thi underlying URL
	 * 
	 * The connection MUST NOT have been opened method or an IOException will be
	 * thrown.
	 * 
	 * @param conn
	 *            a java.net.URLConnection - must NOT be open, or IOException
	 *            will be thrown
	 * @throws java.io.IOException
	 *             Thrown if conn has already been opened.
	 */
	public synchronized void setCookies(URLConnection conn) throws IOException {

		// let's determine the domain and path to retrieve the appropriate
		// cookies
		URL url = conn.getURL();
		String domain = getDomainFromHost(url.getHost());
		String path = url.getPath();

		Map domainStore = (Map) store.get(domain);
		if (domainStore == null)
			return;
		StringBuffer cookieStringBuffer = new StringBuffer();

		Iterator cookieNames = domainStore.keySet().iterator();
		while (cookieNames.hasNext()) {
			String cookieName = (String) cookieNames.next();
			Map cookie = (Map) domainStore.get(cookieName);
			// check cookie to ensure path matches and cookie is not expired
			// if all is cool, add cookie to header string
			if (comparePaths((String) cookie.get(PATH), path)
					&& isNotExpired((String) cookie.get(EXPIRES))) {
				cookieStringBuffer.append(cookieName);
				cookieStringBuffer.append("=");
				cookieStringBuffer.append((String) cookie.get(cookieName));
				if (cookieNames.hasNext())
					cookieStringBuffer.append(SET_COOKIE_SEPARATOR);
			}
		}
		try {
			conn.setRequestProperty(COOKIE, cookieStringBuffer.toString());
		} catch (java.lang.IllegalStateException ise) {
			IOException ioe = new IOException(
					"Illegal State! Cookies cannot be set on a URLConnection that is already connected. "
							+ "Only call setCookies(java.net.URLConnection) AFTER calling java.net.URLConnection.connect().");
			throw ioe;
		}
	}

	private String getDomainFromHost(String host) {
		if (host.indexOf(DOT) != host.lastIndexOf(DOT)) {
			return host.substring(host.indexOf(DOT) + 1);
		} else {
			return host;
		}
	}

	private boolean isNotExpired(String cookieExpires) {
		if (cookieExpires == null)
			return true;
		Date now = new Date();
		try {
			return (now.compareTo(dateFormat.parse(cookieExpires))) <= 0;
		} catch (java.text.ParseException pe) {
			pe.printStackTrace();
			return false;
		}
	}

	private boolean comparePaths(String cookiePath, String targetPath) {
		if (cookiePath == null) {
			return true;
		} else if (cookiePath.equals("/")) {
			return true;
		} else if (targetPath.regionMatches(0, cookiePath, 0,
				cookiePath.length())) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Returns a string representation of stored cookies organized by domain.
	 */

	public synchronized String toString() {
		return store.toString();
	}
	/*
	 * public static void main(String[] args) { CookieManager cm = new
	 * CookieManager(); try { URL url = new
	 * URL("http://www.hccp.org/test/cookieTest.jsp"); URLConnection conn =
	 * url.openConnection(); conn.connect(); cm.storeCookies(conn);
	 * System.out.println(cm); cm.setCookies(url.openConnection()); } catch
	 * (IOException ioe) { ioe.printStackTrace(); } }
	 */
}