package com.example.afinal;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * 用户自选股关联表的 DAO，管理用户与股票的多对多关系
 */
@Dao
public interface WatchlistDao {
    /**
     * 将一条自选股记录插入 watchlist 表，若已存在则忽略
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertEntry(WatchlistEntry entry);

    /**
     * 删除指定用户下某只股票的自选记录
     */
    @Query("DELETE FROM watchlist WHERE userId = :userId AND stockSymbol = :symbol")
    void deleteEntry(int userId, String symbol);

    /**
     * 查询某个用户的所有自选股票详情
     */
    @Query("SELECT s.* FROM stocks s " +
            "INNER JOIN watchlist w ON s.symbol = w.stockSymbol " +
            "WHERE w.userId = :userId")
    List<Stock> getStocksForUser(int userId);
}
