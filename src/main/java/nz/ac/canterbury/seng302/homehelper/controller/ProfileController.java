package nz.ac.canterbury.seng302.homehelper.controller;

import nz.ac.canterbury.seng302.homehelper.entity.User;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import java.time.format.DateTimeFormatter;

/**
 * Controller for the user profile page.
 */
@Controller
public class ProfileController {
	Logger logger = LoggerFactory.getLogger(ProfileController.class);

	private final LoginService loginService;

	/**
	 * Induces spring to automatically set up the LoginService
	 * @param loginService The login service provides the function to get the current user
	 */
	@Autowired
	public ProfileController(LoginService loginService) {
		this.loginService = loginService;
	}

	/**
	 * Takes the user to the profile page when the "/user" URL is entered. Gets the information of the current user and displays
	 * it on the profileTemplate.html form.
	 * @param model Representation of results to be used by Thymeleaf
	 * @return Thymeleaf profileTemplate
	 */
	@GetMapping("/user")
	public String userProfile(Model model) {
		logger.info("GET /user/");
		try {
			User user = loginService.getUserByEmail();
			model.addAttribute("firstName", user.getFirstName());
			model.addAttribute("lastName", user.getLastName());
			model.addAttribute("email", user.getEmail());
			model.addAttribute("dateAdded", user.getCreatedTimestamp().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
			return "profileTemplate";
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
		}
	}


}
