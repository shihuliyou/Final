package com.example.afinal;

public class Index {
    public String name;    // 指数或股票名称
    public String secid;   // 用于行情接口的 secid 标识（如 "1.000001"）
    public double price;   // 最新点位或价格
    public double change;  // 涨跌幅（百分比）

    /** 构造：只给名称和 secid，价格与涨跌幅后续填充 */
    public Index(String name, String secid) {
        this.name = name;
        this.secid = secid;
        this.price = 0.0;
        this.change = 0.0;
    }

    /** 构造：直接给全字段，适用于涨幅榜等 */
    public Index(String name, String secid, double price, double change) {
        this.name = name;
        this.secid = secid;
        this.price = price;
        this.change = change;
    }

    // 可选：如需 getter/setter，可按需添加
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSecid() { return secid; }
    public void setSecid(String secid) { this.secid = secid; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public double getChange() { return change; }
    public void setChange(double change) { this.change = change; }
}
