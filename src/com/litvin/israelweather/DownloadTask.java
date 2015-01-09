package com.litvin.israelweather;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;

import com.turbomanage.httpclient.AbstractHttpClient;
import com.turbomanage.httpclient.BasicHttpClient;
import com.turbomanage.httpclient.HttpResponse;

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
    
	@TargetApi(Build.VERSION_CODES.GINGERBREAD)
	private static ByteBuffer getAsByteArray(String urldisplay, int timeout) throws IOException {
    	AbstractHttpClient.ensureCookieManager();
    	BasicHttpClient httpClient = new BasicHttpClient(urldisplay);
    	httpClient.setConnectionTimeout(timeout);
    	if (DownloadHTMLTask.cookieHeader != null) {
    		httpClient.addHeader("Cookie", DownloadHTMLTask.cookieHeader);
    	}
    	HttpResponse httpResponse = httpClient.get(null, null);
    	
    	if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD) {
	    	try {
    			AbstractHttpClient.getCookieManager().put(new java.net.URI(urldisplay), httpResponse.getHeaders());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	
    	ByteBuffer ret = ByteBuffer.wrap(httpResponse.getBody());
    	return ret;
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
