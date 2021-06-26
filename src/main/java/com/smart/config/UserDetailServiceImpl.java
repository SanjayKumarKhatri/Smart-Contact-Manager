package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.dao.UserRepoistory;
import com.smart.entities.User;

public class UserDetailServiceImpl implements UserDetailsService {
	
	@Autowired
	private UserRepoistory userRepoistory;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		//fetching user from database
		
		User user = userRepoistory.getUserByUserName(username);
		
		if (user == null) {
			throw new UsernameNotFoundException("Could not found user !!");
		}
		
		CustomUserDetail customUserDetail = new CustomUserDetail(user);
		
		
		return customUserDetail;
	}

}
