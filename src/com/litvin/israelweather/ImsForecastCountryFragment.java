package com.litvin.israelweather;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;

public class ImsForecastCountryFragment extends ImsForecastFragment {
	/**
	 * The fragment argument representing the section number for this
	 * fragment.
	 */
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = super.onCreateView(inflater, container, savedInstanceState);
		spinCities.setVisibility(View.GONE);
		
		downloadContent();

		return rootView;
	}
	
	
	
	@Override
	void generateSuccessHtml() {
		String today, nextDays;
		today = getTextBetween(dlToday.getHtml(), "<div id=\"_ctl0_PageBody_HPforecast1_divDailyForecastIsr\"", "</div>");
		nextDays = getTextBetween(dlToday.getHtml(), "<div id=\"_ctl0_PageBody_HPforecast1_divNextDaysIsr\"", "</div>");
		String forecastHtmlFmt = getActivity().getResources().getString(R.string.forecast_html);
		html = String.format(forecastHtmlFmt, today, nextDays);
		html = html.replaceAll("style=\"display: none;\"", "style=\"display: block;\"");
		html = html.replaceAll("class=\"HPWarningsBoldText\"", "class=\"HPWarningsBoldText\" style=\"font-weight:bold;\"");
		
		display();
	}


	private String getTextBetween(String html, String starting, String ending) {
		int start = html.indexOf(starting);
		if (start < 0)
			return "";
		int end = html.indexOf(ending, start);
		if (end < 0)
			return "";
		html = html.substring(start, end+ending.length());
		return html;
	}
	
	void downloadContent() {
		//TODO load selection from state or user store
		dlToday = new DownloadHTMLTask(this, ARG_URL_FORECAST_TODAY);
		dlToday.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, urlToday);
		dlNextDays = new DownloadHTMLTask(this, ARG_URL_FORECAST_FEW_DAYS);
		dlNextDays.setHtml("", true);
	}
	
	
	public static ImsForecastCountryFragment newInstance(String urlHomepage, Bundle state) {
		ImsForecastCountryFragment ret = new ImsForecastCountryFragment();
		ret.setRetainInstance(true);
		Bundle args = new Bundle();
		args.putString(ImsForecastCountryFragment.ARG_URL_FORECAST_TODAY, urlHomepage);
		ret.setArguments(args);
		//ret.downloadContent(urlToday, urlNextDays);
		
		return ret;
	}

}