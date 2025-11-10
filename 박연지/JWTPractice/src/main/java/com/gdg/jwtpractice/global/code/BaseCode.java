package com.gdg.jwtpractice.global.code;

public interface BaseCode {
    String getCode();
    String getMessage();
    ReasonDTO getReasonHttpStatus();
}
