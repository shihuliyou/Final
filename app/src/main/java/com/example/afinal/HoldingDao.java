package com.example.afinal;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HoldingDao {
    /** 获取所有持仓 */
    @Query("SELECT * FROM holdings")
    List<Holding> getAll();

    /** 根据股票代码查询单条持仓 */
    @Query("SELECT * FROM holdings WHERE stockCode = :code LIMIT 1")
    Holding findByCode(String code);

    /** 获取指定用户的所有持仓 */
    @Query("SELECT * FROM holdings WHERE userId = :userId")
    List<Holding> getHoldingsByUserId(int userId);


    /** 插入一条持仓记录，若冲突则替换 */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(Holding holding);

    /** 批量插入持仓记录（冲突时替换） */

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Holding> holdings);

    @Query("SELECT * FROM holdings WHERE stockCode = :code AND userId = :userId LIMIT 1")
    Holding findByCodeAndUser(String code, int userId);
    /** 更新已有持仓 */
    @Update
    void update(Holding holding);

    /** 根据记录 ID 删除持仓 */
    @Query("DELETE FROM holdings WHERE id = :id")
    void deleteById(int id);

    /** 删除某用户的所有持仓 */
    @Query("DELETE FROM holdings WHERE userId = :userId")
    void deleteByUserId(int userId);
}
