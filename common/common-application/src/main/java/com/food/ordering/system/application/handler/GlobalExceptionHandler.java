package com.food.ordering.system.application.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

  @ResponseBody
  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public ErrorDTO handleException(Exception e) {
    log.error(e.getMessage(), e);
    return ErrorDTO.builder()
        .code(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
        .message("Unexpected error")
        .build();
  }

  @ResponseBody
  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ErrorDTO handleException(ValidationException e) {
    ErrorDTO errorDTO;

    if (e instanceof ConstraintViolationException validationException) {
      String violationMessage = validationException.getConstraintViolations().stream()
          .map(ConstraintViolation::getMessage)
          .collect(Collectors.joining(", "));
      log.error(violationMessage, validationException);

      errorDTO = ErrorDTO.builder()
          .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
          .message(violationMessage)
          .build();
    } else {
      log.error(e.getMessage(), e);
      errorDTO = ErrorDTO.builder()
          .code(HttpStatus.BAD_REQUEST.getReasonPhrase())
          .message(e.getMessage())
          .build();
    }

    return errorDTO;
  }
}
