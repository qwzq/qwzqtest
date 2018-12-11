package com.jt.sso.service;

import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jt.common.redis.RedisService;
import com.jt.common.util.ObjectMapperUtils;
import com.jt.common.vo.SysResult;
import com.jt.sso.mapper.UserMapper;
import com.jt.sso.pojo.User;
@Service
public class UserService {

	@Autowired
	UserMapper userMapper;
	public Boolean check(String param, Integer type) {
		//根据type值的不同调用不同的mapper方法完成数据的获取
		int count =0;
		User user=new User();
		if(type==1){
			user.setUsername(param);
			count=userMapper.selectCount(user);
		}else if(type==2){
			user.setPhone(param);
			count=userMapper.selectCount(user);
		}else{
			user.setEmail(param);
			count=userMapper.selectCount(user);
		}
		if(count==0){
			return false;
		}
		return true;
	}
	public void doRegister(User user) {

		//前台缺少一个email，需要唯一值
		user.setEmail(user.getUsername());
		//新增数据，封装完毕数据‘
		
		user.setCreated(new Date());
		user.setUpdated(user.getCreated());
		//密码时加密的
		String password=user.getPassword();
		user.setPassword(DigestUtils.md5Hex(password));
		userMapper.insert(user);
		
	}
	@Autowired
	private RedisService redis;
	public SysResult doLogin(String u, String p) {
		//查询username
		User _user=new User();
		_user.setUsername(u);
		User user = userMapper.selectOne(_user);
		if(user!=null){//用户名正确
			//比对password
			String tPassword=DigestUtils.md5Hex(p);
			if(tPassword.equals(user.getPassword())){
				//登录成功，生成ticket将user信息写入redis
				String ticket=
						DigestUtils.md5Hex("JT_TICKET"+System.currentTimeMillis()+u);
				//写入缓存，mapper将对象转化为json
				try {
					String userJson=
							ObjectMapperUtils.getMapper().writeValueAsString(user);
					redis.set(ticket, userJson,3600);
					return SysResult.oK(ticket);
				} catch (Exception e) {
					return SysResult.build(201, "","");
				}
			}
		}
		return SysResult.build(201, "用户名密码不正确", "");
	}
	public String checkUserJson(String ticket) {
		try {
			String userJson=redis.get(ticket);
			String sysResultJson=
					ObjectMapperUtils.getMapper().writeValueAsString(SysResult.oK(userJson));
			return sysResultJson;
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}

}
