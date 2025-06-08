package com.example.afinal;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface HistoricalPriceDao {

    /** 插入多条历史收盘价，已有的记录则替换 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<HistoricalPrice> prices);

    /** 读取某只股票最近 3 个交易日的收盘价 */
    @Query("SELECT * FROM historical_price WHERE symbol = :symbol ORDER BY date DESC LIMIT 3")
    List<HistoricalPrice> loadLast3(String symbol);
}
