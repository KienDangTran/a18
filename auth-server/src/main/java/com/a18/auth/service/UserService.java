package com.a18.auth.service;

import com.a18.auth.model.repository.UserRepository;
import com.a18.auth.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService extends AbstractUserDetailsService<User, Long, UserRepository> {

  @Autowired private UserRepository userRepository;

  @Override public UserRepository getRepository() {
    return userRepository;
  }
}
