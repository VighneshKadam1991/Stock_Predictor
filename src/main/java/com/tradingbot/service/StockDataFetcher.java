package com.tradingbot.service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import org.json.JSONObject;

public class StockDataFetcher {
    private static final String API_KEY = "YOUR_ALPHAVANTAGE_API_KEY";
    private static final String STOCK_API_URL = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=";

    public static double fetchStockPrice(String symbol) throws Exception {
        URL url = new URL(STOCK_API_URL + symbol + "&interval=5min&apikey=" + API_KEY);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        Scanner scanner = new Scanner(url.openStream());
        StringBuilder response = new StringBuilder();
        while (scanner.hasNext()) {
            response.append(scanner.nextLine());
        }
        scanner.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONObject timeSeries = jsonResponse.getJSONObject("Time Series (5min)");
        String latestTimestamp = timeSeries.keys().next();
        return timeSeries.getJSONObject(latestTimestamp).getDouble("4. close");
    }
}