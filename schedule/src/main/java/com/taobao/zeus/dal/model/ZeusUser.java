package com.taobao.zeus.dal.model;

import com.taobao.zeus.util.DateUtil;

import java.util.Date;

public class ZeusUser {
    public enum UserStatus {
        WAIT_CHECK (0), CHECK_SUCCESS (1), Cancel (-1), CHECK_FAILED (-2);
        private int nCode ;
        private UserStatus( int _nCode) {
            this.nCode = _nCode;
        }
        @Override
        public String toString() {
            return String.valueOf ( this.nCode );
        }
        public int value() {
            return this.nCode;
        }
    }

    public static final ZeusUser ADMIN=new ZeusUser(){
        public String getEmail() {return "512164042@qq.com";};
        public String getName() {return "biadmin";};
        public String getPhone() {return "";};
        public String getUid() {return "biadmin";};
    };
    public static ZeusUser USER=new ZeusUser(null,null,null,null);
    public ZeusUser(String email, String name, String phone,
                    String uid) {
        this.email = email;
        this.name = name;
        this.uid = uid;
        this.phone = phone;
        // TODO Auto-generated constructor stub
    }
    public ZeusUser() {
        // TODO Auto-generated constructor stub
    }

    private Long id;

    private String email;

    private Date gmtCreate;

    private Date gmtModified;

    private String modifiedTime;

    private String name;

    private String phone;

    private String uid;

    private String wangwang;

    //default not effective
    private Integer isEffective=0;

    private Integer userType;

    private String description;

    private String password;

    private String kerberosUser;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public Date getGmtCreate() {
        return gmtCreate;
    }

    public void setGmtCreate(Date gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public Date getGmtModified() {
        return gmtModified;
    }

    public void setGmtModified(Date gmtModified) {
        this.gmtModified = gmtModified;
    }

    public String getModifiedTime() {
        return DateUtil.date2String(this.getGmtModified(),"yyyy-MM-dd HH:mm:ss");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone == null ? null : phone.trim();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? null : uid.trim();
    }

    public String getWangwang() {
        return wangwang;
    }

    public void setWangwang(String wangwang) {
        this.wangwang = wangwang == null ? null : wangwang.trim();
    }

    public Integer getIsEffective() {
        return isEffective;
    }

    public void setIsEffective(Integer isEffective) {
        this.isEffective = isEffective;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public String getKerberosUser() {
        return kerberosUser;
    }

    public void setKerberosUser(String kerberosUser) {
        this.kerberosUser = kerberosUser;
    }
}