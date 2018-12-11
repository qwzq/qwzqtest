package com.jt.sso.controller;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jt.common.util.ObjectMapperUtils;
import com.jt.common.vo.SysResult;
import com.jt.sso.pojo.User;
import com.jt.sso.service.UserService;

@Controller
@RequestMapping("/user/")
public class UserController {

	@Autowired
	private UserService userService;
	//注册校验
	@RequestMapping("check/{param}/{type}")
	@ResponseBody
	public String check(@PathVariable String param,@PathVariable Integer type,
			String callback) throws Exception{
		//type:userName phone email
		Boolean exists=userService.check(param,type);
		String jsonData = ObjectMapperUtils.getMapper().
				writeValueAsString(SysResult.oK(exists));
		//返回jsonp数据
		String jsonpData=callback+"("+jsonData+")";
		return jsonpData;
	}
	@RequestMapping("test")
	@ResponseBody
	public String test() {
		//type:userName phone email
		return "testhello";
	}
	
	//用户注册
	@RequestMapping("register")
	@ResponseBody
	public SysResult doRegister(User user){
		userService.doRegister(user);
		return SysResult.oK(user.getUsername());
	}
	//用户登录
	@RequestMapping("login")
	@ResponseBody
	public SysResult doLogin(String u,String p){
		SysResult result=userService.doLogin(u,p);
		return result;
	}
	
	//接收前台ticket查询用户信息
	@RequestMapping("query/{ticket}")
	@ResponseBody
	public String checkUserJson(String callback,@PathVariable String ticket){
		String sysResultJson=userService.checkUserJson(ticket);
		//判断是否已经登录，userJson为空“”或null
		if(StringUtils.isNotEmpty(callback)){//已经登录
			return callback+"("+sysResultJson+")";
		}
		return sysResultJson;
	}
	
	
}
