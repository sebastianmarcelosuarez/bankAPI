package com.api.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "ACCOUNT_TBL")
public class Account {

    @NotNull
    @Id
    Integer id;
    @NotNull
    String userName;
    @NotNull
    Integer value;

    public Account(Integer id, String userName, int value) {

        this.id = id;
        this.userName = userName;
        this.value = value;

    }

    public Account() {
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

    @Override
    public String toString() {
        return "Account{" +
                "id='" + id + '\'' +
                ", userName='" + userName + '\'' +
                ", value=" + value +
                '}';
    }
}
