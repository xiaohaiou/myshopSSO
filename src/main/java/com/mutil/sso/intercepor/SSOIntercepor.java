package com.mutil.sso.intercepor;

import com.mutil.sso.service.LoginService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import zhu.liang.common.annotation.CheckToken;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

@Component
public class SSOIntercepor implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(SSOIntercepor.class);

    @Autowired
    private LoginService loginService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 如果不是映射到方法直接通过
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        //检查是否有LoginToken注释，有则跳过认证
        if (method.isAnnotationPresent(CheckToken.class)) {
            CheckToken loginToken = method.getAnnotation(CheckToken.class);
            if (loginToken.required()) {
                // 获取请求头登入用户信息
                String token = request.getHeader("token");
                String userId = request.getHeader("uesrId");
                //验证是否登入
                if(StringUtils.isEmpty(userId) || StringUtils.isEmpty(token)){
                    LOG.info("进行CheckToken认证，无用户ID或者token值，验证失败。");
                    return false;
                }
                return this.validateToken(userId,token);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }

    private boolean validateToken(String userId,String token){
        return loginService.checkLogIn(Integer.valueOf(userId),token).isSuccess();
    }

}
