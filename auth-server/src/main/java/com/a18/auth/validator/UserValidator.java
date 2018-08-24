package com.a18.auth.validator;

import com.a18.auth.model.repository.UserRepository;
import com.a18.auth.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserValidator extends UserDetailsCommonValidator {

  @Autowired public UserValidator(UserRepository userRepository) {
    super(userRepository);
  }

  @Override public boolean supports(Class<?> clazz) {
    return User.class.equals(clazz);
  }
}
