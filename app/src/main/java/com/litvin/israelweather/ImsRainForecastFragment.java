package com.litvin.israelweather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

public class ImsRainForecastFragment extends ImageFragment {

	private static final int NX = 4;
	private static final int NY = 5;
	private static final int IMG_WIDTH = 162;
	private static final int IMG_HEIGHT = 361;
	private static final int IMG_X0 = 34;
	private static final int IMG_Y0 = 222;
	private static final int IMG_X_OFFSET = 200;
	private static final int IMG_Y_OFFSET = 400;
	
	private static final int INIT_HOUR_UTC = 0;
	private static final int HOUR_RES = 6;
	
	private int firstImageIdx = -1;
	private int closestMap;
	private SimpleDateFormat dateFormat;
	
	private Bitmap imgs[] = new Bitmap[NX*NY];
	private ArrayList<CharSequence> labels;
	
	public ImsRainForecastFragment() {
		super();
		showImagesProgress = false;
	}

	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		
		dateFormat = new SimpleDateFormat("dd/MM", getResources().getConfiguration().locale);
		MainActivity.stopAnnouncingNew(getActivity(), getString(R.string.title_section_rain_forecast));
		
		return rootView;
	}



	public static ImsRainForecastFragment newInstance(String screenName, String[] urls, Bundle state, Bitmap errorBitmap) {
		ImsRainForecastFragment ret = new ImsRainForecastFragment();
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
	public void downloadComplete(Bitmap result, boolean success, Object id) {
		nComplete++;
		if (imgView == null || progressBar == null || seekBar == null)
			return;
		progressBar.setProgress(nComplete);
			
		if (dl.length == nComplete) {
			labels = getLabels();
			seekBar.setMax(labels.size()-1);
			
			progressBar.setVisibility(View.GONE);
			progressCircle.setVisibility(View.GONE);
			textView.setVisibility(View.VISIBLE);
			setSeekBarVisibility(View.VISIBLE, View.VISIBLE);
			
			seekBar.setProgress(closestMap);
		}
	}
	
	private Bitmap getImage(int ix, int iy) {
		int linIdx = ix+iy*NX;
		if (imgs[linIdx] == null) {
			Bitmap img = dl[0].getBitmap(null);
			if (img != null) {
				int x = IMG_X0 + ix*IMG_X_OFFSET;
				int y = IMG_Y0 + iy*IMG_Y_OFFSET;
				imgs[linIdx] = Bitmap.createBitmap(img, x, y, IMG_WIDTH, IMG_HEIGHT);
				//imgs[linIdx] = Bitmap.createBitmap(imgs[linIdx], 7, 19, 5, 5);
				//imgs[linIdx] = Bitmap.createBitmap(imgs[linIdx], 0, 0, 5, 5);
			} else {
				imgs[linIdx] = errorBitmap;
			}
		}
		return imgs[linIdx];
	}

	private boolean isValid(Bitmap img) {
		/*//For testing, uncomment this and find a suitable pixel. You can also limit the image size in getImage().
		android.util.Log.i("litvin", img.toString());
		for (int x = 0; x < Math.min(20, img.getWidth()); x++) {
			for (int y = 0; y < Math.min(20, img.getHeight()); y++) {
				int px = img.getPixel(x, y);
				android.util.Log.i("litvin", "px(" + x + "," + y + ")=" + Integer.toHexString(px) + "=" + px);
			}
		}
		//*/
		//int px = img.getPixel(8, 20);
		int px = img.getPixel(2, 2);
		boolean ret = (px!=0xffffffff);
		//Log.i("litvin", "px=" + "=" + Integer.toHexString(px)+ px + ";" + ret);
		return ret;
	}
	
	private ArrayList<CharSequence> getLabels() {
		getFirstImageIdx(); //sets firstImageIdx
		
		Calendar calNow = Calendar.getInstance();
		long now = calNow.getTimeInMillis();
		int hourNow = calNow.get(Calendar.HOUR_OF_DAY);
		TimeZone tz = calNow.getTimeZone();
		long initial = now;
		calNow.setTimeZone(TimeZone.getTimeZone("UTC"));
		if (firstImageIdx >= 2 && hourNow < 16) {
			initial -= 1000*60*60*24; //Subtract day
			hourNow += 24;
		}
		
		Calendar calText = new GregorianCalendar(tz);
		
		int hourDiff = 9999;
		
		ArrayList<CharSequence> vals = new ArrayList<CharSequence>(NX*NY-firstImageIdx);
		for (int i = firstImageIdx; i < NX*NY-1; i++) {
			int dayIndex = i / NX;
			int dayOffset = 1000*60*60*24 * dayIndex;
			int hourIndex = i % NX;
			int hour = INIT_HOUR_UTC + hourIndex*HOUR_RES + tz.getRawOffset()/(1000*60*60);
			calText.setTimeInMillis(initial + dayOffset);
			String date = dateFormat.format(calText.getTime()) + " " + hour%24 + ":00-" + (hour+HOUR_RES)%24 + ":00";
			vals.add(date);
			
			int newHourDiff = Math.abs(hour+24*dayIndex-hourNow);
			if (newHourDiff < hourDiff) {
				hourDiff = newHourDiff;
				closestMap = i - firstImageIdx;
			}
		}
		return vals;
	}
	
	private int getFirstImageIdx() {
		if (firstImageIdx < 0) {
			firstImageIdx = 0;
			for (int ix = 0; ix < NX; ix++) {
				if (isValid(getImage(ix,0))) {
					firstImageIdx = ix;
					break;
				}
			}
		}
		return firstImageIdx;
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		int max = seekBar.getMax();
		if (imgView != null && dl[0].isFinished() && seekBar != null && max > 0) {
			int off = progress + firstImageIdx;
			if (dl[0].isFinished()) {
				//imgView.setImageBitmap(dl[progress].getBitmap(errorBitmap));
				Bitmap img = getImage(off%NX,off/NX);
				if (img != null)
					imgView.setImageBitmap(img);
				textView.setText(labels.get(progress));
			}
		}
	}

	
}
