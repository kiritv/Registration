package com.ishtec.server.controller;

import com.ishtec.server.data.DataLoader;
import com.ishtec.server.model.UserRole;
import com.ishtec.server.types.ROLE_TYPE;
import com.ishtec.server.entities.UserProfile;
import com.ishtec.server.entities.UserSecret;
import com.ishtec.server.exceptions.CreateUserFailedException;
import com.ishtec.server.exceptions.PasswordValidationFailedException;
import com.ishtec.server.exceptions.UpdateLogoutFailedException;
import com.ishtec.server.exceptions.UpdatePasswordFailedException;
import com.ishtec.server.exceptions.UpdateUserFailedException;
import com.ishtec.server.exceptions.UserNotFoundException;
import com.ishtec.server.model.UserInfo;
import com.ishtec.server.model.UserLogin;
import com.ishtec.server.model.UserPasswordChange;
import com.ishtec.server.service.MyUserDetailsService;
import com.ishtec.server.service.UserService;
import com.ishtec.server.util.JwtUtil;

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.validation.Valid;
import javax.validation.constraints.Email;

import java.net.URI;
import java.security.Principal;
import java.util.Optional;

@RestController
@RequestMapping("/users")
// @Slf4j
public class UserController {

    private final UserService userService;
    private MyUserDetailsService userDetailsService;
    private JwtUtil jwtTokenUtil;

    public UserController(UserService userService, MyUserDetailsService userDetailsService, JwtUtil jwtTokenUtil) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * provides login functionality to user, authenticates user credentials
     *
     * @param userLogin
     * @return http ok on success and unauthorized on failure
     */
    @PostMapping(value = "/login", produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<String> loginUser(@RequestBody UserLogin userLogin) {
        Optional<UserSecret> userSecret = userService.validateEmailAndPassword(userLogin.getEmail(), userLogin.getPassword());
        if (userSecret.isPresent()) {
            final UserDetails userDetails = userDetailsService.loadUserByUsername(userLogin.getEmail());
            final String jwt = jwtTokenUtil.generateToken(userDetails);
            String jsonResponse = "{\"token\": \"" + jwt + "\"}";
            return ResponseEntity.ok(jsonResponse);
        }
        return new ResponseEntity<>("{\"status\": \"Unauthorized\"}", HttpStatus.UNAUTHORIZED);
    }

    /**
     * Registers user
     *
     * @param userInfo
     * @return valid registered user on success with http ok, http i'm used on failure
     */
    @PostMapping
    @ApiResponse(responseCode = "201")
    public ResponseEntity<String> create(@RequestBody UserInfo userInfo) {
        if (!userService.createUser(userInfo.getEmail(), userInfo.getPassword())) {
            throw new CreateUserFailedException();
        }
        if (!userService.updateUserProfile(userInfo.getEmail(), userInfo.getFirstName(), userInfo.getLastName())) {
            throw new UpdateUserFailedException("Account was created, but profile did not get updated with metadata");
        }
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{email}")
                .buildAndExpand(userInfo.getEmail()).toUri();
        return ResponseEntity.created(location).build();
    }

    /**
     * changes password for a user after validating existing credential
     *
     * @param userPasswordChange
     * @return http ok on success, forbidden in failure
     */
    @PutMapping("/password")
    public void updatePassword(Principal principal, @RequestBody @Valid UserPasswordChange userPasswordChange) {
        String email = principal.getName();
        if (!userService.validateEmailAndPassword(email, userPasswordChange.getOldPassword()).isPresent()) {
            throw new PasswordValidationFailedException();
        }
        if (!userService.updateUserPassword(email, userPasswordChange.getNewPassword())) {
            throw new UpdatePasswordFailedException();
        }
    }

    /**
     * provides login functionality to user after token retrieval request
     *
     * @param token
     * @return http ok on success and unauthorized on failure
     */
    @GetMapping(path = "/loginWithToken", produces = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<?> loginWithToken(@RequestParam("token") String token) {
        final String username = jwtTokenUtil.getValidUsername(token);
        if (username != null) {
            String jsonResponse = "{\"token\": \"" + token + "\"}";
            return ResponseEntity.ok(jsonResponse);
        }
        return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
    }

    /**
     * logout user and remove user's jwt from database so it can't be used anymore
     *
     * @return http ok on success
     */
    @PutMapping("/logout")
    public void logout(Principal principal) {
        String email = principal.getName();
        if (userService.clearUserTokens(email) == 0) {
            throw new UpdateLogoutFailedException();
        }
    }

    @GetMapping("/")
    public UserInfo getSingle(Principal principal) {
        String email = principal.getName();
        UserSecret userSecret = userService.findUserByEmail(email).orElseThrow(() -> new UserNotFoundException());

        UserProfile profile = userSecret.getUserProfile();
        UserInfo userInfo = new UserInfo();
        if (profile != null) {
            userInfo.setFirstName(profile.getFirstName());
            userInfo.setLastName(profile.getLastName());
        }
        userInfo.setEmail(userSecret.getEmail());
        return userInfo;
    }

    @PutMapping("/")
    public void update(Principal principal, @RequestBody @Valid UserInfo userInfo) {
        String email = principal.getName();
        if (!userService.updateUserProfile(email, userInfo.getFirstName(), userInfo.getLastName())) {
            throw new UpdateUserFailedException("Updating profile failed.");
        }
    }

    @PostMapping("/role")
    public ResponseEntity<?> addUserRole(Principal principal, @RequestBody UserRole userRole) {
        if (userDetailsService.hasRole(ROLE_TYPE.ROLE_ADMIN)
        	&& userService.addRoleMembership(userRole.getEmail(), userRole.getRoleName())) {
            return new ResponseEntity<>(DataLoader.SUCCESS_MESSAGE, HttpStatus.OK);
        }
        return new ResponseEntity<>(DataLoader.FAILURE_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @DeleteMapping("/role")
    public ResponseEntity<?> deleteUserRole(Principal principal, @RequestBody UserRole userRole) {
        if (userDetailsService.hasRole(ROLE_TYPE.ROLE_ADMIN)
                && userService.deleteRoleMembership(userRole.getEmail(), userRole.getRoleName())) {
            return new ResponseEntity<>(DataLoader.SUCCESS_MESSAGE, HttpStatus.OK);
        }
        return new ResponseEntity<>(DataLoader.FAILURE_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/role/{roleName}")
    public ResponseEntity<?> checkUserRole(Principal principal, @PathVariable("roleName") ROLE_TYPE roleName) {
    	String email = principal.getName();
        if (userService.existsRoleMembership(email, roleName)) {
            return new ResponseEntity<>(DataLoader.SUCCESS_MESSAGE, HttpStatus.OK);
        }
        return new ResponseEntity<>(DataLoader.FAILURE_MESSAGE, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * helper function to let user retrieve active token to login
     *
     * @return http ok on success
     */
    @GetMapping("/getToken")
    public ResponseEntity<?> getToken(@Email @RequestParam("email") String email) {
	    Optional<UserSecret> userSecret = userService.findUserByEmail(email);
	    if (userSecret.isPresent()) {
	        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
	        final String token = jwtTokenUtil.generateToken(userDetails);
	    	if(token != null) {
	            String location = ServletUriComponentsBuilder
	                    .fromCurrentRequest()
	                    .build()
	                    .toUri()
	                    .toString();
	            location = location.substring(0, location.indexOf("?"));
	            location = location.replace("users/getToken", "updatePassword/resetPassword") + "?token=" + token;
	            userService.sendResetPasswordMessage(email, location);
	            return new ResponseEntity<>("Email sent", HttpStatus.OK);
	    	}
	    }
        return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
    }

    
}