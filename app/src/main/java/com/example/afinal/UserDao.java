package com.example.afinal;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

/**
 * 用户表的 DAO，包含增删改查方法
 */
@Dao
public interface UserDao {
    /**
     * 根据用户名和密码查询用户，用于登录校验
     */
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    User getUserByCredentials(String username, String password);

    /**
     * 根据用户名查询用户，用于注册时判断是否已存在
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getUserByUsername(String username);

    /**
     * 插入新用户
     */
    @Insert
    void insertUser(User user);
}
