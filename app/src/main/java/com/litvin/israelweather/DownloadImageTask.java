package com.litvin.israelweather;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;


public class DownloadImageTask extends DownloadTask<Bitmap> implements Parcelable {
		
    private Bitmap bm = null;

    public DownloadImageTask(Callback<Bitmap> callback, Object id) {
    	super(callback, id);
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
		super(null, -1);
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

	@Override
	protected Bitmap decode(byte[] byteBuf) {
        bm = BitmapFactory.decodeByteArray(byteBuf, 0, byteBuf.length);
		return bm;
	}    
}
