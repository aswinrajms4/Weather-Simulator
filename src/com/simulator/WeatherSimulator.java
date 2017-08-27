package com.simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.simulator.service.impl.HttpServiceImpl;

public class WeatherSimulator {

	private static final String[] LOCATIONS = { "Sydney", "Hobart", "Brisbane",
			"London", "Melbourne", "Perth", "Cairns", "Busselton", "Adelaide",
			"Paris" };
	private static String weatherAPIKey;
	private static String elevationAPIKey;
	private static final String WEATHER_API = "http://api.openweathermap.org/data/2.5/weather?q=%s&APPID=%s";
	private static final String ELEVATION_API = "https://maps.googleapis.com/maps/api/elevation/json?locations=%s,%s&key=%s";
	private static final String TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private static final String OUTPUT_DATA = "%s|%s,%s,%s|%s|%s|%s|%s|%s\n";
	private static final float TEMPERATURE_CONVERSION_FACTOR = 273.15F;
	private static final long DATA_FEED_INTERVAL = 500;
	private HttpServiceImpl httpServiceImpl = new HttpServiceImpl();

	static {
		weatherAPIKey = System.getenv("WEATHER_API_KEY");
		elevationAPIKey = System.getenv("ELEVATION_API_KEY");
	}

	static int randomInt(int max) {
		Random randomId = new Random();
		int randomInt = randomId.nextInt(max);
		return randomInt;
	}

	static String fetchUrlLocations() {
		int randomInt = randomInt(LOCATIONS.length);
		return String.format(WEATHER_API, LOCATIONS[randomInt], weatherAPIKey);
	}

	/**
	 * Method to fetch the weather details of a location
	 * 
	 * @throws MalformedURLException
	 * @throws JSONException
	 * @throws IOException
	 */
	private void fetchLocationWeather() throws MalformedURLException,
			JSONException, IOException, Exception {

		StringBuffer weatherResultJSON = new StringBuffer();
		HttpURLConnection httpURLConnection = httpServiceImpl.get(fetchUrlLocations());

		if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

			InputStreamReader inputStreamReader = new InputStreamReader(
					httpURLConnection.getInputStream());
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader, 8192);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				weatherResultJSON.append(line);
			}

			if(inputStreamReader != null) {
				inputStreamReader.close();
			}
			
			if(bufferedReader != null) {
				bufferedReader.close();
			}

			String weatherResult = parseResult(weatherResultJSON.toString());

			System.out.println(weatherResult);

		} else {
			invalidConnection(httpURLConnection.getResponseCode());
		}

	}

	private String parseResult(String json) throws JSONException,
			MalformedURLException, IOException, Exception {

		JSONObject jsonObject = new JSONObject(json);

		// fetch the coordinates for longitude and latitude
		JSONObject JSONObjectCoord = jsonObject.getJSONObject("coord");
		Double lon = JSONObjectCoord.getDouble("lon");
		Double lat = JSONObjectCoord.getDouble("lat");

		// To fetch the elevation
		Double elevation = round(fetchElevation(lon, lat));

		// To fetch the weather details
		String weather = null;
		JSONArray weatherArray = jsonObject.getJSONArray("weather");
		if (weatherArray.length() > 0) {
			JSONObject jsonObjectWeather = weatherArray.getJSONObject(0);
			weather = jsonObjectWeather.getString("main");
		}
		// To fetch the temperature details
		JSONObject JSONObjectMain = jsonObject.getJSONObject("main");
		Double temp = JSONObjectMain.getDouble("temp");
		Double tempInCelsius = round(temp - TEMPERATURE_CONVERSION_FACTOR);
		Double pressure = JSONObjectMain.getDouble("pressure");
		Double humidity = JSONObjectMain.getDouble("humidity");

		// To fetch the local time and convert to ISO8601
		int localTime = jsonObject.getInt("dt");
		String ISOTime = getISO8601Time(localTime);
		String city = jsonObject.getString("name");

		return String.format(OUTPUT_DATA, city, lat, lon, elevation, ISOTime,
				weather, tempInCelsius, pressure, humidity);

	}

	/**
	 * Method to fetch the elevation for a location
	 * 
	 * @param longitude
	 * @param lattitude
	 * @return double
	 * @throws Exception
	 */
	private Double fetchElevation(double longitude, double lattitude) throws Exception{
		StringBuffer elevationResult = new StringBuffer();
		Double elevationHeight = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader bufferedReader = null;
		try {
			HttpURLConnection httpURLConnection = httpServiceImpl.get(String.format(ELEVATION_API, lattitude,
					longitude, elevationAPIKey));

			if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

				inputStreamReader = new InputStreamReader(
						httpURLConnection.getInputStream());
				bufferedReader = new BufferedReader(inputStreamReader, 8192);
				String line = null;
				while ((line = bufferedReader.readLine()) != null) {
					elevationResult.append(line);
				}

				JSONObject jsonObject = new JSONObject(
						elevationResult.toString());
				JSONArray jsonArrayElevation = jsonObject
						.getJSONArray("results");
				if (jsonArrayElevation.length() > 0) {
					JSONObject jsonObjectElevation = jsonArrayElevation
							.getJSONObject(0);
					elevationHeight = jsonObjectElevation
							.getDouble("elevation");

				} else {
					System.out.println("Failed to fetch the elevation as the response JSON is invalid.");
				}

			} else {
				invalidConnection(httpURLConnection.getResponseCode());
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch (IOException e) {

				}
			}

			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {

				}
			}
		}
		return elevationHeight;
	}

	/**
	 * Method to convert time in UTC to ISO8601
	 * 
	 * @param resultDate
	 * @return String
	 */
	private String getISO8601Time(int resultDate) {
		TimeZone timeZone = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat(TIME_FORMAT);
		df.setTimeZone(timeZone);
		Date dateTime = new Date((long) resultDate * 1000);
		return df.format(dateTime);
	}

	/**
	 * Method to round the values
	 * 
	 * @param value
	 * @return double
	 */
	private Double round(Double value) {
		return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP)
				.doubleValue();
	}

	private void invalidConnection(int responseCode) {
		if (responseCode == 401 || responseCode == 403) {
			System.out.println("Invalid Key!!!Check API key!!");
		} else {
			System.out
					.println("Error in HTTP connection");
		}
	}

	public static void main(String[] args) throws Exception {
		if (weatherAPIKey == null || weatherAPIKey.isEmpty()) {
			throw new Exception(
					"Could not get the Weather API Key. Please set the environment varibale 'WEATHER_API_KEY'");
		}

		if (elevationAPIKey == null || elevationAPIKey.isEmpty()) {
			throw new Exception(
					"Could not get the Elevation API Key. Please set the environment varibale 'ELEVATION_API_KEY'");
		}

		WeatherSimulator weathersimuator = new WeatherSimulator();
		try {
			for (int i = 0; i <= 100; i++) {
				weathersimuator.fetchLocationWeather();
				Thread.sleep(DATA_FEED_INTERVAL);
			}

		} catch (MalformedURLException ex) {
			Logger.getLogger(WeatherSimulator.class.getName()).log(
					Level.SEVERE, ex.toString(), ex);
		} catch (IOException ex) {
			Logger.getLogger(WeatherSimulator.class.getName()).log(
					Level.SEVERE, ex.toString(), ex);
		} catch (JSONException ex) {
			Logger.getLogger(WeatherSimulator.class.getName()).log(
					Level.SEVERE, ex.toString(), ex);
		} catch (Exception ex) {
			Logger.getLogger(WeatherSimulator.class.getName()).log(
					Level.SEVERE, ex.toString(), ex);
		}

	}
}
