package com.hong_phat.crypto_trading.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;

import static com.hong_phat.crypto_trading.constants.CryptoTradingConstants.SUCCESS_CODE;
import static com.hong_phat.crypto_trading.constants.CryptoTradingConstants.SUCCESS_MESSAGE;

/**
 * The type Base response.
 *
 * @param <T> the type parameter
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> implements Serializable {
    private boolean success;
    private String code;
    private String des;
    private String messageCode;
    private T data;

    /**
     * Ok base response.
     *
     * @param <T>  the type parameter
     * @param data the data
     * @return the base response
     */
    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .success(true)
                .code(SUCCESS_CODE)
                .des(SUCCESS_MESSAGE)
                .messageCode(SUCCESS_CODE)
                .data(data)
                .build();
    }

    /**
     * Ok base response.
     *
     * @param <T> the type parameter
     * @return the base response
     */
    public static <T> BaseResponse<T> success() {
        return BaseResponse.<T>builder()
                .success(true)
                .code(SUCCESS_CODE)
                .des(SUCCESS_MESSAGE)
                .messageCode(SUCCESS_CODE)
                .build();
    }

    /**
     * Error base response.
     *
     * @param <T>     the type parameter
     * @param code    the code
     * @param message the message
     * @return the base response
     */
    public static <T> BaseResponse<T> error(String code, String message) {
        return BaseResponse.<T>builder()
                .success(false)
                .code(code)
                .des(message)
                .messageCode(code)
                .build();
    }
}
