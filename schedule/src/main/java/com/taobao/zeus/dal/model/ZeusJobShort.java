package com.taobao.zeus.dal.model;

import java.io.Serializable;

public class ZeusJobShort implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Long value;

    private String text;

    public Long getValue() {
        return this.getId();
    }

    public String getText() {
        return this.getName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

}