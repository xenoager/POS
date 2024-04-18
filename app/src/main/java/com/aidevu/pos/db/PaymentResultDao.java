package com.aidevu.pos.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PaymentResultDao {

    @Insert
    void insert(PaymentResult paymentResult);

    @Query("DELETE From paymentResult_table WHERE id = :paymentResultId")
    void delete(int paymentResultId);

    @Query("DELETE FROM paymentResult_table")
    void clearPaymentResultList();

    @Query("SELECT * FROM paymentResult_table")
    List<PaymentResult> getPaymentResultList();

    @Query("SELECT * FROM paymentResult_table WHERE id = :paymentResultId ")
    PaymentResult getPaymentResultList(int paymentResultId);
}
