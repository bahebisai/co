package com.xiaomi.emm.model;

/**
 * Created by Administrator on 2017/5/26.
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 令牌
 */
public class Token implements Serializable {


    private String token;     // 用户令牌(获取相关数据使用)
    // private String token_type;     // 令牌类型
    // private int expires_in;        // 过期时间
    // private String refresh_token; // 刷新令牌(获取新的令牌)
    // private int created_at;        // 创建时间
    private String alias;      //用户别名

    private String keepAliveHost; //long link host
    private String keepAlivePort; //long link port

    private List<TelephoyWhiteUser> whiteList;


    public String getAccess_token() {
        return token;
    }

    public void setAccess_token(String access_token) {
        this.token = access_token;
    }

    /*public String getToken_type() {
        return token_type;
    }

    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    public int getExpires_in() {
        return expires_in;
    }

    public void setExpires_in(int expires_in) {
        this.expires_in = expires_in;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    public int getCreated_at() {
        return created_at;
    }

    public void setCreated_at(int created_at) {
        this.created_at = created_at;
    }*/

    public String getUser_alias() {
        return alias;
    }

    public void setUser_alias(String user_alias) {
        this.alias = user_alias;
    }

    public String getKeepAliveHost() {
        return keepAliveHost;
    }

    public void setKeepAliveHost(String keepAliveHost) {
        this.keepAliveHost = keepAliveHost;
    }

    public String getKeepAlivePort() {
        return keepAlivePort;
    }

    public void setKeepAlivePort(String keepAlivePort) {
        this.keepAlivePort = keepAlivePort;
    }

    public List<TelephoyWhiteUser> getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(List<TelephoyWhiteUser> whiteList) {
        this.whiteList = whiteList;
    }

    @Override
    public String toString() {
        return "Token{" +
                "access_token='" + token + '\'' +
                /*", token_type='" + token_type + '\'' +
                ", expires_in=" + expires_in +
                ", refresh_token='" + refresh_token + '\'' +
                ", created_at=" + created_at + '\'' +*/
                ", user_alias" + alias +
                '}';
    }
}
