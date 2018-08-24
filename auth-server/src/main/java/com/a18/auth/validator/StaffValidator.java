package com.a18.auth.validator;

import com.a18.auth.model.Staff;
import com.a18.auth.model.repository.StaffRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StaffValidator extends UserDetailsCommonValidator {

  @Autowired public StaffValidator(StaffRepository staffRepository) {
    super(staffRepository);
  }

  @Override public boolean supports(Class<?> clazz) {
    return Staff.class.equals(clazz);
  }
}
