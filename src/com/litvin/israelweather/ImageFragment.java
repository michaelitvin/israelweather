package com.litvin.israelweather;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;


public class ImageFragment extends Fragment implements OnTouchListener, OnSeekBarChangeListener, DownloadImageTask.Callback<Bitmap> {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_URLS = "urls";
	public static final String ARG_ERROR_BITMAP = "error_bitmap";
	public static final String TAG_NCOMPLETE = "nComplete";
	
	private ZoomableImageView imgView;
	private SeekBar seekBar;
	private ProgressBar progressBar;
	private ProgressBar progressCircle;
	
	private Bitmap errorBitmap;
	
	private int nComplete;
	private int lastDownloadedId = 0;

	private DownloadImageTask[] dl;
	private String[] urls;
	
	public ImageFragment() {
		urls = new String[0];
		dl = new DownloadImageTask[urls.length];
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_image,
				container, false);

		imgView = (ZoomableImageView) rootView.findViewById(R.id.imageView);
		imgView.setOnTouchListener(this);

		urls = getArguments().getStringArray(ARG_URLS);
		errorBitmap = (Bitmap)getArguments().getParcelable(ARG_ERROR_BITMAP);

		seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(this);
		seekBar.setMax(urls.length-1);
		seekBar.setBackgroundColor(0x4422cc33);
		seekBar.setVisibility(View.GONE);
		progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
		progressBar.setMax(urls.length);
		progressBar.setProgress(0);
		progressBar.setVisibility(View.VISIBLE);

		progressCircle = (ProgressBar) rootView.findViewById(R.id.progressCircle);
		progressCircle.setVisibility(View.VISIBLE);

		return rootView;
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		downloadImages(urls, savedInstanceState);
		super.onActivityCreated(savedInstanceState);
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (dl == null)
			return;
		outState.putInt(TAG_NCOMPLETE, nComplete);
		for (int i = 0; i < dl.length; i++) {
			if (dl[i] != null) {
				if (dl[i].isFinished()) {
					outState.putParcelable(getBitmapTag(i), dl[i]);
				} else {
					dl[i].cancel();
				}
			}
		}
	}

	public void downloadImages(String[] urls, Bundle state) {
		if (dl == null || dl.length != urls.length)
			dl = new DownloadImageTask[urls.length];
		nComplete = 0;
		lastDownloadedId = -1;
		if (state != null)
			nComplete = state.getInt(TAG_NCOMPLETE, 0);
		for (int i = 0; i < urls.length; i++) {
			if (dl[i] == null || !dl[i].isSuccess()) {
				Parcelable parcelable = null;
				if (state != null)
					parcelable = state.getParcelable(getBitmapTag(i));
				dl[i] = (DownloadImageTask)parcelable;
				if (dl[i] != null && dl[i].isSuccess()) {
					dl[i].setCallbackAndId(this, i);
				} else {
					dl[i] = new DownloadImageTask(this, i);
					dl[i].executeOnThreads(urls[i]);
				}
				
			} else {
				
				downloadComplete(dl[i].getBitmap(errorBitmap), true, i);
				
			}
		}
	}
	
	public static ImageFragment newInstance(String[] urls, Bundle state, Bitmap errorBitmap) {
		ImageFragment ret = new ImageFragment();
		ret.setRetainInstance(true);
		
		Bundle args = new Bundle();
		args.putStringArray(ImageFragment.ARG_URLS, urls);
		args.putParcelable(ImageFragment.ARG_ERROR_BITMAP, errorBitmap);
		ret.setArguments(args);

		//ret.downloadImages(urls, state);
		
		return ret;
	}

	@Override
	public boolean onTouch(View view, MotionEvent e) {
		if (imgView != null)
			return imgView.onTouchEvent(e);
		return true;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if (seekBar != null && progress >= dl.length) {
			seekBar.setProgress(0);
		} else if (imgView != null && dl[progress].isFinished()) {
			imgView.setImageBitmap(dl[progress].getBitmap(errorBitmap));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}

	@Override
	public void downloadComplete(Bitmap result, boolean success, Object id) {
		nComplete++;
		if (imgView == null || progressBar == null || seekBar == null)
			return;
		progressBar.setProgress(nComplete);
		Integer intid = (Integer)id;
		//imgView.setImageBitmap(dl[intid].getBitmap(errorBitmap));
		//*
		if (success && intid > lastDownloadedId) {
			imgView.setImageBitmap(result);
			lastDownloadedId = intid;
		}
		//else
		//	imgView.setImageBitmap(errorBitmap);
		//*/
		if (dl.length == nComplete) {
			if (dl.length > 1)
				seekBar.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			progressCircle.setVisibility(View.GONE);
			seekBar.setProgress(urls.length-1);
		}
	}
	
	private String getBitmapTag(int i) {
		return "TAG_BM" + i;
	}

}