package nz.ac.canterbury.seng302.homehelper.controller;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.homehelper.entity.Location;
import nz.ac.canterbury.seng302.homehelper.entity.users.Contractor;
import nz.ac.canterbury.seng302.homehelper.entity.users.User;
import nz.ac.canterbury.seng302.homehelper.repository.userRepositories.ContractorRepository;
import nz.ac.canterbury.seng302.homehelper.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Controller for the user profile page.
 */
@Controller
public class ProfileController {

	private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

	private final LoginService loginService;
	private final ContractorRepository contractorRepository;

	/**
	 * Induces spring to automatically set up the LoginService
	 * @param loginService The login service provides the function to get the current user
	 */
	@Autowired
	public ProfileController(LoginService loginService, ContractorRepository contractorRepository) {
		this.loginService = loginService;
		this.contractorRepository = contractorRepository;
	}

	/**
	 * Takes the user to the profile page when the "/user" URL is entered. Gets the information of the current user and displays
	 * it on the profileTemplate.html form.
	 * @param model Representation of results to be used by Thymeleaf
	 * @param request network request included to extract {@code Locale} of the request
	 * @return Thymeleaf profileTemplate
	 */
	@GetMapping("/user")
	public String userProfile(Model model, HttpServletRequest request) {
		logger.info("GET /user/");
		try {
			User user = loginService.getUserByEmail();
			model.addAttribute("firstName", user.getFirstName());
			model.addAttribute("lastName", user.getLastName());
			model.addAttribute("email", user.getEmail());
			model.addAttribute("dateAdded", user.getCreatedTimestamp().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
			model.addAttribute("profilePicture", user.getProfilePicture());
			Location location = user.getLocation();
			model.addAttribute("hasLocation", location != null);
			model.addAttribute("location", location);
			if (user instanceof Contractor contractor) {
				model.addAttribute("userType", "Contractor");
				model.addAttribute("phoneNumber", contractor.getPhoneNumberFormatted());
				model.addAttribute("hourlyRate", contractor.getHourlyRateFormatted(request.getLocale()));
				model.addAttribute("skills", contractor.getSkills());
				model.addAttribute("isAvailable", contractor.getAvailable());
				model.addAttribute("contractor", contractor);
			}
			return "profileTemplate";
		} catch (IllegalArgumentException e) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
		}
	}

	/**
	 * Returns the profile picture image
	 * @param filename profile picture file name
	 * @return the profile picture file
	 */
	@GetMapping({"/profile_pictures/{filename}", "/profile_pictures/default/{filename}"})
	@ResponseBody
	public ResponseEntity<Resource> getProfilePicture(@PathVariable String filename) {
		try {

			Path file;
			if (filename.equals("default.jpg")) {
				file = Paths.get("profile_pictures/default/").resolve("default.jpg").normalize();
			} else {
				file = Paths.get("profile_pictures/").resolve(filename).normalize();
			}

			Resource resource = new UrlResource(file.toUri());

			String imageType = Files.probeContentType(file);

			// Return the Image
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(imageType))
					.body(resource);

		} catch (Exception e) {
			// Return 404 not found error
			return ResponseEntity.notFound().build();
		}
	}


	/**
	 * Updates the availability status of a contractor.
	 *
	 * @param id      the contractor id
	 * @param payload a JSON map containing the new contractor availability status
	 * @return a redirect URL to the updated profile view
	 */
	@PostMapping("/editAvailability/{id}")
	public String submitAvailability(@PathVariable("id") Long id,
									 @RequestBody Map<String, Boolean> payload) {
		logger.info("POST editAvailability/{}", id);
		boolean isAvailable = payload.get("isAvailable");

		Contractor contractor = contractorRepository.findById(id).orElse(null);
        contractor.setAvailable(isAvailable);
		contractorRepository.save(contractor);
		return "redirect:/user";
	}
}
