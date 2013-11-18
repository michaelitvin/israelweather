package com.litvin.israelweather;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;


public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> implements Parcelable {
	
	public static final Integer MAX_CONCURRENT = 14;
	private static int concurrent = 0;
	
    private Bitmap bm = null;
    private Callback callback;
    private Object id;
    private boolean success = false;
    private boolean finished = false;
    private boolean cancelled = false;

    public DownloadImageTask(Callback callback, Object id) {
    	setCallbackAndId(callback, id);
    }
    
    public void setCallbackAndId(Callback callback, Object id) {
        this.callback = callback;
        this.id = id;
    }

    protected Bitmap doInBackground(String... urls) {
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
        int retries = 10;
        while (!success && retries > 0 && !cancelled) {
	        try {
	        	URL url = new URL(urldisplay);
	        	ByteBuffer buf = getAsByteArray(url, 3000);
	        	byte[] bufArr = buf.array();
	            bm = BitmapFactory.decodeByteArray(bufArr, 0, bufArr.length);
	            if (bm != null)
	            	success = true;
	            else {
	            	retries--;
		        	String logMsg = "Couldn't decode image from " + urldisplay;
		        	Log.w("litvin", logMsg);
	            }
	        } catch (SocketTimeoutException e) {
	        	retries--;
	        	String logMsg = "Couldn't download " + urldisplay + "retries=" + retries + ";"
						+ ((e.getMessage()==null)?("Unkown error: " + e.toString()):e.getMessage());
	        	//Log.w("litvin", logMsg);
	        } catch (Exception e) {
	        	String logMsg = "Error downloading " + urldisplay
	        						+ ((e.getMessage()==null)?("Unkown error: " + e.toString()):e.getMessage());
	            Log.e("litvin", logMsg);
	            e.printStackTrace();
	            break;
	        }
        }
        finished = true;
    	String logMsg = "Done with " + urldisplay + "retries=" + retries + ";success=" + success;
    	//Log.i("litvin", logMsg);
        
    	synchronized (MAX_CONCURRENT) {
			concurrent--;
			MAX_CONCURRENT.notify();
		}

        return bm;
    }
    
    private static ByteBuffer getAsByteArray(URL url, int timeout) throws IOException {
        URLConnection connection = url.openConnection();
        // Set the timeout
        connection.setReadTimeout(timeout);
        // Since you get a URLConnection, use it to get the InputStream
        InputStream in = connection.getInputStream();
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
        in.close();
        tmpOut.close(); // No effect, but good to do anyway to keep the metaphor alive

        byte[] array = tmpOut.toByteArray();

        return ByteBuffer.wrap(array);
    }
    
    public Bitmap getBitmap(Bitmap def) {
    	if (bm != null) {
    		return bm;
    	} else {
    		return def;
    	}	
	}

    public Bitmap getBitmap() {
		return bm;
	}

	public void setBitmap(Bitmap bm, boolean success) {
		setBitmap(bm);
		this.success = success;
	}
	
    public void setBitmap(Bitmap bm) {
		this.bm = bm;
		success = true;
		finished = true;
		onPostExecute(bm);
	}

	protected void onPostExecute(Bitmap result) {
    	if (callback != null && !cancelled)
    		callback.downloadComplete(result, success, id);
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

    public static interface Callback {
    	public void downloadComplete(Bitmap result, boolean success, Object id);
    }

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeParcelable(bm, flags);
		out.writeBooleanArray(new boolean[] {success, finished});
	}
	
	public DownloadImageTask(Parcel in) {
		bm = in.readParcelable(null);
		boolean[] baTmp = new boolean[2];
		in.readBooleanArray(baTmp);
		success = baTmp[0];
		finished = baTmp[1];
	}
	
	public static final Parcelable.Creator<DownloadImageTask> CREATOR = new Parcelable.Creator<DownloadImageTask>() {

		@Override
		public DownloadImageTask createFromParcel(Parcel in) {
			return new DownloadImageTask(in);
		}

		@Override
		public DownloadImageTask[] newArray(int size) {
			return new DownloadImageTask[size];
		}

	};



    
}
