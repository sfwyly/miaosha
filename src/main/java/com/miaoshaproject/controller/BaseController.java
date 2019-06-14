package com.miaoshaproject.controller;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName BaseController
 * @Description TODO
 * @Author 逝风无言
 * @Data 2019/6/12 15:01
 * @Version 1.0
 **/
public class BaseController {
    //定义exceptionhandler解决未被controller吸收exception
//    @ExceptionHandler(Exception.class)
//    @ResponseStatus(HttpStatus.OK)
//    @ResponseBody
    public Object handlerException(HttpServletRequest request, Exception e){
        Map<String,Object> responseData = new HashMap<>();
        if(e instanceof BusinessException){
            BusinessException businessException = (BusinessException)e;

            responseData.put("errCode",businessException.getErrCode());
            responseData.put("errMsg",businessException.getErrMsg());
        }else{
            responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            responseData.put("errMsg",EmBusinessError.UNKNOWN_ERROR.getErrMsg());
        }



        return CommonReturnType.create(responseData,"fail");
    }
}
