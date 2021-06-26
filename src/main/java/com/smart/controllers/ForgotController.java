package com.smart.controllers;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepoistory;
import com.smart.entities.User;
import com.smart.service.EmailService;



@Controller
public class ForgotController {
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepoistory userRepoistory;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	Random random = new Random(1000);
	
	//email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		
		return "forgot_email_form";
	}
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session) {
		
		System.out.println("Email : " + email);
		
		//generating OTP of 4 digit
		
		int otp = random.nextInt(99999);
		System.out.println("OTP "+otp);
		
		//write code for send otp email...
		String subject = "OTP From SCM";
		String message = ""
				+"<div style='border:1px;solid #e2e2e2; padding:20px' >"
				+"<h1>"
				+"OTP is : "
				+"<b>"+otp
				+"</n>"
				+"</h1>"
				+"</div>";
		String to = email;
		
		boolean flag = this.emailService.sendEmail(subject,message,to);
		
		if (flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
			
		} else {
			session.setAttribute("message", "Check your email id !!");
			return "forgot_email_form";
		}		
		
	}
	
	// Verify OTP
	
	@PostMapping("/verify-otp")
	public String verfiyOtp(@RequestParam("otp") int otp , HttpSession session) {
		
		int myOtp = (int)session.getAttribute("myotp");
		String email = (String)session.getAttribute("email");
		
		if (myOtp==otp) {
			
			//Change Password Form
			User user = userRepoistory.getUserByUserName(email);
			
			if (user == null) {
				
				//send error messsage
				session.setAttribute("message", "User does not exits with this email !!");
				
				return "forgot_email_form";
				
			} else {
				
				// send change password form
			}
			
			return "password_change_form";
			
		} else {
			session.setAttribute("message", "You have Entered Wrong OTP !!");
			return "verify_otp";
		}
	}
	
	//Change Password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newPassword,HttpSession session) {
		
		String email = (String)session.getAttribute("email");
		User user = userRepoistory.getUserByUserName(email);
		user.setPassword(bCryptPasswordEncoder.encode(newPassword));
		this.userRepoistory.save(user);
		
		return "redirect:/signin?change=password changed successfully..";
	}
}
