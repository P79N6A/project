package com.yunxiao.base.yunxiaospringbootstarter.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.aegis.config.client.AegisConfigClient;
import com.alibaba.aone.common.facade.UserOpenFacade;
import com.alibaba.aone.common.model.Result;
import com.alibaba.aone.login.handle.AbstractLoginHandle;
import com.alibaba.aone.sso.constants.SSOConstants;
import com.alibaba.aone.sso.constants.SSOProperties;
import com.alibaba.aone.sso.utils.CookieUtil;
import com.alibaba.aone.user.presenter.UserDTO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.yunxiao.base.yunxiaospringbootstarter.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.utils.URIBuilder;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static com.alibaba.aone.sso.utils.CookieUtil.removeCookie;

/**
 * @author voidman
 * @date 2018/05/14
 */
@Service
public class CASLoginHandle extends AbstractLoginHandle {

    private static final Logger logger = LoggerFactory.getLogger(CASLoginHandle.class);

    //@Autowired
    UserOpenFacade userOpenFacade ;
    @Autowired
    RestTemplate restTemplate;

    /**
     * 生成cookie
     *
     * @param request
     * @param response
     * @param loginConfigObject
     * @param cookieDomain
     * @return
     */
    @Override
    public boolean genCookie(HttpServletRequest request, HttpServletResponse response,
                             JSONObject loginConfigObject, String cookieDomain) {
        String ticket = request.getParameter("ticket");
        if (StringUtils.isBlank(ticket)) {
            logger.error("ticket is empty，relogin");
            return false;
        }
        String from = request.getParameter("from");

        String url = UrlUtil.getCasServer() + "/serviceValidate?service=%s&ticket=%s";
        try {
            url = String.format(url, URLEncoder.encode(UrlUtil.getUcServer() + "/custom/response.do", "utf-8"), ticket);
            String resp = restTemplate.getForObject(url, String.class);
            logger.info("get user info:" + resp);
            if (StringUtils.isBlank(resp)) {
                return false;
            }

            //解析username
            String username = parseUser(resp);
            logger.info("login user:" + username);

            if(StringUtils.isBlank(username)){
                logger.error("parse user null from response:"+resp);
                return false;
            }

            Result<UserDTO> userDTOResult = userOpenFacade.syncByInternalUsername(username);
            if (userDTOResult.getResult() == null) {
                logger.error("user get result err:" + username);
                return false;
            }
            setCookie(userDTOResult.getResult(), request, response, cookieDomain, loginConfigObject);

            return true;

        } catch (Exception e) {
            logger.error("gen cookie error,",e);
            return false;
        }

    }

    /**
     * @param request
     * @param response
     * @param loginConfigObject
     * @throws IOException
     */
    @Override
    public void doRedirectLogin(HttpServletRequest request, HttpServletResponse response, JSONObject
        loginConfigObject)
        throws IOException {
        try {
            String url = UrlUtil.getCasServer() + "/login?service=" + UrlUtil.getUcServer() + "/custom/response.do";
            URIBuilder builder = new URIBuilder(url);
            builder.addParameter("oauth_callback", getUcLoginCallbackUrl(request).toString());
            String redirectTo = builder.toString();
            response.sendRedirect(redirectTo);
        } catch (URISyntaxException e) {
            logger.error("uri syntax error, " + e);
        }
    }

    public void doRedirectCallback(HttpServletRequest request, HttpServletResponse response, String mainAccountType) {
        try {
            response.sendRedirect(UrlUtil.getYunxiaoIndex());
        } catch (IOException e) {
            logger.error("redirect err:url="+UrlUtil.getYunxiaoIndex(),e);
        }
    }
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       String loginType, String cookieDomain)
        throws IOException {
        removeCookie(SSOProperties.INTERNAL_COOKIE, SSOConstants.SLASH, cookieDomain, response);
        removeCookie(SSOProperties.USER_COOKIE, SSOConstants.SLASH, cookieDomain, response);
        removeCookie(SSOProperties.LAST_HEART_BEAT_TIME, SSOConstants.SLASH, cookieDomain, response);
        removeCookie(SSOProperties.YUNXIAO_USER_COOKIE, SSOConstants.SLASH, cookieDomain, response);
        removeCookie(SSOProperties.LOGIN_COOKIE, SSOConstants.SLASH, cookieDomain, response);
        removeCookie(SSOProperties.DOMAIN_COOKIE, SSOConstants.SLASH, cookieDomain, response);

        String casServer = AegisConfigClient.getKey("cas.server.url");
        if(StringUtils.isNotBlank(casServer)){
            try {
                response.sendRedirect(casServer+"/logout");
            }catch (Exception e){
                logger.error("logout err,"+casServer,e);
            }
        }
    }

    private String parseJSONUser(String s) {
        if (StringUtils.isBlank(s)) { return null;}
        try {
            JSONObject jo = JSON.parseObject(s);
            return jo.getJSONObject("serviceResponse").getJSONObject("authenticationSuccess").getString("user");
        } catch (Throwable e) {
            return null;
        }

    }

    private String parseUser(String s) {
        if (StringUtils.isBlank(s)) { return null;}
        try {
            Document doc = DocumentHelper.parseText(s);
            Element root = doc.getRootElement();

            return ((DefaultElement)((DefaultElement)root.elements().get(0)).content().get(1)).getText();
        } catch (Throwable e) {
            logger.error("parse user err:",e);
            return null;
        }

    }

    private int getMaxAge(){
        String min = AegisConfigClient.getKey("cas.timeout.minute");
        int defaultMaxAge = 60*24*7;//一周
        //没有设置超时时间，则永不过期
        if(StringUtils.isBlank(min)){
            return defaultMaxAge;
        }
        if(!StringUtils.isNumeric(min)){
            return defaultMaxAge;
        }
        return Integer.valueOf(min);

    }
    private boolean isCookieAlive(HttpServletRequest request){
        String min = AegisConfigClient.getKey("cas.timeout.minute");
        //没有设置超时时间，则永不过期
        if(StringUtils.isBlank(min)){
            return true;
        }
        //没有设置正确的超时时间，则永不过期
        if(!StringUtils.isNumeric(min)){
            return true;
        }

        String a_u_t = com.alibaba.aone.sso.utils.CookieUtil.getCookieValue(SSOProperties.LAST_HEART_BEAT_TIME,request);
        if(StringUtils.isBlank(a_u_t)){
            return false;
        }
        try {
            String time = com.alibaba.aone.sso.utils.CookieUtil.decodeCookie(a_u_t);
            Long timeLong = Long.valueOf(time);
            //时间间隔超过{min}分钟，则认为超时，cookie失效
            long cost = System.currentTimeMillis()-timeLong;
            if(cost<1000*60*Integer.valueOf(min)){
                logger.info("cookie time out:"+cost+",min="+min);
                return false;
            }else{
                return true;
            }
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void setCookie(UserDTO userDTO, HttpServletRequest request, HttpServletResponse response, String cookieDomain, JSONObject loginConfigObject) {
        try {
            Cookie userCookie = new Cookie("a_u", CookieUtil.encodeCookie(JSON.toJSONString(userDTO)));
            userCookie.setPath("/");
            userCookie.setDomain(cookieDomain);
            userCookie.setMaxAge(getMaxAge()*60);
            response.addCookie(userCookie);
            request.setAttribute("ucLoginUser", userDTO);
            Cookie heartBeatTimeCookie = new Cookie("a_u_t", CookieUtil.encodeCookie(String.valueOf(System.currentTimeMillis())));
            heartBeatTimeCookie.setPath("/");
            heartBeatTimeCookie.setDomain(cookieDomain);
            heartBeatTimeCookie.setMaxAge(getMaxAge()*60);

            response.addCookie(heartBeatTimeCookie);
            Cookie uuidCookie = new Cookie("aone_user_uuid", userDTO.uuid);
            uuidCookie.setPath("/");
            uuidCookie.setDomain(cookieDomain);
            uuidCookie.setMaxAge(getMaxAge()*60);

            response.addCookie(uuidCookie);
            this.checkUserInfo(request, response, userDTO, loginConfigObject);
        } catch (InvalidKeyException var9) {
            logger.error("invalid key, ", var9);
        } catch (IllegalBlockSizeException var10) {
            logger.error("illegal block size, ", var10);
        } catch (BadPaddingException var11) {
            logger.error("bad padding, ", var11);
        } catch (UnsupportedEncodingException var12) {
            logger.error("unsupported encoding, ", var12);
        }

    }
}
