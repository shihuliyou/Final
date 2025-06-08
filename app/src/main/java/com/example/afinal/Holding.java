package com.example.afinal;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "holdings")
public class Holding {
    /** 自增主键 */
    @PrimaryKey(autoGenerate = true)
    public int id;

    /** 所属用户 ID */
    public int userId;

    /** 股票代码 */
    public String stockCode;

    /** 持有数量 */
    public int quantity;

    /** 平均成本价 */
    public double averagePrice;

    /** Room 使用的无参构造器 */
    public Holding() { }

    /**
     * 应用中使用的构造器，标注 @Ignore 让 Room 只调用无参构造器
     */
    @Ignore
    public Holding(int userId, String stockCode, int quantity, double averagePrice) {
        this.userId       = userId;
        this.stockCode    = stockCode;
        this.quantity     = quantity;
        this.averagePrice = averagePrice;
    }
}
