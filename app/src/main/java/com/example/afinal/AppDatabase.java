package com.example.afinal;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

/**
 * Room 数据库，包含 User、Stock、WatchlistEntry、HistoricalPrice 和 Holding 五张表
 */
@Database(
        entities = {
                User.class,
                Stock.class,
                WatchlistEntry.class,
                HistoricalPrice.class,
                Holding.class
        },
        version = 3,            // 升级到 3，使用 fallbackToDestructiveMigration() 清库重建
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract StockDao stockDao();
    public abstract WatchlistDao watchlistDao();
    public abstract HistoricalPriceDao historicalPriceDao();
    public abstract HoldingDao holdingDao();

    private static volatile AppDatabase INSTANCE;
    private static final String DB_NAME = "stock_app.db";

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME
                            )
                            // 开发时允许在主线程查询；生产环境请删除此行并使用异步调用
                            .allowMainThreadQueries()
                            // 版本不匹配时直接丢弃旧库、重建新库
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
