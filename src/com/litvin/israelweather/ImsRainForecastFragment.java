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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;


public class ImsRainForecastFragment extends ImageFragment implements OnItemSelectedListener {

	private static final int NX = 4;
	private static final int NY = 5;
	private static final int IMG_WIDTH = 166+12;
	private static final int IMG_HEIGHT = 370+24;
	private static final int IMG_X0 = 27-6;
	private static final int IMG_Y0 = 218-18;
	private static final int IMG_X_OFFSET = 200;
	private static final int IMG_Y_OFFSET = 400;
	
	private static final int INIT_HOUR_UTC = 0;
	private static final int HOUR_RES = 6;
	
	private int firstImageIdx = -1;
	private SimpleDateFormat dateFormat;
	
	private Bitmap imgs[] = new Bitmap[NX*NY];
	
	public ImsRainForecastFragment() {
		super();
		showImagesProgress = false;
	}

	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		
		spinImages.setOnItemSelectedListener(ImsRainForecastFragment.this);
		
		dateFormat = new SimpleDateFormat("dd/MM", getResources().getConfiguration().locale);
		
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
			spinImages.setVisibility(View.VISIBLE);
			progressBar.setVisibility(View.GONE);
			progressCircle.setVisibility(View.GONE);
			
			// Create an ArrayAdapter using the string array and a default spinner layout
			ArrayList<CharSequence> vals = getLabels();
			ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(getActivity().getBaseContext(), R.layout.spinner_layout, vals);
			// Specify the layout to use when the list of choices appears
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			// Apply the adapter to the spinner
			spinImages.setAdapter(adapter);
			spinImages.setSelection(0);
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
			} else {
				imgs[linIdx] = errorBitmap;
			}
		}
		return imgs[linIdx];
	}

	private boolean isValid(Bitmap img) {
		/*//For testing, uncomment this and find a suitable pixel. You can also limit the image size in getImage().
		Log.i("litvin", img.toString());
		for (int x = 0; x < Math.min(50, img.getWidth()); x++) {
			for (int y = 0; y < Math.min(50, img.getHeight()); y++) {
				int px = img.getPixel(x, y);
				Log.i("litvin", "px(" + x + "," + y + ")=" + Integer.toHexString(px) + "=" + px);
			}
		}
		//*/
		int px = img.getPixel(8, 20);
		boolean ret = (px!=0xffffffff);
		//Log.i("litvin", "px=" + "=" + Integer.toHexString(px)+ px + ";" + ret);
		return ret;
	}
	
	private ArrayList<CharSequence> getLabels() {
		getFirstImageIdx(); //sets firstImageIdx
		
		Calendar calNow = Calendar.getInstance();
		long now = calNow.getTimeInMillis();
		TimeZone tz = calNow.getTimeZone();
		long initial = now;
		calNow.setTimeZone(TimeZone.getTimeZone("UTC"));
		if (firstImageIdx >= 2 && calNow.get(Calendar.HOUR_OF_DAY) < 12)
			initial -= 1000*60*60*24; //Subtract day
		
		Calendar calText = new GregorianCalendar(tz);
		
		ArrayList<CharSequence> vals = new ArrayList<CharSequence>(NX*NY-firstImageIdx);
		for (int i = 0; i < NX*NY-1; i++) {
			int dayIndex = (i+firstImageIdx+1) / NX;
			int dayOffset = 1000*60*60*24 * dayIndex;
			int hourIndex = (i+firstImageIdx) % NX;
			int hour = INIT_HOUR_UTC + hourIndex*HOUR_RES + tz.getRawOffset()/(1000*60*60);
			calText.setTimeInMillis(initial + dayOffset);
			String date = dateFormat.format(calText.getTime()) + " " + hour%24 + ":00-" + (hour+HOUR_RES)%24 + ":00";
			vals.add(date);
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
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		int off = pos + firstImageIdx;
		if (dl[0].isFinished()) {
			//imgView.setImageBitmap(dl[progress].getBitmap(errorBitmap));
			Bitmap img = getImage(off%NX,off/NX);
			if (img != null)
				imgView.setImageBitmap(img);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}
	
}
