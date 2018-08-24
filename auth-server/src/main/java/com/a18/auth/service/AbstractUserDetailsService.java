package com.a18.auth.service;

import com.a18.auth.model.AbstractUserDetails;
import com.a18.auth.model.repository.UserDetailsRepository;
import com.a18.auth.validator.UserDetailsCommonValidator;
import com.a18.common.util.StringUtil;
import java.io.Serializable;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MissingServletRequestParameterException;

@Service
public abstract class AbstractUserDetailsService<T extends AbstractUserDetails, ID extends Serializable, R extends UserDetailsRepository<T, ID>>
    implements UserDetailsService {

  private static final String CURRENT_PASSWORD_PARAM_NAME = "currentPassword";

  private static final String NEW_PASSWORD_PARAM_NAME = "newPassword";

  @Autowired private PasswordEncoder passwordEncoder;

  @Autowired private MessageSource messageSource;

  @Autowired private TokenBlackListService tokenBlackListService;

  @Autowired private DefaultTokenServices tokenServices;

  public abstract R getRepository();

  @Override public T loadUserByUsername(String input) {
    Optional<T> user = this.getRepository().findDistinctFirstByUsername(input);
    if (user.isPresent()) return user.get();
    throw new UsernameNotFoundException(this.messageSource.getMessage(
        "username.password.not.found",
        new Object[] {},
        LocaleContextHolder.getLocale()
    ));
  }

  public T createUserDetails(@NotNull T userDetails) {
    userDetails.setPassword(this.passwordEncoder.encode(userDetails.getPassword()));
    return this.getRepository().save(userDetails);
  }

  public void logoutAllSession(String username) {
    T user = this.loadUserByUsername(username);
    if (user == null) {
      throw new EntityNotFoundException(this.messageSource.getMessage(
          "username.not.found",
          new Object[] {username},
          LocaleContextHolder.getLocale()
      ));
    }
    this.tokenBlackListService.addAllToBlacklistByUsername(user.getUsername());
  }

  public void logoutCurrentSession(Authentication auth) {
    OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
    OAuth2AccessToken token = tokenServices.readAccessToken(details.getTokenValue());

    String jti = String.valueOf(token.getAdditionalInformation().get("jti"));
    if (StringUtil.isBlank(jti)) {
      throw new EntityNotFoundException("jti is missing");
    }
    this.tokenBlackListService.addToBlackList(jti);
  }

  @Transactional
  public void changePassword(HttpServletRequest request, String username)
      throws MissingServletRequestParameterException {
    T user = this.loadUserByUsername(username);
    if (user == null) {
      throw new EntityNotFoundException(this.messageSource.getMessage(
          "username.not.found",
          new Object[] {username},
          LocaleContextHolder.getLocale()
      ));
    }

    if (StringUtil.isBlank(request.getParameter(CURRENT_PASSWORD_PARAM_NAME))) {
      throw new MissingServletRequestParameterException(CURRENT_PASSWORD_PARAM_NAME, String.class.getSimpleName());
    }

    if (StringUtil.isBlank(request.getParameter(NEW_PASSWORD_PARAM_NAME))) {
      throw new MissingServletRequestParameterException(NEW_PASSWORD_PARAM_NAME, String.class.getSimpleName());
    }

    char[] currentPassword = request.getParameter(CURRENT_PASSWORD_PARAM_NAME).toCharArray();
    char[] newPassword = request.getParameter(NEW_PASSWORD_PARAM_NAME).toCharArray();
    if (newPassword.length <= 0) {
      throw new DataIntegrityViolationException("user.new.password.is.invalid");
    } else if (!String.valueOf(currentPassword).matches(UserDetailsCommonValidator.PASSWORD_REGEX)) {
      throw new DataIntegrityViolationException("user.info.password.invalid");
    }

    char[] dbPassword = this.getRepository().getPassword((ID) user.getId()).toCharArray();

    if (currentPassword.length <= 0
        || !this.passwordEncoder.matches(String.valueOf(currentPassword), String.valueOf(dbPassword))) {
      throw new DataIntegrityViolationException("user.current.password.is.not.match");
    }

    this.getRepository().changePassword(this.passwordEncoder.encode(String.valueOf(newPassword)), (ID) user.getId());

    this.tokenBlackListService.addAllToBlacklistByUsername(user.getUsername());
  }
}
