package com.simulator.service;

import java.net.HttpURLConnection;

public interface HttpService {

	HttpURLConnection get(String url) throws Exception;
}
