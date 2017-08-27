package com.simulator.service.impl;

import java.net.HttpURLConnection;
import java.net.URL;

import com.simulator.service.HttpService;

public class HttpServiceImpl implements HttpService{

	@Override
	public HttpURLConnection get(String url) throws Exception {
		try {
			URL elevationURL = new URL(url);

			HttpURLConnection httpURLConnection = (HttpURLConnection) elevationURL
					.openConnection();
			return httpURLConnection;
		} catch(Exception e) {
			throw e;
		}
	}
}
