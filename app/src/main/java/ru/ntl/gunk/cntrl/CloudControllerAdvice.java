package ru.ntl.gunk.cntrl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.ntl.gunk.dto.ErrorDTO;
import ru.ntl.gunk.sec.AuthException;

import javax.naming.AuthenticationException;
import java.util.NoSuchElementException;

@ControllerAdvice
public class CloudControllerAdvice {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorDTO> notFound(NoSuchElementException ex) {
        return templateError(HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler({AuthenticationException.class, AuthException.class})
    public ResponseEntity<ErrorDTO> authFailed(Exception ex){
        return templateError(HttpStatus.UNAUTHORIZED, ex);
    }

    private ResponseEntity<ErrorDTO> templateError(HttpStatus statusCode, Exception ex){
        return ResponseEntity.status(statusCode).body(ErrorDTO.builder()
                .id(statusCode.value())
                .message(ex.getLocalizedMessage())
                .build());
    }
}
