package com.smart.controllers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.swing.text.html.HTMLDocument.HTMLReader.PreAction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepoistory;
import com.smart.dao.UserRepoistory;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepoistory userRepoistory;	
	@Autowired
	private ContactRepoistory contactRepoistory;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {
		String userName = principal.getName();
		System.out.println("User Name : "+userName);
		
		User user = userRepoistory.getUserByUserName(userName);
		System.out.println("USER : "+user);
		
		model.addAttribute("user", user);
	}
	
	//user dashboard
	@RequestMapping("/index")
	public String dashBoard(Model model , Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	//open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		
		return "normal/add_contact_form";
		
	}
	
	//processing add contact
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) throws IOException {
		
		//processing and uploading file...
		
		if (file.isEmpty()) {
			
			//if the file is empty then try our message
			
			System.out.println("Image not selected");
			contact.setImage("contact.png");
			
		} else {
			
			//upload file to the folder and update the name to contact
			contact.setImage(file.getOriginalFilename());
			
			File saveFile = new ClassPathResource("/static/image").getFile();
			
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image is uploaded");
		}
		
		try {
			String name = principal.getName();
			User user = userRepoistory.getUserByUserName(name);
			
			contact.setUser(user);
			
			user.getContacts().add(contact);
			userRepoistory.save(user);
			
			System.out.println("DATA : "+contact);
			
			System.out.println("Added to database");
			//message - success
			session.setAttribute("message" , new Message("Your contact is added !! Add more..", "success"));
						
			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("ERROR : "+e.getMessage());
			//message - error
			session.setAttribute("message" , new Message("Some thing went wrong !! Try again...", "danger"));
		}
		return "normal/add_contact_form";
	} 
	
	//show contacts handler
	//per page = 5[n]
	//current page = 0[Page]
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page , Model model , Principal principal) {
		
		model.addAttribute("title", "Show User Contact");
		// contacts ki list ko behjni hn

		String userName = principal.getName();

		User user = userRepoistory.getUserByUserName(userName);

		// currentPage-page
		// Contact per page - 5
		Pageable pageable = PageRequest.of(page, 3);

		Page<Contact> contacts = contactRepoistory.findContactByUser(user.getId(), pageable);

		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}
	
	//showing particular contact details
	@RequestMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId , Model model, Principal principal) {
		
		Optional<Contact> optionalContact = contactRepoistory.findById(cId);
		Contact contact = optionalContact.get();
		
		String userName = principal.getName();
		User user = userRepoistory.getUserByUserName(userName);
		
		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("title", contact.getName());
			model.addAttribute("contact", contact);
		}
		return "normal/contact_detail";
	}
	
	//delete contact handler
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cId , HttpSession session,Principal principal) {
		
		Contact contact = contactRepoistory.findById(cId).get();
		
		User user = userRepoistory.getUserByUserName(principal.getName());
		
		user.getContacts().remove(contact);
		
		userRepoistory.save(user);
		
		
		session.setAttribute("message", new Message("Contact Delete Successfully !!", "success"));
		
		return "redirect:/user/show-contacts/1";
	}
	
	//update form handler...
	@PostMapping("/update-contact/{cid}")
	public String updateContact(@PathVariable("cid") Integer cid , Model model) {
		
		model.addAttribute("title", "Update Contact");
		
		Contact contact = contactRepoistory.findById(cid).get();
		
		model.addAttribute("contact", contact);
		
		return "normal/update_form";
	}
	
	//update contact handler
	@PostMapping("/process-update")
	public String updateContact(@ModelAttribute Contact contact , @RequestParam("profileImage") MultipartFile file , 
			Model model , HttpSession session , Principal principal ) 
	{
		
		try {
			
			//old contact details
			Contact oldContact = contactRepoistory.findById(contact.getcId()).get();
			
			
			//image
			if (!file.isEmpty())
			{
				//file work..
				//rewrite
				
				//delete old photo
				File deleteFile = new ClassPathResource("/static/image").getFile();
				File file2 = new File(deleteFile, oldContact.getImage());
				file2.delete();
						
				//update new photo
				File saveFile = new ClassPathResource("/static/image").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				
				
			}else {
				contact.setImage(oldContact.getImage());
			}
			
			User user = userRepoistory.getUserByUserName(principal.getName());
			contact.setUser(user);
			
			contactRepoistory.save(contact);
			
			session.setAttribute("message" , new Message("Contact Update successfully !! ", "success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	//your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {	
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}  
	
	//open setting handler
	@GetMapping("/settings")
	public String openSettings() {
		
		return "normal/settings";
	}
	
	//change password handler..
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {

		System.out.println("OLD PASSWORD : " + oldPassword);
		System.out.println("NEW PASSWORD : " + newPassword);

		String userName = principal.getName();
		User currentUser = userRepoistory.getUserByUserName(userName);
		System.out.println(currentUser.getPassword());

		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {

			// change the password

			currentUser.setPassword(bCryptPasswordEncoder.encode(newPassword));
			userRepoistory.save(currentUser);
			session.setAttribute("message", new Message("Password is Changed... ", "success"));
		} else {
			// error
			session.setAttribute("message", new Message("Please Enter Correct Old Password !! ", "danger"));
			return "redirect:/user/settings";
		}

		return "redirect:/user/index";
	}
}
