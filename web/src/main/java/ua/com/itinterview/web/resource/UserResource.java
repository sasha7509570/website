package ua.com.itinterview.web.resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import ua.com.itinterview.service.UserService;
import ua.com.itinterview.web.command.UserCommand;
import ua.com.itinterview.web.command.UserEditProfileCommand;
import ua.com.itinterview.web.resource.viewpages.ModeView;
import ua.com.itinterview.web.security.AuthenticationUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@Controller
public class UserResource extends ValidatedResource {

    @Autowired
    UserService userService;
    @Autowired
    AuthenticationUtils authenticationUtils;

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView getSignupUserPage() {
	return goToSignupPageWithCommand(new UserCommand(), ModeView.CREATE);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView createUser(
	    @Valid @ModelAttribute UserCommand userCommand,
	    BindingResult bindResult, HttpServletRequest request) {
	if (bindResult.hasErrors()) {
	    return goToSignupPageWithCommand(userCommand, ModeView.CREATE);
	}
	UserCommand newUserCommand = userService.createUser(userCommand);
	authenticationUtils.loginUser(userCommand.getEmail(), userCommand.getPassword(), request);
	return new ModelAndView("redirect:/user/" + newUserCommand.getId()
		+ "/view");

    }

    @PreAuthorize("#userId == principal.info.id")
    @RequestMapping(value = "/user/{id}/view", method = RequestMethod.GET)
    public String getViewUser(@PathVariable("id") Integer userId,
	    Map<String, Object> map) {
	UserCommand userCommand = userService.getUserById(userId);
	map.put("userCommand", userCommand);
	return "profile_page";
    }

    @PreAuthorize("#userId == principal.info.id")
    @RequestMapping(value = "/user/{id}/edit", method = RequestMethod.GET)
    public String getEditUser(@PathVariable("id") Integer userId,
	    Map<String, Object> map) {
	UserEditProfileCommand userEditProfileCommand = new UserEditProfileCommand(
		userService.getUserById(userId));
	map.put("userEditProfileCommand", userEditProfileCommand);
	return "profile_page";
    }

    @RequestMapping(value = "/user/{id}/save", method = RequestMethod.POST)
    public String saveUser(
	    @PathVariable("id") int userId,
	    @Valid @ModelAttribute UserEditProfileCommand userEditProfileCommand,
	    BindingResult bindResult, HttpServletRequest request,
	    Map<String, Object> map) {
	if (bindResult.hasErrors()) {
	    map.put("UserEditProfileCommand", userEditProfileCommand);
	    return "profile_page";
	}

	userService.updateUser(userId, userEditProfileCommand);
	return "redirect:/user/" + userId + "/view";
    }

    private ModelAndView goToSignupPageWithCommand(UserCommand userCommand,
	    ModeView modeView) {
	ModelAndView view = new ModelAndView("signup");
	view.addObject(userCommand);
	view.addObject("mode", modeView);
	return view;
    }

}
