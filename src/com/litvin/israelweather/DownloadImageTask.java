package com.litvin.israelweather;

import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

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
	        	URLConnection conn = url.openConnection();
	        	conn.setReadTimeout(3000);
	        	InputStream in = conn.getInputStream();
	            bm = BitmapFactory.decodeStream(in);
	            success = true;
	        } catch (SocketTimeoutException e) {
	        	retries--;
	        } catch (Exception e) {
	        	String err = "Error downloading " + urldisplay
	        						+ ((e.getMessage()==null)?("Unkown error: " + e.toString()):e.getMessage());
	            Log.e("Error", err);
	            e.printStackTrace();
	            break;
	        }
        }
        finished = true;
        
    	synchronized (MAX_CONCURRENT) {
			concurrent--;
			MAX_CONCURRENT.notify();
		}

        return bm;
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
