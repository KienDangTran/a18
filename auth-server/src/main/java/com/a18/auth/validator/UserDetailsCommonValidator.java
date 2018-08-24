package com.a18.auth.validator;

import com.a18.auth.model.repository.UserDetailsRepository;
import com.a18.auth.model.AbstractUserDetails;
import com.a18.common.util.ClockProvider;
import java.time.LocalDate;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public abstract class UserDetailsCommonValidator implements Validator {
  /**
   * <pre>
   * ^                 # start-of-string
   * (?=.*[0-9])       # a digit must occur at least once
   * (?=.*[a-z])       # a lower case letter must occur at least once
   * (?=.*[A-Z])       # an upper case letter must occur at least once
   * (?=\S+$)          # no whitespace allowed in the entire string
   * .{8,}             # anything, at least eight places though
   * $                 # end-of-string
   * </pre>
   */
  public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$";

  private final UserDetailsRepository userDetailsRepository;

  public UserDetailsCommonValidator(UserDetailsRepository userDetailRepository) {
    this.userDetailsRepository = userDetailRepository;
  }

  @Override public abstract boolean supports(Class<?> clazz);

  @Override public void validate(Object target, Errors errors) {
    AbstractUserDetails user = (AbstractUserDetails) target;
    this.validateRequiredFields(errors);
    if (!errors.hasErrors()) {
      this.validateUsername(user.getUsername(), errors);
      this.validatePassword(user.getPassword(), errors);
      this.validateEmail(user.getEmail(), errors);
      this.validatePhone(user.getPhone(), errors);
      this.validateDateOfBirth(user.getDateOfBirth(), errors);
    }
  }

  private void validateRequiredFields(Errors errors) {
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "username",
        "common.field.required",
        new Object[] {"username"}
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "password",
        "common.field.required",
        new Object[] {"password"}
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "fullname",
        "common.field.required",
        new Object[] {"fullname"}
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "email",
        "common.field.required",
        new Object[] {"email"}
    );
    ValidationUtils.rejectIfEmptyOrWhitespace(
        errors,
        "phone",
        "common.field.required",
        new Object[] {"phone"}
    );
  }

  private void validateUsername(String username, Errors errors) {
    if (this.userDetailsRepository.countAllByUsername(username) > 0) {
      errors.rejectValue(
          "username",
          "user.info.username.existed",
          new Object[] {username},
          "user.info.username.existed"
      );
    }
  }

  private void validatePassword(String password, Errors errors) {
    if (!password.matches(PASSWORD_REGEX)) {
      errors.rejectValue(
          "password",
          "user.info.password.invalid"
      );
    }
  }

  private void validateEmail(String email, Errors errors) {
    if (this.userDetailsRepository.countAllByEmail(email) > 0) {
      errors.rejectValue(
          "email",
          "user.info.email.used",
          new Object[] {email},
          "user.info.email.used"
      );
    } else if (!email.matches("^[\\w-+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$")) {
      errors.rejectValue(
          "email",
          "user.info.email.invalid",
          new Object[] {email},
          "user.info.email.invalid"
      );
    }
  }

  private void validatePhone(String phone, Errors errors) {
    if (this.userDetailsRepository.countAllByPhone(phone) > 0) {
      errors.rejectValue(
          "phone",
          "user.info.phone.used",
          new Object[] {phone},
          "user.info.phone.used"
      );
    } else if (!phone.matches("\\d{10,13}|(?:\\d{3}-){2}\\d{4,7}|\\(\\d{3}\\)\\d{3}-?\\d{4,7}")) {
      errors.rejectValue(
          "phone",
          "user.info.invalid",
          new Object[] {phone},
          "user.info.invalid"
      );
    }
  }

  private void validateDateOfBirth(LocalDate dateOfBirth, Errors errors) {
    if (dateOfBirth != null && !dateOfBirth.isBefore(ClockProvider.today())) {
      errors.rejectValue(
          "dateOfBirth",
          "user.dateOfBirth.invalid",
          new Object[] {dateOfBirth},
          "user.dateOfBirth.invalid"
      );
    }
  }
}
