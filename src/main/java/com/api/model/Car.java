package com.api.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.util.UUID;

public class Car {

    @NotNull
    String id;
    @NotNull
    String trademark;
    @NotNull
    String name;
    @PositiveOrZero
    Integer sits;
    @PositiveOrZero
    Integer wheels;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTrademark() {
        return trademark;
    }

    public void setTrademark(String trademark) {
        this.trademark = trademark;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSits() {
        return sits;
    }

    public void setSits(Integer sits) {
        this.sits = sits;
    }

    public Integer getWheels() {
        return wheels;
    }

    public void setWheels(Integer wheels) {
        this.wheels = wheels;
    }
}
