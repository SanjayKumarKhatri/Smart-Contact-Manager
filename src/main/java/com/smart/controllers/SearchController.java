package com.smart.controllers;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.smart.dao.ContactRepoistory;
import com.smart.dao.UserRepoistory;
import com.smart.entities.Contact;
import com.smart.entities.User;

@RestController
public class SearchController {
	
	@Autowired
	private UserRepoistory userRepoistory;
	
	@Autowired
	private ContactRepoistory contactRepoistory;

	//search handler
	@GetMapping("/search/{query}")
	public ResponseEntity<?> search(@PathVariable("query") String query,Principal principal) {
		
		System.out.println(query);	
		User user = userRepoistory.getUserByUserName(principal.getName());
		List<Contact> contacts = contactRepoistory.findByNameContainingAndUser(query, user);
		return ResponseEntity.ok(contacts);
	} 
}
