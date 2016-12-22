package com.litvin.israelweather;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;


public class ImageFragment extends Fragment implements OnTouchListener, OnSeekBarChangeListener, OnClickListener, DownloadImageTask.Callback<Bitmap> {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_SCREEN_NAME = "screen_name";
	
	public static final String ARG_URLS = "urls";
	public static final String ARG_ERROR_BITMAP = "error_bitmap";
	public static final String TAG_NCOMPLETE = "nComplete";
	
	protected ZoomableImageView imgView;
	protected SeekBar seekBar;
	protected ImageButton btnNext, btnPrev;
	protected ProgressBar progressBar;
	protected ProgressBar progressCircle;
	protected TextView textView;

	protected boolean showImagesProgress = true;
	
	protected Bitmap errorBitmap;
	
	protected int nComplete;
	protected int lastDownloadedId = 0;

	protected String screenName;
	
	protected DownloadImageTask[] dl;
	protected String[] urls;
	
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

		screenName = getArguments().getString(ARG_SCREEN_NAME);
		
		urls = getArguments().getStringArray(ARG_URLS);
		errorBitmap = (Bitmap)getArguments().getParcelable(ARG_ERROR_BITMAP);

		seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
		seekBar.setMax(urls.length-1);
		seekBar.setOnSeekBarChangeListener(this);
		
		setSeekBarVisibility(View.GONE, View.GONE);
		progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
		progressBar.setMax(urls.length);
		progressBar.setProgress(0);
		progressBar.setVisibility(View.VISIBLE);
		
		btnPrev = (ImageButton) rootView.findViewById(R.id.btnPrev);
		btnPrev.setOnClickListener(this);
		btnNext = (ImageButton) rootView.findViewById(R.id.btnNext);
		btnNext.setOnClickListener(this);

		//seekBar.setBackgroundColor(0x4422cc33);
		//seekBar.setBackgroundColor(0xffF7FFEF);
		//btnPrev.setBackgroundColor(0xffccff99);
		//btnNext.setBackgroundColor(0xffccff99);
		
		textView = (TextView) rootView.findViewById(R.id.textView);

		progressCircle = (ProgressBar) rootView.findViewById(R.id.progressCircle1);
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
	
	public static ImageFragment newInstance(String screenName, String[] urls, Bundle state, Bitmap errorBitmap) {
		ImageFragment ret = new ImageFragment();
		ret.setRetainInstance(true);
		
		Bundle args = new Bundle();
		args.putString(ImageFragment.ARG_SCREEN_NAME, screenName);
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
		if (showImagesProgress && success && intid > lastDownloadedId) {
			imgView.setImageBitmap(result);
			lastDownloadedId = intid;
		}
		//else
		//	imgView.setImageBitmap(errorBitmap);
		//*/
		if (dl.length == nComplete) {
			if (dl.length > 1)
				setSeekBarVisibility(View.VISIBLE, View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			progressCircle.setVisibility(View.GONE);
			seekBar.setProgress(urls.length-1);
		}
		
		EasyTracker tracker = EasyTracker.getInstance(getActivity());
		tracker.send(MapBuilder.createEvent("data_display", success?"success":"failure", "image", null).build());

	}
	
	protected String getBitmapTag(int i) {
		return "TAG_BM" + i;
	}

	@Override
	public void onStart() {
		Activity activity = getActivity();
		if (activity != null) {
			EasyTracker tracker = EasyTracker.getInstance(activity);
			tracker.set(Fields.SCREEN_NAME, screenName);
			tracker.send(MapBuilder.createAppView().build());
		}
		super.onStart();
	}
	
	protected void setSeekBarVisibility(int seekBarVisiblity, int buttonsVisiblity) {
		if (seekBar != null) {
			seekBar.setVisibility(seekBarVisiblity);
		}
		if (btnPrev != null && btnNext != null) {
			btnPrev.setVisibility(buttonsVisiblity);
			btnNext.setVisibility(buttonsVisiblity);
		}
	}

	@Override
	public void onClick(View v) {
		int val = seekBar.getProgress();
		if (v == btnPrev && val > 0)
			seekBar.setProgress(val-1);
		else if (v == btnNext && val < seekBar.getMax())
			seekBar.setProgress(val+1);
	}

}