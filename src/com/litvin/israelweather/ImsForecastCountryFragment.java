package com.litvin.israelweather;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


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
		
		downloadContent(savedInstanceState);

		return rootView;
	}
	
	
	
	@Override
	void generateSuccessHtml() {
		try {		
			String today, nextDays;
			today = getTextBetween(dlToday.getHtml(), "<div id=\"_ctl0_PageBody_HPforecast1_divDailyForecastIsr\"", "</div>");
			nextDays = getTextBetween(dlToday.getHtml(), "<div id=\"_ctl0_PageBody_HPforecast1_divNextDaysIsr\"", "</div>");
			String forecastHtmlFmt = getActivity().getResources().getString(R.string.forecast_html);
			String head = getActivity().getResources().getString(R.string.html_head);
			html = String.format(forecastHtmlFmt, head, today, nextDays);
			html = html.replaceAll("style=\"display: none;\"", "style=\"display: block;\""); //Bold heading fix
			html = html.replaceAll("style=\"width:27px;", "width=\"55px\" style=\"width:55px;"); //Date column fix
			
			html = html.replaceAll("class=\"HPWarningsBoldText\"", "class=\"HPWarningsBoldText\" style=\"font-weight:bold;\"");
			
			display();
		} catch (NullPointerException ex) {
			Log.w("litvin", ex.toString());
		}

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
	
	void downloadContent(Bundle savedInstanceState) {
		if (savedInstanceState != null)
			html = savedInstanceState.getString(TAG_HTML);
		if (html == null) {
			dlToday = new DownloadHTMLTask(this, ARG_URL_FORECAST_TODAY);
			dlToday.executeOnThreads(urlToday);
			dlNextDays = new DownloadHTMLTask(this, ARG_URL_FORECAST_FEW_DAYS);
			dlNextDays.setHtml("", true);
		} else {
			display();
			progressCircle.setVisibility(View.GONE);
		}
	}
	
	
	public static ImsForecastCountryFragment newInstance(String screenName, String urlHomepage, Bundle state) {
		ImsForecastCountryFragment ret = new ImsForecastCountryFragment();
		ret.setRetainInstance(true);
		Bundle args = new Bundle();
		args.putString(ImageFragment.ARG_SCREEN_NAME, screenName);
		args.putString(ImsForecastCountryFragment.ARG_URL_FORECAST_TODAY, urlHomepage);
		ret.setArguments(args);
		//ret.downloadContent(urlToday, urlNextDays);
		
		return ret;
	}

}