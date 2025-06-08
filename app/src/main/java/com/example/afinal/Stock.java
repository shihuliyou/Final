package com.example.afinal;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * 股票实体
 */
@Entity(tableName = "stocks")
public class Stock {
    /** 股票代码，主键 */
    @PrimaryKey
    @NonNull
    private String symbol;

    /** 股票名称 */
    private String name;

    /** 最新价格 */
    private double price;

    /** Room 要求的无参构造器 */
    public Stock() { }

    /**
     * 应用中使用的构造器——标注 @Ignore 以免 Room 也将其当作可选构造器。
     */
    @Ignore
    public Stock(@NonNull String symbol, String name, double price) {
        this.symbol = symbol;
        this.name   = name;
        this.price  = price;
    }

    // --- Getter & Setter ---

    @NonNull
    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(@NonNull String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
