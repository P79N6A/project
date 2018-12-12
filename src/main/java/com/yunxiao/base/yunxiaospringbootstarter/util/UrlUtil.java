package com.yunxiao.base.yunxiaospringbootstarter.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.alibaba.aegis.config.client.AegisConfigClient;

import org.apache.commons.lang.StringUtils;

/**
 * @author voidman
 * @date 2018/05/24
 */
public class UrlUtil {

    private static final String DEFUALT_UC_SERVER = "http://uc.aliyun.com";
    private static final String UC_LOGIN = "/login?from=%s&ctx=%s&ticket=%s";
    public static final String DEFAULT_DOMAIN = "aliyun.com";
    public static final String YUNXIAO_DOT = "yunxiao.";
    public static final String GLOBAL_DOMAIN_KEY = "global.domain";
    public static final String IS_DOMAIN_UNITE_KEY = "is.domain.unite";
    public static final String LOGIN_URL_KEY = "login.url";
    public static final String UC_SERVER_KEY = "uc.server.url";
    private static final String DEFAULT_CAS_URL = "https://cas.devcloud.alibaba.com:8443/cas";
    private static final String DEFAULT_YUNXIAO_INDEX = "http://aone.aliyun.com";
    private static final String AONE_SERVER_URL = "aone.server.url";

    public static String getUcLogin(String from, String ctx, String username) {
        return getUcLogin(from, ctx, username);
    }

    private static String getUcLoginPrivate(String from, String ctx, String username)
        throws UnsupportedEncodingException {
        String ucServer = getUcServer();
        return String.format(UC_LOGIN, ucServer, URLEncoder.encode(from), "utf-8", URLEncoder.encode(ctx, "utf-8"),
            username);
    }

    public static String getRedirectUrl() {
        String url = get(LOGIN_URL_KEY, getUcServer() + "/internal/login");
        url = url + "?oauth_callback=http://" + YUNXIAO_DOT + get(GLOBAL_DOMAIN_KEY, DEFAULT_DOMAIN);
        return url;
    }

    public static String getDefaultIndexUrl() {
        return "http://" + YUNXIAO_DOT + get(GLOBAL_DOMAIN_KEY, DEFAULT_DOMAIN);
    }

    public static String getCasServer() {
        String casurl = AegisConfigClient.getKey("cas.server.url") == null ? DEFAULT_CAS_URL : AegisConfigClient.getKey(
            "cas.server.url");
        return casurl;
    }

    public static String getUcServer() {
        String uc = AegisConfigClient.getKey(UC_SERVER_KEY) == null ? DEFUALT_UC_SERVER : AegisConfigClient.getKey(
            UC_SERVER_KEY);
        //casurl = "http://uc.aliyun.com:9800";
        if (uc.endsWith("/")) {
            return uc.substring(0, uc.length() - 1);
        }

        return uc;
    }

    public static String getYunxiaoIndex() {
        if (StringUtils.isBlank(AegisConfigClient.getKey(AONE_SERVER_URL))) {
            return DEFAULT_YUNXIAO_INDEX;
        } else {
            return AegisConfigClient.getKey(AONE_SERVER_URL);
        }
    }

    public static String get(String key, String defaultValue) {
        String s = AegisConfigClient.getKey(key);
        if (StringUtils.isBlank(s)) {
            return defaultValue;
        } else {
            return s;
        }
    }
}
