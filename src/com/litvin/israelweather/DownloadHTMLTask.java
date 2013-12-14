package com.litvin.israelweather;

public class DownloadHTMLTask extends DownloadTask<String> {

	private String html;
	
	public DownloadHTMLTask(Callback<String> callback, Object id) {
    	super(callback, id);
    }
    
	@Override
	protected String decode(byte[] byteBuf) {
		html = new String(byteBuf);
		if (html.length() > 144 || !html.contains("document.cookie"))
			return html;
		else
			return null;
	}    

	public String getHtml() {
		return html;
	}

}
