package com.example.afinal;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;

import static androidx.room.ForeignKey.CASCADE;

/**
 * 用户自选股关联表，表示某个用户关注的某只股票
 */
@Entity(
        tableName = "watchlist",
        primaryKeys = {"userId", "stockSymbol"},
        foreignKeys = {
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "id",
                        childColumns = "userId",
                        onDelete = CASCADE
                ),
                @ForeignKey(
                        entity = Stock.class,
                        parentColumns = "symbol",
                        childColumns = "stockSymbol",
                        onDelete = CASCADE
                )
        },
        indices = {
                @Index("userId"),
                @Index("stockSymbol")
        }
)
public class WatchlistEntry {
    // 用户ID（外键，关联 User.id）
    public int userId;

    // 股票代码（外键，关联 Stock.symbol）
    @NonNull
    public String stockSymbol;

    // 空构造方法供 Room 使用
    public WatchlistEntry() { }

    // 标记为 @Ignore，让 Room 忽略此构造函数
    @Ignore
    public WatchlistEntry(int userId, @NonNull String stockSymbol) {
        this.userId = userId;
        this.stockSymbol = stockSymbol;
    }

    // Getter 和 Setter

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    @NonNull
    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(@NonNull String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }
}
