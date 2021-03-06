package com.connext.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.connext.pojo.User;
import com.connext.pojo.Userlog;
import com.connext.service.UserServiceImpl;
/**
 * 
 * @author Zach.Zhang
 *
 */
@Controller
public class UserController {
	
	@Autowired
	UserServiceImpl userService;
	
	private Integer failTimes=3;
	private Integer loginLockTime=86400000;
	
	@RequestMapping("/toRegist.do")
	public String userToRegist(){
		System.out.println("正在前往注册页面…………");
		return "regist";
	}
	
	@RequestMapping("/toLogin.do")
	public String userToLogin(){
		System.out.println("正在前往登录页面…………");
		return "login";
	}
		
	@RequestMapping("/userRegist.do")
	public String userLogin(User user,String phonecode){
		System.out.println("正在注册…………");
		user.setUserpassword(MD5.getInstance().getMD5(user.getUserpassword()));
		userService.registUser(user);
		System.out.println("注册完成…………");
		System.out.println("添加用户日志…………");
		Userlog userlog = new Userlog();
		userlog.setUserphone(user.getUserphone());
		userService.insertSelective(userlog);
		return "login";
	}
	/**
	 * userphoneExist.do
	 * @throws IOException 
	 */
	
	@RequestMapping("/userphoneExist.do")
	@ResponseBody
	public void  userphoneExist(String userphone,HttpServletRequest request,HttpServletResponse response) throws IOException{
		System.out.println("正在检测用户手机号是否存在…………");
		System.out.println("userphoneuserphoneuserphoneuserphone"+userphone);
		User user = userService.selectByUserphone(userphone);
		if(user!=null){
			request.setAttribute("loginMessage1", "手机号“"+userphone+"”已经存在");
			System.out.println("已存在");
			response.getWriter().write("true");
		}else{
			System.out.println("不存在");
			response.getWriter().write("false");
		}
		System.out.println("检测完成…………");
	}
	
	/**
	 * phonecodeCreate.do
	 */
	@RequestMapping("/phonecodeCreate.do")
	@ResponseBody
	public void  phonecodeCreate(String userphone,HttpServletRequest request,HttpServletResponse response) throws IOException{
		System.out.println("正在准备发送手机验证码…………");
		 // 验证码中所使用到的字符
        char[] codeChar = "0123456789".toCharArray();
        // 存放生成的验证码
        String captcha = ""; 
        Random random = new Random();
        int a = 4;
        for(int i = 0; i < a; i++) { 
            int index = random.nextInt(codeChar.length);
           
            captcha += codeChar[index];
        }
        // 将生成的验证码code放入sessoin中
        request.getSession().setAttribute("code", captcha);
        request.getSession().setMaxInactiveInterval(60);
		System.out.println("【联蔚科技】您的验证码是： "+captcha+"，在60秒内有效。如非本人操作请忽略本短信。");
		response.getWriter().write("true");
	}
	
	/**
	 * phonecodeCheck.do
	 * 判断验证码是否正确
	 */
	@RequestMapping("/phonecodeCheck.do")
	@ResponseBody
	public void  phonecodeCheck(String phonecode,HttpServletRequest request,HttpServletResponse response) throws IOException{
		System.out.println("正在判断验证码是否正确…………");
		String code = (String) request.getSession().getAttribute("code");
		if(phonecode.equals(code)){
			System.out.println("验证码正确");
			response.getWriter().write("true");
		}else{
			System.out.println("验证码错误");
			response.getWriter().write("false");
		}
		System.out.println("验证码检测完成…………");
	}
	/**
	 * login.do
	 * 用户登录
	 * @throws IOException 
	 * @throws ParseException 
	 */
	@RequestMapping("userLogin.do")
	public String userLogin(User user,HttpServletRequest request,HttpServletResponse response) throws IOException, ParseException{
		User u = userService.selectByUserphone(user.getUserphone());
		user.setUserpassword(MD5.getInstance().getMD5(user.getUserpassword()));
		User uu=userService.selectByUser(user);
		//得到该手机的登录日志
		Userlog userlog = userService.selectUserlogByUserphone(user.getUserphone());
		if(u==null){
			System.out.println("手机号不存在");
			request.setAttribute("phonex", "手机号不存在");
			return "login";
		}else if(userlog.getFailtimes()>=failTimes&&(new java.util.Date().getTime()-userlog.getLastfailtime().getTime())<loginLockTime){
			//当该用户的密码输错次数大于等于3且现在时间比上次输错时间小于24小时的话，就返回登录界面不允许其登录
			request.setAttribute("loginx", "密码输错三次！禁止登录！");
			return "login";
		}else if(uu==null){
			System.out.println("密码错误");
			request.setAttribute("passwordx", "密码错误");
			System.out.println("-------将错误次数+1");
			//new一个Userlog对象
			Userlog ul=new Userlog();
			//设置Userlog对象的手机号
			ul.setUserphone(user.getUserphone());
			SimpleDateFormat dFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	        String formatDate = dFormat.format(new java.util.Date());  
	        //将当前时间加入到对象中
			ul.setLastfailtime(dFormat.parse(formatDate));
			//调用方法，将密码错误次数加1，更新输错的时间
			userService.updateAddFailtimesByUserphone(ul);
			return "login";
		}else{
			System.out.println("登陆成功");
			System.out.println("重置用户密码输错次数……");
			//把用户日志里的错误次数置为0
			userService.updateResetFailtimesByUserphone(user.getUserphone());
			System.out.println("用户密码输错次数已重置为0……");
			request.getSession().setAttribute("user", uu);
			request.getSession().setMaxInactiveInterval(3600);
			return "redirect:toMessageList.do";
		}
	}
}

