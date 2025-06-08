package com.example.afinal;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * 股票表的 DAO，包含插入、更新、删除以及查询方法
 */
@Dao
public interface StockDao {
    /**
     * 插入一条股票记录，若主键冲突则忽略
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertStock(Stock stock);

    /**
     * 批量插入或更新股票记录，若主键冲突则替换
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Stock> stocks);

    /**
     * 更新已有的股票记录
     */
    @Update
    void updateStock(Stock stock);

    /**
     * 根据股票代码查询单只股票
     */
    @Query("SELECT * FROM stocks WHERE symbol = :symbol LIMIT 1")
    Stock getStock(String symbol);

    /**
     * 删除一条股票记录
     */
    @Delete
    void deleteStock(Stock stock);

    /**
     * 查询所有股票列表
     */
    @Query("SELECT * FROM stocks")
    List<Stock> getAllStocks();

    /**
     * 清空 stocks 表，用于在缓存全市场前删除旧数据
     */
    @Query("DELETE FROM stocks")
    void clearAllStocks();
}
