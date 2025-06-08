package com.example.afinal;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "historical_price", primaryKeys = {"symbol", "date"})
public class HistoricalPrice {
    @NonNull
    public String symbol;

    /** 交易日，格式 "yyyy-MM-dd" */
    @NonNull
    public String date;

    /** 当日收盘价 */
    public double closePrice;

    public HistoricalPrice(@NonNull String symbol, @NonNull String date, double closePrice) {
        this.symbol = symbol;
        this.date = date;
        this.closePrice = closePrice;
    }
}
