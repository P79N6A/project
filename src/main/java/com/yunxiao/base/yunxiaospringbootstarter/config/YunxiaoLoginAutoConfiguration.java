package com.yunxiao.base.yunxiaospringbootstarter.config;

import java.io.IOException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.aone.sso.utils.CookieUtil;

import com.yunxiao.base.yunxiaospringbootstarter.service.CASLoginHandle;
import com.yunxiao.base.yunxiaospringbootstarter.util.UrlUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author voidman
 * @date 2018/05/14
 */

//@Configuration
//@RestController
//@ConditionalOnExpression("${open.login.customization:true}")
public class YunxiaoLoginAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(YunxiaoLoginAutoConfiguration.class);
    @Autowired
    CASLoginHandle casLoginHandle;

    /**
     * 登录页面开发
     */
    @RequestMapping(value = "/custom/yxlogin", method = RequestMethod.GET)
    public void customLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //todo
        //重定向到自研登录界面

    }

    /**
     * 注册页面开发
     */
    @RequestMapping(value = "/custom/yxregister", method = RequestMethod.GET)
    public void customRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //todo
        //重定向到自研注册界面

    }

    /**
     * 校验接口开发
     */
    @RequestMapping(value = "/custom/yxcheck", method = RequestMethod.POST)
    public void customCheck(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //todo
        //校验账号密码是否正确
        //账号正确后，检查是否已经有初始化的公司
        //无初始化公司时&&公司数据大于1个，跳转到选择公司页面
        //这里生成一个ticket，规则为用户名+密码的encode后的值
        try {
            String ticket = CookieUtil.encodeCookie("user+password");
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }

    }


    /**
     * 第三方登录回调路由
     *
     * @param req
     * @param res
     */
    @RequestMapping("/custom/yxcallback")
    public void customResponse(HttpServletRequest req, HttpServletResponse res) {
        String ticket = req.getParameter("ticket");
        log.info("get ticket info:" + ticket);
        String groupId = req.getParameter("groupId");
        log.info("get groupId info:" + groupId);
        //todo
        //
        try {
            res.sendRedirect(UrlUtil.getUcServer()+"/login?ticket="+ticket+"&groupId="+groupId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 登录页面开发
     */
    @RequestMapping(value = "/check/login", method = RequestMethod.GET)
    public String check(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //重定向
        return "login ok";
    }


}