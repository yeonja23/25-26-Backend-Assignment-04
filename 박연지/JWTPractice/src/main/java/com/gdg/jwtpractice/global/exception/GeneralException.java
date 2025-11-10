package com.gdg.jwtpractice.global.exception;

import com.gdg.jwtpractice.global.code.BaseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {
    private BaseCode code;

    public GeneralException(BaseCode code, Throwable cause) {
        super(code.getReasonHttpStatus().getMessage(), cause);
        this.code = code;
    }
}
