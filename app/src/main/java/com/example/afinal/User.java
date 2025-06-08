package com.example.afinal;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * 用户实体类，对应本地 SQLite 表 "users"
 */
@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;                      // 用户ID，自增主键

    @ColumnInfo(name = "username")
    public String username;             // 用户名

    @ColumnInfo(name = "password")
    public String password;             // 密码（实际应用中请存储哈希值）

    // 空的构造方法（Room 要求）
    public User() { }

    // 可选：方便初始化时赋值的构造方法，Room 会忽略它
    @Ignore
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
