package io.recallops.api;

import io.recallops.api.IncidentDtos.ErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.Instant;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({MethodArgumentNotValidException.class,ConstraintViolationException.class,HttpMessageNotReadableException.class,IllegalArgumentException.class})
    ResponseEntity<ErrorResponse> badRequest(Exception ex){return response(HttpStatus.BAD_REQUEST,"Check the request fields and try again.");}
    @ExceptionHandler(NoSuchElementException.class) ResponseEntity<ErrorResponse> missing(Exception ex){return response(HttpStatus.NOT_FOUND,ex.getMessage());}
    @ExceptionHandler(IllegalStateException.class) ResponseEntity<ErrorResponse> unavailable(Exception ex){return response(HttpStatus.SERVICE_UNAVAILABLE,ex.getMessage());}
    @ExceptionHandler(Exception.class) ResponseEntity<ErrorResponse> failed(Exception ex){return response(HttpStatus.INTERNAL_SERVER_ERROR,"The incident could not be processed.");}
    private ResponseEntity<ErrorResponse> response(HttpStatus status,String message){return ResponseEntity.status(status).body(new ErrorResponse(message,Instant.now()));}
}
