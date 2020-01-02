package com.mutil.sso.controller;

import com.mutil.sso.domain.MmallUser;
import com.mutil.sso.service.LoginService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import zhu.liang.common.response.ResponseCode;
import zhu.liang.common.response.ServerResponse;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping(value = "/login")
public class LoginController {

	private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	private LoginService loginService;

	@PostMapping("/loginIn.do")
	@ResponseBody
	public String logIn(@RequestParam(value = "userName",required = true) String userName,
						@RequestParam(value = "userPwd",required = true) String userPwd,
						@RequestParam(value = "returnUrl",required = true) String returnUrl,
						HttpServletRequest request,
						HttpServletResponse response){
		if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(userPwd)) {
			return ResponseCode.ERROR.getDesc();
		}
		LOG.info("用户：{}，请求登入。",userName);
		ServerResponse serverResponse = loginService.validateLogIn(userName,userPwd,response);
		if(!serverResponse.isSuccess()){
			return ResponseCode.ERROR.getDesc();
		}
		try {
			LOG.info("请求重定向地址为：{}",returnUrl);
			loginService.setResponse(request,response);
			response.sendRedirect(returnUrl);
		} catch (IOException e) {
			LOG.error("重定向错误！");
			e.printStackTrace();
		}
		return ResponseCode.SUCCESS.getDesc();
	}

	@PostMapping("/loginInByAjax.do")
	@ResponseBody
	public String loginInByAjax(@RequestParam(value = "userName",required = true) String userName,
								@RequestParam(value = "userPwd",required = true) String userPwd,
								HttpServletRequest request,
								HttpServletResponse response){
		if (StringUtils.isEmpty(userName) || StringUtils.isEmpty(userPwd)) {
			return ResponseCode.ERROR.getDesc();
		}
		LOG.info("用户：{}，请求登入。",userName);
		loginService.setResponse(request,response);
		ServerResponse serverResponse = loginService.validateLogIn(userName,userPwd,response);
		if(!serverResponse.isSuccess()){
			return ResponseCode.ERROR.getDesc();
		}
		return ResponseCode.SUCCESS.getDesc();
	}

	@PostMapping("checkLogin.do")
	@ResponseBody
	public ServerResponse<String> checkLogin(@RequestBody MmallUser mmallUser){
		if (mmallUser ==null || mmallUser.getId()==null
				|| StringUtils.isEmpty(mmallUser.getToken())) {
			return ServerResponse.createByError();
		}
		LOG.info("检查用户userId：{}是否登入，token:{}",mmallUser.getId(),mmallUser.getToken());
		return loginService.checkLogIn(mmallUser.getId(),mmallUser.getToken());
	}

}
