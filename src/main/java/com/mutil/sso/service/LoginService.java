package com.mutil.sso.service;

import com.alibaba.fastjson.JSON;
import com.lc.service.RedisService;
import com.mutil.sso.dao.MmallUserMapper;
import com.mutil.sso.domain.MmallUser;
import com.mutil.sso.util.AesUtil;
import com.mutil.sso.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import zhu.liang.common.constants.Const;
import zhu.liang.common.response.ServerResponse;
import zhu.liang.common.util.MD5Util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Service
public class LoginService {

    private static final Logger LOG = LoggerFactory.getLogger(LoginService.class);

    @Autowired
    private MmallUserMapper mmallUserMapper;

    @Autowired
    private RedisService redisService;

    /**
     * 登入操作,验证登入信息，
     * 成功写入cookie信息，并写入redis
     * @param userName
     * @param userpwd
     * @return
     */
    public ServerResponse validateLogIn(String userName, String userpwd, HttpServletResponse response){
        MmallUser mmallUserSelectBean = new MmallUser();
        mmallUserSelectBean.setUsername(userName);
        mmallUserSelectBean.setPassword(MD5Util.MD5EncodeUtf8(userpwd));
        MmallUser mmallUserBack = mmallUserMapper.selectByMmallUser(mmallUserSelectBean);
        if(mmallUserBack==null){
            return ServerResponse.createByErrorMessage("用户名或者密码有误，请核实后重新登入！");
        }
        // 设置登入用户token值
        mmallUserBack.setToken(JwtUtil.createJWT(Const.TOKEN_EXPIRE_TIME,mmallUserBack));
        // 置空返回用户隐私信息
        mmallUserBack.setPassword(null);
        mmallUserBack.setQuestion(null);
        mmallUserBack.setAnswer(null);
        mmallUserBack.setEmail(null);
        mmallUserBack.setCreateTime(null);
        mmallUserBack.setUpdateTime(null);
        mmallUserBack.setPhone(null);
        // 返回请求response，添加登入成功cookie信息
        String cookieId = new AesUtil(Const.TOKENNAME).encrypt();
        String cookieValue = new AesUtil(JSON.toJSONString(mmallUserBack)).encrypt();
        String redisKey = new AesUtil(mmallUserBack.getId().toString()).encrypt();
        Cookie userCookie = new Cookie(cookieId,cookieValue);
        userCookie.setPath("/");
        userCookie.setHttpOnly(true);
        //有效期 1 天
        userCookie.setMaxAge(1*24*60*60);
        userCookie.setDomain("localhost");
        response.addCookie(userCookie);
        LOG.info("用户：{}登入成功,写入返回cookie。",userName);
        // 写入redis缓存
        redisService.set(redisKey,mmallUserBack,Const.TOKEN_EXPIRE_TIME);
        LOG.info("用户：{}登入成功，写入redis用户信息,key:{}。",userName,redisKey);
        return ServerResponse.createBySuccess("登入成功！",cookieValue);
    }

    /**
     * 验证用户token的有效性
     * @param userId
     * @param token
     * @return
     */
    public ServerResponse<String> checkLogIn(Integer userId, String token) {
        String redisKey = new AesUtil(userId.toString()).encrypt();
        Object obj = redisService.get(redisKey);
        if(null==obj){
            LOG.info("缓存中无登入信息，验证失败！");
            return ServerResponse.createByErrorMessage("缓存中无登入信息，验证失败！");
        }
        MmallUser mmallUser = JSON.parseObject(String.valueOf(obj),MmallUser.class);
        if(!token.equals(mmallUser.getToken())){
            LOG.info("登入用户token错误，验证失败！");
            return ServerResponse.createByErrorMessage("登入用户token错误，验证失败！");
        }
        if(!JwtUtil.isVerify(token,mmallUser)){
            LOG.info("登入用户token失效，验证失败！");
            return ServerResponse.createByErrorMessage("登入用户token失效，验证失败！");
        }
        // 重置登入用户redis失效时间
        redisService.set(redisKey,mmallUser,Const.TOKEN_EXPIRE_TIME);
        return ServerResponse.createBySuccess(new AesUtil(JSON.toJSONString(mmallUser)).encrypt());
    }

    public void setResponse(HttpServletRequest request,HttpServletResponse response){
        response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
        response.setHeader("Access-Control-Allow-Credentials", "true");
    }

}
