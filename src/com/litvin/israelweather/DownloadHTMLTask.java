package com.litvin.israelweather;

import com.turbomanage.httpclient.AbstractHttpClient;

public class DownloadHTMLTask extends DownloadTask<String> {

	private String html;
	public static String cookieHeader = null;
	
	public DownloadHTMLTask(Callback<String> callback, Object id) {
    	super(callback, id);
    }
    
	@Override
	protected String decode(byte[] byteBuf) {
		html = new String(byteBuf);
		if (html.length() > 144 || !html.contains("document.cookie")) {
			return html;
		} else {
			if (html.contains("document.cookie"))
				addCookie(html);
			return null;	
		}
	}
	
	private void addCookie(String html) {
		String str1 = "document.cookie='";
		int begin = html.indexOf(str1) + str1.length();
		String str2 = html.substring(begin);
		cookieHeader = str2.substring(0, str2.indexOf(';'));
		
//		CookieManager cm = AbstractHttpClient.getCookieManager();
//		cm.
	}

	public String getHtml() {
		return html;
	}
	
	public void setHtml(String html, boolean success) {
		this.html = html;
		this.success = success;
		finished = true;
		onPostExecute(html);
	}

}
