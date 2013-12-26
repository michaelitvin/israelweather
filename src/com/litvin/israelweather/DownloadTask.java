package com.litvin.israelweather;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;


public abstract class DownloadTask<Result> extends AsyncTask<String, Void, Result> {
	
	public static final int MAX_RETRIES = 50;
	public static final int TIMEOUT = 3000;
	
	public static final Integer MAX_CONCURRENT = 14;
	private static int concurrent = 0;
	
    protected Callback<Result> callback;
    protected Object id;
    protected boolean success = false;
    protected boolean finished = false;
    protected boolean cancelled = false;
    
    public DownloadTask(Callback<Result> callback, Object id) {
    	setCallbackAndId(callback, id);
    }
    
    public void setCallbackAndId(Callback<Result> callback, Object id) {
        this.callback = callback;
        this.id = id;
    }
    
    protected abstract Result decode(byte[] byteBuf);

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void executeOnThreads(String... params) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
      }
      else {
        execute(params);
      }
    }
    
    protected Result doInBackground(String... urls) {
    	 synchronized (MAX_CONCURRENT) {
    	 
			try {
				while (concurrent >= MAX_CONCURRENT)
					MAX_CONCURRENT.wait();
				concurrent++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}

        String urldisplay = urls[0];
        success = false;
        int retries = MAX_RETRIES;
        Result res = null;
        while (!success && retries > 0 && !cancelled) {
	        try {
	        	ByteBuffer buf = getAsByteArray(urldisplay, TIMEOUT);
	        	byte[] bufArr = buf.array();
	        	res = decode(bufArr);
	        	if (res != null)
	            	success = true;
	            else {
	            	retries--;
		        	String logMsg = "Couldn't decode " + urldisplay + "; retries=" + retries;
		        	Log.w("litvin", logMsg);
	            }
	        } catch (SocketTimeoutException e) {
	        	retries--;
	        	/*
	        	String logMsg = "Couldn't download " + urldisplay + "retries=" + retries + ";"
	        			+ ((e.getMessage()==null)?(("[Unkown error: " + e.toString()) + "]"):("["+e.getMessage())+"]");
	        	Log.w("litvin", logMsg);
	        	//*/
	        } catch (SocketException e) {
	        	retries--;
	        	//*
	        	String logMsg = "Couldn't download " + urldisplay + "retries=" + retries + ";"
	        			+ ((e.getMessage()==null)?(("[Unkown error: " + e.toString()) + "]"):("["+e.getMessage())+"]");
	        	Log.w("litvin", logMsg);
	        	//*/
	        } catch (Exception e) {
	        	retries--;
	        	String logMsg = "Error downloading " + urldisplay
	        						+ ((e.getMessage()==null)?(("[Unkown error: " + e.toString()) + "]"):("["+e.getMessage())+"]");
	            Log.e("litvin", logMsg);
	            e.printStackTrace();
	            //break;
	        }
        }
        finished = true;
        /*
    	String logMsg = "Done with " + urldisplay + "retries=" + retries + ";success=" + success;
    	Log.i("litvin", logMsg);
    	//*/
        
    	synchronized (MAX_CONCURRENT) {
			concurrent--;
			MAX_CONCURRENT.notify();
		}

        return res;
    }
    
    static protected CookieManager cookieManager = new CookieManager();
    
    private static ByteBuffer getAsByteArray(String urldisplay, int timeout) throws IOException {
    	URL url = new URL(urldisplay);
        URLConnection connection = url.openConnection();
        cookieManager.setCookies(connection);
        // Set the timeout
        connection.setReadTimeout(timeout);
        connection.setDoOutput(false);
        connection.connect();
        cookieManager.storeCookies(connection);
        
        
        // Since you get a URLConnection, use it to get the InputStream
        InputStream in = new BufferedInputStream(connection.getInputStream(),16384);
        // Now that the InputStream is open, get the content length
        
        int contentLength = connection.getContentLength();
        
        // To avoid having to resize the array over and over and over as
        // bytes are written to the array, provide an accurate estimate of
        // the ultimate size of the byte array
        ByteArrayOutputStream tmpOut;
        if (contentLength != -1) {
            tmpOut = new ByteArrayOutputStream(contentLength);
        } else {
            tmpOut = new ByteArrayOutputStream(131072); // Pick some appropriate size
        }

        byte[] buf = new byte[16384];
        while (true) {
            int len = in.read(buf);
            if (len == -1) {
                break;
            }
            tmpOut.write(buf, 0, len);
        }
        byte[] array = tmpOut.toByteArray();
        
        in.close();
        tmpOut.close(); // No effect, but good to do anyway to keep the metaphor alive

        if (array.length < 1024) { //Check if it's a cookie request
        	String content = new String(array);
        	Pattern pattern = Pattern.compile("document\\s*\\.\\s*cookie\\s*=\\s*'([^']+)'");
        	Matcher matcher = pattern.matcher(content);
        	while (matcher.find()) {
        		String cookie;
        		if (matcher.groupCount() == 1) {
        			cookie = matcher.group(1);
        			cookieManager.storeCookie(connection, cookie);
        		}
        	}
        }

        return ByteBuffer.wrap(array);
    }
    
	protected void onPostExecute(Result res) {
    	if (callback != null && !cancelled)
    		callback.downloadComplete(res, success, id);
    }
    
    public boolean isSuccess() {
		return success;
	}

    public boolean isFinished() {
		return finished;
	}
    
    public void cancel() {
    	cancelled = true;
    }

    public interface Callback<Result> {
    	public void downloadComplete(Result res, boolean success, Object id);
    }

}
