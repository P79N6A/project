package com.yunxiao.base.yunxiaospringbootstarter.config;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.aegis.config.client.AegisConfigClient;

import com.yunxiao.base.yunxiaospringbootstarter.service.CASLoginHandle;
import com.yunxiao.base.yunxiaospringbootstarter.util.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @author voidman
 * @date 2018/05/14
 */

@Configuration
@RestController
@ConditionalOnExpression("${open.login.customization:true}")
public class CASLoginAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(CASLoginAutoConfiguration.class);
    @Autowired
    CASLoginHandle casLoginHandle;

    /**
     * 登录页面开发
     */
    @RequestMapping(value = "/custom/login", method = RequestMethod.GET)
    public void customLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //重定向
        String callback = request.getParameter("oauth_callback");
        String url = UrlUtil.getCasServer() + "/login?service=" + UrlUtil.getUcServer() + "/custom/response.do?callback="+callback;
        response.sendRedirect(url);

    }

    /**
     * 登录页面开发
     */
    @RequestMapping(value = "/check/login", method = RequestMethod.GET)
    public String check(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //重定向
        return "login ok";

    }

    /**
     * 第三方登录回调路由
     *
     * @param req
     * @param res
     */
    @RequestMapping("/custom/response.do")
    public void customResponse(HttpServletRequest req, HttpServletResponse res) {
        String callback = req.getParameter("callback");
        log.info("get callback info:" + callback);
        ////ex.:http://uc.aliyun.com/login?from=http://amon.aliyun.com:80&ctx=/project/project/project_list.htm
        //if (StringUtils.isBlank(callback)) {
        //    callback = UrlUtil.getDefaultIndexUrl();
        //}
        //String from = "", ctx = "", username = "";
        //if (callback.startsWith("http://")) {
        //    from = "http://" + callback.substring(7).split("/")[0];
        //} else if (callback.startsWith("https://")) {
        //    from = "https://" + callback.substring(8).split("/")[0];
        //}
        //ctx = callback.substring(from.length());
        String ticket = req.getParameter("ticket");
        log.info("get ticket info:" + ticket);
        try {
            res.sendRedirect(UrlUtil.getUcServer()+"/login?ticket="+ticket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}