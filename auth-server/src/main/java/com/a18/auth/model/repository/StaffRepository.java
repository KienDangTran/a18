package com.a18.auth.model.repository;

import com.a18.auth.model.Staff;
import com.a18.common.constant.Privilege;
import org.springframework.security.access.prepost.PreAuthorize;

@PreAuthorize("hasAuthority('" + Privilege.READ + Privilege.STAFF + "')")
public interface StaffRepository extends UserDetailsRepository<Staff, Long> {}
