package com.hong_phat.crypto_trading.exception;

import com.hong_phat.crypto_trading.dto.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * The type Global exception handler.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle business exception response entity.
     *
     * @param e the ex
     * @return the response entity
     */
    @ExceptionHandler(CryptoTradingException.class)
    public ResponseEntity<BaseResponse<Void>> handleBusinessException(CryptoTradingException e) {
        return ResponseEntity.ok(BaseResponse.error(e.getCode(), e.getMessage()));
    }

    /**
     * Handle generic response entity.
     *
     * @param e the ex
     * @return the response entity
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleGeneric(Exception e) {
        return ResponseEntity.internalServerError()
                .body(BaseResponse.error("500", "An unexpected error occurred: " + e.getMessage()));
    }

    /**
     * Handle validation exception response entity.
     *
     * @param e the e
     * @return the response entity
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<BaseResponse<Void>> handleValidationException(BindException e) {

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse("Validation failed");

        return ResponseEntity.ok(BaseResponse.error("400", errorMessage));
    }
}
