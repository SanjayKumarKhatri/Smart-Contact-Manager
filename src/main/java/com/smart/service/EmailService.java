package com.smart.service;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

	public boolean sendEmail(String subject, String message, String to) {
		boolean f = false;
		String from = "skkhatri948@gmail.com";
		String gmailhost = "smtp.gmail.com";
		// get the system properties
		Properties properties = System.getProperties();
		System.out.println("Properties " + properties);

		properties.put("mail.smtp.host", gmailhost);
		properties.put("mail.smtp.port", "465");
		properties.put("mail.smtp.ssl.enable", "true");
		properties.put("mail.smtp.auth", "true");

		// Step 1 Create Session and get Session Object

		Session session = Session.getInstance(properties, new Authenticator() {

			/*
			 * (non-Javadoc)
			 * 
			 * @see javax.mail.Authenticator#getPasswordAuthentication()
			 */
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				// TODO Auto-generated method stub
				return new PasswordAuthentication("khatrisanjay106@gmail.com", "Sanjay123");
			}

		});
		session.setDebug(true);
		// Step 2 : Compose Message whether text or multimedia
		MimeMessage m = new MimeMessage(session);
		try {
			// from email
			m.setFrom(from);
			// Add Recipient
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			// Adding SUbject
			m.setSubject(subject);
			// Adding text to message
			// m.setText(message);
			m.setContent(message, "text/html");
			// Step 3 : Send the message using transport
			Transport.send(m);
			System.out.println("Send Success..");
			f = true;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return f;
	}
}
