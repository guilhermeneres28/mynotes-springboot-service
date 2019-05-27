package com.ctzn.mynotesservice.model.apimessage;

import com.ctzn.mynotesservice.model.apimessage.exceptions.HttpStatusContainer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiMessage> handleOtherExceptions(Exception ex) {
        String message = ex.getMessage();
        if (message == null) message = "Unknown error";
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        if (ex instanceof HttpStatusContainer) httpStatus = ((HttpStatusContainer) ex).getHttpStatus();
        return new ResponseEntity<>(new ApiMessage(message), httpStatus);
    }

}