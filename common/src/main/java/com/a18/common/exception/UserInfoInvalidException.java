package com.a18.common.exception;

import java.util.List;
import org.springframework.validation.FieldError;

public class UserInfoInvalidException extends RuntimeException {
  public List<FieldError> errors;

  public UserInfoInvalidException(List<FieldError> errors) {
    this.errors = errors;
  }
}
