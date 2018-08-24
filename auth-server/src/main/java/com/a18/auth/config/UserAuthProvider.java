package com.a18.auth.config;

import com.a18.auth.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.stereotype.Component;

@Component
public class UserAuthProvider extends DaoAuthenticationProvider {

  @Autowired private UserService userService;

  @Override protected void doAfterPropertiesSet() throws Exception {
    super.setUserDetailsService(this.userService);
    super.doAfterPropertiesSet();
  }

  @Override public boolean supports(Class<?> authentication) {
    return super.supports(authentication);
  }
}