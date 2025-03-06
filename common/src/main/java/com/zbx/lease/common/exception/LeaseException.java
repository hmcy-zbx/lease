package com.zbx.lease.common.exception;

import lombok.Data;
import com.zbx.lease.common.result.ResultCodeEnum;
@Data
public class LeaseException extends RuntimeException {
    private Integer code;
    public LeaseException(Integer code, String message) {
        super(message);
        this.code = code;
    }
    public LeaseException(ResultCodeEnum resultCodeEnum) {
        super(resultCodeEnum.getMessage());
        this.code = resultCodeEnum.getCode();
    }
}
