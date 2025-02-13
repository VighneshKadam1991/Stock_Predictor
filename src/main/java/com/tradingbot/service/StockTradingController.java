package com.tradingbot.service;

import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.lambda.*;
import software.amazon.awssdk.services.lambda.model.*;
import org.json.JSONObject;
import java.util.*;

@RestController
@RequestMapping("/stocks")
public class StockTradingController {

    private static final String LAMBDA_FUNCTION_NAME = "StockPredictionFunction";

    @GetMapping("/predict")
    public Map<String, Object> predictStock(@RequestParam String symbol) {
        try {
            // Fetch stock price (can integrate Alpha Vantage, Binance, etc.)
            double stockPrice = StockDataFetcher.fetchStockPrice(symbol);

            // Call AWS Lambda for ML-based buy/sell decision
            double buySellSignal = callLambdaForPrediction(symbol, stockPrice);

            // Decision logic (buy if > 0.8, sell if < 0.2)
            String recommendation = buySellSignal > 0.8 ? "BUY" : (buySellSignal < 0.2 ? "SELL" : "HOLD");

            // Response
            Map<String, Object> response = new HashMap<>();
            response.put("symbol", symbol);
            response.put("price", stockPrice);
            response.put("confidence_score", buySellSignal);
            response.put("recommendation", recommendation);
            return response;

        } catch (Exception e) {
            return Collections.singletonMap("error", e.getMessage());
        }
    }

    // Call AWS Lambda for ML Prediction
    private double callLambdaForPrediction(String symbol, double price) {
        AWSLambdaClient lambdaClient = AWSLambdaClient.builder().build();
        String payload = new JSONObject().put("symbol", symbol).put("price", price).toString();

        InvokeRequest request = InvokeRequest.builder()
                .functionName(LAMBDA_FUNCTION_NAME)
                .payload(payload)
                .build();
        InvokeResponse response = lambdaClient.invoke(request);

        String result = response.payload().asUtf8String();
        return new JSONObject(result).getDouble("buy_probability");
    }
}