package com.a18.common.exception;

import java.nio.file.AccessDeniedException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.context.MessageSource;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.rest.core.RepositoryConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class CommonExceptionHandler extends ResponseEntityExceptionHandler {

  protected MessageSource messageSource;

  @Autowired
  public CommonExceptionHandler(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @InitBinder
  public void initBinder(WebDataBinder binder) {
    StringTrimmerEditor stringTrimmer = new StringTrimmerEditor(true);
    binder.registerCustomEditor(String.class, stringTrimmer);
  }

  /**
   * Happens when request JSON is malformed.
   */
  @Override protected ResponseEntity<Object> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex,
      HttpHeaders headers,
      HttpStatus status,
      WebRequest request
  ) {
    log.error(ex.getLocalizedMessage(), ex);
    return buildResponseEntity(new ApiError(HttpStatus.BAD_REQUEST, ex.getLocalizedMessage()));
  }

  @ExceptionHandler(RepositoryConstraintViolationException.class)
  protected ResponseEntity handleRepositoryConstraintViolationException(
      RepositoryConstraintViolationException ex,
      WebRequest request
  ) {
    ApiError apierror = new ApiError(HttpStatus.CONFLICT, ex.getLocalizedMessage());
    if (ex.getErrors().getFieldErrors().isEmpty()) {
      apierror.addErrors(
          ex.getErrors()
            .getFieldErrors()
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
    } else {
      apierror.addErrors(
          ex.getErrors().getFieldErrors().stream().map(fieldError -> {
            Object[] field = this.localizeParams(fieldError.getArguments(), request.getLocale());
            return new ApiError.ApiSubError(
                field[0].toString(),
                null,
                this.messageSource.getMessage(fieldError.getCode(), field, request.getLocale()),
                fieldError.getCode()
            );
          }).collect(Collectors.toList())
      );
    }

    return ResponseEntity.status(apierror.status).body(apierror);
  }

  @ExceptionHandler(AccessDeniedException.class)
  protected ResponseEntity handleAccessDeniedException(
      AccessDeniedException ex,
      WebRequest request
  ) {
    log.error(ex.getLocalizedMessage(), ex);
    return buildResponseEntity(new ApiError(HttpStatus.FORBIDDEN, ex.getLocalizedMessage()));
  }

  /**
   * Handle EntityNotFoundException
   */
  @ExceptionHandler(EntityNotFoundException.class)
  protected ResponseEntity handleEntityNotFound(EntityNotFoundException ex) {
    log.error(ex.getLocalizedMessage(), ex);
    return buildResponseEntity(new ApiError(HttpStatus.NOT_FOUND, ex.getLocalizedMessage(), ex));
  }

  /**
   * Handle DataIntegrityViolationException, inspects the cause for different DB causes.
   *
   * @param ex the DataIntegrityViolationException
   * @return the ApiError object
   */
  @ExceptionHandler(DataIntegrityViolationException.class)
  protected ResponseEntity handleDataIntegrityViolation(
      DataIntegrityViolationException ex,
      WebRequest request
  ) {
    if (ex.getCause() instanceof ConstraintViolationException) {
      return buildResponseEntity(
          new ApiError(HttpStatus.CONFLICT, ex.getCause().getCause().getLocalizedMessage())
      );
    }
    if (ex.getCause() instanceof DataException) {
      return buildResponseEntity(
          new ApiError(HttpStatus.CONFLICT, ex.getCause().getCause().getLocalizedMessage())
      );
    }

    log.error(ex.getLocalizedMessage(), ex);
    return buildResponseEntity(new ApiError(HttpStatus.CONFLICT, ex.getLocalizedMessage()));
  }

  @ExceptionHandler(HttpServerErrorException.class)
  protected ResponseEntity handleHttpServerErrorException(HttpServerErrorException ex) {
    log.error(ex.getLocalizedMessage(), ex);
    return this.buildResponseEntity(
        new ApiError(ex.getStatusCode(), ex.getLocalizedMessage(), ex)
    );
  }

  @ExceptionHandler(HttpClientErrorException.class)
  protected ResponseEntity handleHttpClientErrorException(HttpClientErrorException ex) {
    log.error(ex.getLocalizedMessage(), ex);
    return this.buildResponseEntity(
        new ApiError(ex.getStatusCode(), ex.getLocalizedMessage(), ex)
    );
  }

  @ExceptionHandler(Exception.class)
  protected ResponseEntity handleGenericExcetion(Exception ex) {
    log.error(ex.getLocalizedMessage(), ex);
    return this.buildResponseEntity(
        new ApiError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getCause().getLocalizedMessage(), ex)
    );
  }

  protected ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
    return ResponseEntity.status(apiError.status).body(apiError);
  }

  protected Object[] localizeParams(Object[] params, Locale locale) {
    if (params == null || params.length == 0) return new Object[] {""};
    return Arrays.stream(params)
                 .map(param -> param instanceof String
                               ? this.messageSource.getMessage(param.toString(), null, locale)
                               : param)
                 .toArray();
  }
}
