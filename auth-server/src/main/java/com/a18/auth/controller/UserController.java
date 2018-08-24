package com.a18.auth.controller;

import com.a18.auth.config.OwnerPasswordTokenGranter;
import com.a18.auth.service.UserService;
import com.a18.auth.validator.UserValidator;
import com.a18.auth.model.User;
import com.a18.common.constant.Privilege;
import com.a18.common.exception.ApiError;
import com.a18.common.exception.UserInfoInvalidException;
import com.a18.common.util.StringUtil;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RepositoryRestController
public class UserController {

  public static final String PATH_USER = "/users";

  public static final String PATH_REGISTER = PATH_USER + "/register";

  public static final String PATH_USER_LOGIN = PATH_USER + "/login";

  private static final String PATH_USER_LOGOUT_ALL = PATH_USER + "/logoutAll";

  private static final String PATH_USER_LOGOUT = PATH_USER + "/logout";

  public static final String PATH_USER_CHANGE_PASSWORD = PATH_USER + "/changePassword";

  private final UserValidator userValidator;

  private final UserService userService;

  private final OwnerPasswordTokenGranter tokenGranter;

  @Autowired public UserController(
      UserValidator userValidator,
      UserService userService,
      OwnerPasswordTokenGranter tokenGranter
  ) {
    this.userValidator = userValidator;
    this.userService = userService;
    this.tokenGranter = tokenGranter;
  }

  @InitBinder("user")
  public void setupBinder(WebDataBinder binder) {
    binder.addValidators(userValidator);
  }

  @PostMapping(PATH_REGISTER)
  public ResponseEntity registerAccount(@Valid @RequestBody User user, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      throw new UserInfoInvalidException(bindingResult.getFieldErrors());
    }

    user = this.userService.createUserDetails(user);
    URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                                              .path(PATH_USER + "/{id}")
                                              .buildAndExpand(user.getId())
                                              .toUri();

    return ResponseEntity.created(location).body(user);
  }

  @PostMapping(path = PATH_USER_LOGIN, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity login(HttpServletRequest request) throws MissingServletRequestParameterException {
    String username = request.getParameter(OwnerPasswordTokenGranter.USERNAME_PARAM_NAME);
    String password = request.getParameter(OwnerPasswordTokenGranter.PASSWORD_PARAM_NAME);

    if (StringUtil.isBlank(username) || StringUtil.isBlank(password)) {
      throw new MissingServletRequestParameterException(
          OwnerPasswordTokenGranter.USERNAME_PARAM_NAME + "/" + OwnerPasswordTokenGranter.PASSWORD_PARAM_NAME,
          String.class.getSimpleName()
      );
    }

    Authentication auth = new UsernamePasswordAuthenticationToken(
        username,
        password,
        List.of(new SimpleGrantedAuthority(Privilege.read(Privilege.USER)))
    );

    SecurityContextHolder.getContext().setAuthentication(auth);

    OAuth2AccessToken token = this.tokenGranter.grant(auth);

    if (token == null) {
      SecurityContextHolder.clearContext();
      return ResponseEntity.badRequest().body(new ApiError(HttpStatus.BAD_REQUEST, "username.password.not.found"));
    }
    return ResponseEntity.ok().body(token);
  }

  @PostMapping(PATH_USER_LOGOUT_ALL)
  public ResponseEntity logoutAllSession(Principal principal) {
    this.userService.logoutAllSession(principal.getName());
    return ResponseEntity.noContent().build();
  }

  @PostMapping(PATH_USER_LOGOUT)
  public ResponseEntity logoutCurrentSession(Authentication auth) {
    this.userService.logoutCurrentSession(auth);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(PATH_USER_CHANGE_PASSWORD)
  public ResponseEntity changePassword(HttpServletRequest request, Principal principal)
      throws MissingServletRequestParameterException {
    this.userService.changePassword(request, principal.getName());
    return ResponseEntity.ok().build();
  }
}
