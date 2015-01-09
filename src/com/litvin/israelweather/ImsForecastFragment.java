package com.litvin.israelweather;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.Spinner;

public abstract class ImsForecastFragment extends Fragment implements DownloadTask.Callback<String>  {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_SCREEN_NAME = "screen_name";
	
	public static final String ARG_URL_FORECAST_TODAY = "urlForecastToday";
	public static final String ARG_URL_FORECAST_FEW_DAYS = "urlForecastNextDays";
	
	public static final String TAG_HTML = "html";
	
	private String screenName;
	
	protected ProgressBar progressCircle;
	protected WebView webView;
	protected Spinner spinCities;
	
	protected DownloadHTMLTask dlToday, dlNextDays;
	protected String urlToday, urlNextDays;
	protected String html;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_ims_forecast,
				container, false);

		screenName = getArguments().getString(ARG_SCREEN_NAME);
		
		urlToday = getArguments().getString(ARG_URL_FORECAST_TODAY);
		urlNextDays = getArguments().getString(ARG_URL_FORECAST_FEW_DAYS);

		progressCircle = (ProgressBar) rootView.findViewById(R.id.progressCircle2);
		progressCircle.setVisibility(View.VISIBLE);
		
		webView = (WebView) rootView.findViewById(R.id.webView);
		spinCities = (Spinner) rootView.findViewById(R.id.spinCities);

		return rootView;
	}
	
	abstract void downloadContent(Bundle savedInstanceState);
	
	protected void generateSuccessHtml() {
		try {
			String forecastHtmlFmt = getActivity().getResources().getString(R.string.forecast_html);
			String head = getActivity().getResources().getString(R.string.html_head);
			html = String.format(forecastHtmlFmt, head, dlToday.getHtml(), dlNextDays.getHtml());
			display();
		} catch (NullPointerException ex) {
			Log.w("litvin", ex.toString());
		}
	}
	
	protected void generateFailureHtml() {
		try {
			String head = getActivity().getResources().getString(R.string.html_head);
			String errHtmlFmt = getActivity().getResources().getString(R.string.ims_forecast_error);
			html = String.format(errHtmlFmt, head);
			display();
			
			EasyTracker tracker = EasyTracker.getInstance(getActivity());
			tracker.send(MapBuilder.createEvent("data_display", "failure", "html", null).build());
		} catch (NullPointerException ex) {
			Log.w("litvin", ex.toString());
		}
	}
	
	protected void display() {
		//webView.loadUrl("about:blank");
		webView.loadDataWithBaseURL("http://www.ims.gov.il/", html, "text/html", null, null);
	}



	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(TAG_HTML, html);
	}

	
	@Override
	public void downloadComplete(String result, boolean success, Object id) {
		if (!success) {
			generateFailureHtml();
			progressCircle.setVisibility(View.GONE);
		} else if (dlToday.getHtml() != null && dlNextDays.getHtml() != null) {
			generateSuccessHtml();
			progressCircle.setVisibility(View.GONE);
		}
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

}