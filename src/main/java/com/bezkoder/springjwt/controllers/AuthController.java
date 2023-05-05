package com.bezkoder.springjwt.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import com.bezkoder.springjwt.service.MfaService;
import com.bezkoder.springjwt.service.PasswordlessService;
import com.bezkoder.springjwt.service.UserService;
import com.bezkoder.springjwt.util.PasswordValidatorUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.*;

import com.bezkoder.springjwt.models.ERole;
import com.bezkoder.springjwt.models.Role;
import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.payload.request.LoginRequest;
import com.bezkoder.springjwt.payload.request.SignupRequest;
import com.bezkoder.springjwt.payload.response.JwtResponse;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import com.bezkoder.springjwt.repository.RoleRepository;
import com.bezkoder.springjwt.repository.UserRepository;
import com.bezkoder.springjwt.security.jwt.JwtUtils;
import com.bezkoder.springjwt.security.services.UserDetailsImpl;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
	@Autowired
	AuthenticationManager authenticationManager;

	@Autowired
	UserRepository userRepository;

	@Autowired
	RoleRepository roleRepository;

	@Autowired
	PasswordEncoder encoder;

	@Autowired
	JwtUtils jwtUtils;

	@Autowired
	UserService userService;


	@Autowired
	private MfaService mfaService;

	@Autowired
	private PasswordlessService passwordlessService;

	@PostMapping("/signin")
	public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);
		
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();		
		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());

		return ResponseEntity.ok(new JwtResponse(jwt, 
												 userDetails.getId(), 
												 userDetails.getUsername(), 
												 userDetails.getEmail(), 
												 roles));
	}

	@PostMapping("/signup")
	public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
		String validationResult = PasswordValidatorUtil.validatePassword(signUpRequest.getPassword());
		if (validationResult != null) {
			return new ResponseEntity<>("Invalid password: " + validationResult, HttpStatus.BAD_REQUEST);
		}
		if (userRepository.existsByUsername(signUpRequest.getUsername())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Username is already taken!"));
		}

		if (userRepository.existsByEmail(signUpRequest.getEmail())) {
			return ResponseEntity
					.badRequest()
					.body(new MessageResponse("Error: Email is already in use!"));
		}

		// Create new user's account
		User user = new User(signUpRequest.getUsername(), 
							 signUpRequest.getEmail(),
							 encoder.encode(signUpRequest.getPassword()));

		Set<String> strRoles = signUpRequest.getRole();
		Set<Role> roles = new HashSet<>();

		if (strRoles == null) {
			Role userRole = roleRepository.findByName(ERole.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
			roles.add(userRole);
		} else {
			strRoles.forEach(role -> {
				switch (role) {
				case "admin":
					Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(adminRole);

					break;
				case "mod":
					Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(modRole);

					break;
				default:
					Role userRole = roleRepository.findByName(ERole.ROLE_USER)
							.orElseThrow(() -> new RuntimeException("Error: Role is not found."));
					roles.add(userRole);
				}
			});
		}

		user.setRoles(roles);
		userRepository.save(user);

		return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
	}

	@PostMapping("/reset-password")
	public ResponseEntity<String> resetPassword(@RequestParam("email") String email,
												@RequestParam("newPassword") String newPassword) {
		User user = userService.findByEmail(email);
		if (user != null) {
			userService.changePassword(email, newPassword);
			return new ResponseEntity<>("Password reset successfully", HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@GetMapping("/logout")
	public ResponseEntity<String> logoutUser(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth != null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
			return new ResponseEntity<>("Logged out successfully", HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
	}

	// Add a new API endpoint for sending the MFA code after a successful login
	@PostMapping("/send-mfa-code")
	public ResponseEntity<String> sendMfaCode(@RequestBody User user) {
		User existingUser = userService.findByEmail(user.getUsername());
		if (existingUser != null) {
			mfaService.generateAndSendMfaCode(existingUser);
			return new ResponseEntity<>("MFA code sent", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
		}
	}

	// Add a new API endpoint for verifying the MFA code
	@PostMapping("/verify-mfa-code")
	public ResponseEntity<String> verifyMfaCode(@RequestBody User user, @RequestParam("code") String code) {
		User existingUser = userService.findByEmail(user.getUsername());
		if (existingUser != null) {
			if (mfaService.verifyMfaCode(existingUser, code)) {
				return new ResponseEntity<>("MFA code verified", HttpStatus.OK);
			} else {
				return new ResponseEntity<>("Invalid MFA code", HttpStatus.BAD_REQUEST);
			}
		} else {
			return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
		}
	}

	// Add a new API endpoint for generating and sending a passwordless login link
	@PostMapping("/send-login-link")
	public ResponseEntity<String> sendLoginLink(@RequestParam("email") String email) {
		User existingUser = userService.findByEmail(email);
		if (existingUser != null) {
			String loginLink = passwordlessService.generateLoginLink(existingUser);
			// Send the login link to the user, e.g., via email
			return new ResponseEntity<>("Login link sent", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
		}
	}

	// Add a new API endpoint for generating and sending a passwordless reset link
	@PostMapping("/send-reset-link")
	public ResponseEntity<String> sendResetLink(@RequestParam("email") String email) {
		User existingUser = userService.findByEmail(email);
		if (existingUser != null) {
			String resetLink = passwordlessService.generateResetLink(existingUser);
			// Send the reset link to the user, e.g., via email
			return new ResponseEntity<>("Reset link sent", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
		}
	}

	// Add a new API endpoint for processing the passwordless login link
	@GetMapping("/process-login-link/{token}")
	public ResponseEntity<String> processLoginLink(@PathVariable("token") String token) {
		User user = passwordlessService.verifyLoginLink(token);
		if (user != null) {
			// Log the user in and redirect them to the appropriate page
			return new ResponseEntity<>("Login successful", HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Invalid login link", HttpStatus.BAD_REQUEST);
		}
	}
}
