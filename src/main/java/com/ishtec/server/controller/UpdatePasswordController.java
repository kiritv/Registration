package com.ishtec.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import com.ishtec.server.exceptions.UpdatePasswordFailedException;
import com.ishtec.server.model.UpdatePassword;
import com.ishtec.server.service.UserService;
import com.ishtec.server.util.JwtUtil;

@Controller
@RequestMapping("/updatePassword")
@SessionAttributes("updatePassword")
public class UpdatePasswordController {
    private final UserService userService;
    private JwtUtil jwtTokenUtil;

    public UpdatePasswordController(UserService userService,
    		JwtUtil jwtTokenUtil) {
    	this.userService = userService;
    	this.jwtTokenUtil = jwtTokenUtil;
    }

    /**
     * resets password using user email link
     * @param model
     * @param token
     * @return presents form to reset user password
     */
	@RequestMapping(value = "/resetPassword", method = RequestMethod.GET)
	public String resetPassword(Model model, @RequestParam(name="token", required=true) String token) {
        final String username = jwtTokenUtil.getValidUsername(token);
        if(username != null) {
        	String newToken = jwtTokenUtil.generateTokenFromUsername(username);
        	UpdatePassword updatePassword = new UpdatePassword(username, null, newToken, null);
            model.addAttribute("updatePassword", updatePassword);
        }
        return "updatePassword";
    }
	

    @RequestMapping(method = RequestMethod.GET)
    public String setupForm(Model model)
    {
         model.addAttribute("updatePassword", new UpdatePassword());
         return "updatePassword";
    }
     
    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String submitForm(@ModelAttribute("updatePassword") UpdatePassword updatePassword, BindingResult result, SessionStatus status) {
    	//Validation code start
        boolean error = false;

        final String username = jwtTokenUtil.getValidUsername(updatePassword.getToken());
        if(username == null) {
            result.rejectValue("token", "error.token");
        	error = true;
        }

        System.out.println(updatePassword);

        if(updatePassword.getPassword().isEmpty()){
            result.rejectValue("password", "error.password");
            error = true;
        }
         
        if(updatePassword.getEmail().isEmpty()){
            result.rejectValue("email", "error.email");
            error = true;
        }
         
        if(updatePassword.getToken().isEmpty()){
            result.rejectValue("token", "error.token");
            error = true;
        }
         
        if(error) {
            return "updatePassword";
        }
        //validation code ends         
        if (!userService.updateUserPassword(updatePassword.getEmail(), updatePassword.getPassword())) {
            throw new UpdatePasswordFailedException();
        }

        //Mark Session Complete
        status.setComplete();
        updatePassword.setMessage("Password updated for " + updatePassword.getEmail());
        updatePassword.setEmail(null);
        updatePassword.setPassword(null);
        updatePassword.setToken(null);
        return "updatePassword";
    }

}
