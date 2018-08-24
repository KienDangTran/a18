package com.a18.auth.helper;

import com.a18.common.exception.ApiError;
import com.a18.common.exception.CommonExceptionHandler;
import com.a18.common.exception.UserInfoInvalidException;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class AuthExceptionHandler extends CommonExceptionHandler {

  @Autowired
  public AuthExceptionHandler(MessageSource messageSource) {
    super(messageSource);
  }

  @Override protected ResponseEntity<Object> handleMissingServletRequestParameter(
      MissingServletRequestParameterException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request
  ) {
    return buildResponseEntity(new ApiError(status, ex.getMessage()));
  }

  @ExceptionHandler(InvalidGrantException.class)
  public ResponseEntity handleInvalidGrantException(InvalidGrantException ex) {
    return this.buildResponseEntity(new ApiError(HttpStatus.FORBIDDEN, ex.getLocalizedMessage()));
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity handleInvalidTokenException(InvalidTokenException ex) {
    return this.buildResponseEntity(new ApiError(HttpStatus.FORBIDDEN, ex.getLocalizedMessage()));
  }

  @ExceptionHandler(UserInfoInvalidException.class)
  public ResponseEntity handleUserInfoInvalidException(
      UserInfoInvalidException ex,
      WebRequest request
  ) {
    ApiError apierror = new ApiError(
        HttpStatus.CONFLICT,
        this.messageSource.getMessage("user.info.is.invalid", null, request.getLocale())
    );
    apierror.addErrors(
        ex.errors
            .stream()
            .map(error -> new ApiError.ApiSubError(
                    error.getField(),
                    Objects.requireNonNull(error.getRejectedValue()).toString(),
                    this.messageSource.getMessage(
                        error.getCode(),
                        this.localizeParams(error.getArguments(), request.getLocale()),
                        request.getLocale()
                    ),
                    error.getCode()
                )
            )
            .collect(Collectors.toList())
    );
    return this.buildResponseEntity(apierror);
  }
}
