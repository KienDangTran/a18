package com.a18.auth.controller;

import com.a18.auth.config.OwnerPasswordTokenGranter;
import com.a18.auth.model.Staff;
import com.a18.auth.service.StaffService;
import com.a18.auth.validator.StaffValidator;
import com.a18.common.constant.Privilege;
import com.a18.common.exception.ApiError;
import com.a18.common.exception.UserInfoInvalidException;
import java.net.URI;
import java.security.Principal;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RepositoryRestController
public class StaffController {
  public static final String PATH_STAFF = "/staffs";

  public static final String PATH_STAFF_LOGIN = PATH_STAFF + "/login";

  public static final String PATH_STAFF_NEW = PATH_STAFF + "/new";

  private static final String PATH_STAFF_LOGOUT_ALL = PATH_STAFF + "/logoutAll";

  private static final String PATH_STAFF_LOGOUT = PATH_STAFF + "/logout";

  private static final String PATH_STAFF_CHANGE_PASSWORD = PATH_STAFF + "/changePassword";

  private final OwnerPasswordTokenGranter tokenGranter;

  private final StaffValidator staffValidator;

  private final StaffService staffService;

  @Autowired public StaffController(
      OwnerPasswordTokenGranter tokenGranter,
      StaffValidator staffValidator,
      StaffService staffService
  ) {
    this.tokenGranter = tokenGranter;
    this.staffValidator = staffValidator;
    this.staffService = staffService;
  }

  @InitBinder("staff")
  public void setupBinder(WebDataBinder binder) {
    binder.addValidators(staffValidator);
  }

  @PreAuthorize("hasAuthority('" + Privilege.WRITE + Privilege.STAFF + "')")
  @PostMapping(PATH_STAFF_NEW)
  public ResponseEntity createStaff(@RequestBody @Valid Staff staff, BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      throw new UserInfoInvalidException(bindingResult.getFieldErrors());
    }

    staff = this.staffService.createUserDetails(staff);

    URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
                                              .path(PATH_STAFF + "/{id}")
                                              .buildAndExpand(staff.getId())
                                              .toUri();

    return ResponseEntity.created(location).body(staff);
  }

  @PostMapping(value = PATH_STAFF_LOGIN, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  public ResponseEntity login(Authentication authentication) {
    OAuth2AccessToken token = this.tokenGranter.grant(authentication);

    return token == null
           ? ResponseEntity.badRequest().body(new ApiError(HttpStatus.BAD_REQUEST, "username.password.not.found"))
           : ResponseEntity.ok().body(token);
  }

  @PostMapping(PATH_STAFF_LOGOUT_ALL)
  public ResponseEntity logoutAllSession(Principal principal) {
    this.staffService.logoutAllSession(principal.getName());
    return ResponseEntity.noContent().build();
  }

  @PostMapping(PATH_STAFF_LOGOUT)
  public ResponseEntity logoutCurrentSession(Authentication auth) {
    this.staffService.logoutCurrentSession(auth);
    return ResponseEntity.noContent().build();
  }

  @PostMapping(PATH_STAFF_CHANGE_PASSWORD)
  public ResponseEntity changePassword(HttpServletRequest request, Principal principal)
      throws MissingServletRequestParameterException {
    this.staffService.changePassword(request, principal.getName());
    return ResponseEntity.ok().build();
  }
}
