package com.a18.auth.model.repository;

import com.a18.auth.model.User;
import com.a18.common.constant.Privilege;
import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasAuthority('" + Privilege.READ + Privilege.USER + "')")
public interface UserRepository extends UserDetailsRepository<User, Long> {}
