package com.litvin.israelweather;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;

public class ImsForecastCitiesFragment extends ImsForecastFragment implements OnItemSelectedListener  {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String PREF_CITY_CODE = "City code";
			
	private String[] cityNames;
	private int[] cityCodes;
	private int cityIdx;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		
		cityCodes = getActivity().getResources().getIntArray(R.array.city_codes);
		cityNames = getActivity().getResources().getStringArray(R.array.city_names);

		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity().getBaseContext(),
		        R.array.city_names, R.layout.spinner_layout);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinCities.setAdapter(adapter);
		SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.GENERAL_PREFS, 0);
	    cityIdx = cityCode2Idx(settings.getInt(PREF_CITY_CODE, getActivity().getResources().getInteger(R.integer.default_city_code)));
		spinCities.setSelection(cityIdx);
		spinCities.setVisibility(View.VISIBLE);
		spinCities.setOnItemSelectedListener(ImsForecastCitiesFragment.this);
		
		downloadContent(savedInstanceState); //This already happens onItemSelected

		return rootView;
	}
	
	
	void downloadContent(Bundle savedInstanceState) {
		if (savedInstanceState != null)
			html = savedInstanceState.getString(TAG_HTML);
		if (html == null) {
		    String imsForecastTodayFmt = String.format(urlToday, cityCodes[cityIdx], cityNames[cityIdx] );
			try {
				imsForecastTodayFmt = String.format(urlToday, cityCodes[cityIdx], URLEncoder.encode(cityNames[cityIdx],"UTF-8") );
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		    String imsForecastNextDaysFmt = String.format(urlNextDays, cityCodes[cityIdx]);
	
			dlToday = new DownloadHTMLTask(this, ARG_URL_FORECAST_TODAY);
			dlToday.executeOnThreads(imsForecastTodayFmt);
			dlNextDays = new DownloadHTMLTask(this, ARG_URL_FORECAST_FEW_DAYS);
			dlNextDays.executeOnThreads(imsForecastNextDaysFmt);
		} else {
			display();
			progressCircle.setVisibility(View.GONE);
		}
	}
	
	
	public static ImsForecastCitiesFragment newInstance(String urlToday, String urlNextDays, Bundle state) {
		ImsForecastCitiesFragment ret = new ImsForecastCitiesFragment();
		ret.setRetainInstance(true);
		Bundle args = new Bundle();
		args.putString(ImsForecastCitiesFragment.ARG_URL_FORECAST_TODAY, urlToday);
		args.putString(ImsForecastCitiesFragment.ARG_URL_FORECAST_FEW_DAYS, urlNextDays);
		ret.setArguments(args);
		//ret.downloadContent(urlToday, urlNextDays);
		
		return ret;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		if (cityIdx == pos)
			return;
		html = null;
		display();
		progressCircle.setVisibility(View.VISIBLE);
		cityIdx = pos;
		downloadContent(null);
		
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = getActivity().getSharedPreferences(MainActivity.GENERAL_PREFS, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(PREF_CITY_CODE, cityIdx2Code(pos));
		// Commit the edits!
		editor.commit();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		
	}
	
	private int cityCode2Idx(int code) {
		for (int i = 0; i < cityCodes.length; i++)
			if (code == cityCodes[i])
				return i;
		return 0;
	}
	
	private int cityIdx2Code(int idx) {
		return cityCodes[idx];
	}
	
}