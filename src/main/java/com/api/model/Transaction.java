package com.api.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Date;

@Entity
@Table(name = "TRANSACTION_TBL")
public class Transaction {

    @NotNull
    @Id
    Integer id;
    @NotNull
    String userName;
    @NotNull
    Integer value;
    @NotNull
    Date date;

    public Transaction(Integer id, String userName, int value, Date date) {

        this.id = id;
        this.userName = userName;
        this.value = value;
        this.date = date;
    }

    public Transaction() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
