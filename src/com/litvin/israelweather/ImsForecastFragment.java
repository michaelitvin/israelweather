package com.litvin.israelweather;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Spinner;

public abstract class ImsForecastFragment extends Fragment implements DownloadTask.Callback<String>  {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	public static final String ARG_URL_FORECAST_TODAY = "urlForecastToday";
	public static final String ARG_URL_FORECAST_FEW_DAYS = "urlForecastNextDays";
	
	public static final String TAG_HTML = "html";
	
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

		urlToday = getArguments().getString(ARG_URL_FORECAST_TODAY);
		urlNextDays = getArguments().getString(ARG_URL_FORECAST_FEW_DAYS);

		progressCircle = (ProgressBar) rootView.findViewById(R.id.progressCircle);
		progressCircle.setVisibility(View.VISIBLE);
		
		webView = (WebView) rootView.findViewById(R.id.webView);
		spinCities = (Spinner) rootView.findViewById(R.id.spinCities);

		return rootView;
	}
	
	abstract void downloadContent(Bundle savedInstanceState);
	
	void generateSuccessHtml() {
		try {
			String forecastHtmlFmt = getActivity().getResources().getString(R.string.forecast_html);
			String head = getActivity().getResources().getString(R.string.html_head);
			html = String.format(forecastHtmlFmt, head, dlToday.getHtml(), dlNextDays.getHtml());
			display();
		} catch (NullPointerException ex) {
			Log.w("litvin", ex.toString());
		}
	}
	
	void generateFailureHtml() {
		try {
			String head = getActivity().getResources().getString(R.string.html_head);
			String errHtmlFmt = getActivity().getResources().getString(R.string.ims_forecast_error);
			html = String.format(errHtmlFmt, head);
			display();
		} catch (NullPointerException ex) {
			Log.w("litvin", ex.toString());
		}
	}
	
	void display() {
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
	
}