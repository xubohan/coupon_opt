package com.hexin.gift.interfaces.rest.vo;

public class BaseResponse<T> {

    private int errorCode;
    private String errorMsg;
    private T result;

    public BaseResponse() {
    }

    public BaseResponse(int errorCode, String errorMsg, T result) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.result = result;
    }

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, "OK", data);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
