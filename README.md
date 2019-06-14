# miaosha
基于springboot的秒杀系统  


## 项目重点思想说明  


### 数据模型  

>DataObject 数据对象 用于与数据库表元素对应  
>Model 模型层 用户service层封装DataObject进行处理  
>VO 数据显示层 对于Model数据并不一定需要所有数据都开放给用户（如密码等）仅仅将VO的属性元素数据传递给用户  
>CommonReturnType 对于后台向用户的响应结果统一封装为该类型进行返回

### 错误处理  

>CommonError接口用于封装同一错误规范（错误码以及错误信息）
```
public interface CommonError {
    public int getErrCode();
    public String getErrMsg();
    public CommonError setErrMsg(String errMsg);

}
```
>枚举类EmBusinessError实现CommonError接口，用于自定义响应错误
```
public enum EmBusinessError implements CommonError {
    //通用错误类型10001
    PARAMENER_VALIDATION_ERROR(10001,"参数不合法"),
    UNKNOWN_ERROR(10002,"未知错误"),
    //以10000开头为用户信息相关错误定义
    USER_NOT_EXIST(20001,"用户不存在"),
    USER_LOGIN_FAIL(20002,"用户手机号或密码不正确"),
    USER_NOT_LOGIN(20003,"用户还未登录"),
    //以30000开头为交易信息错误
    STOCK_NOT_ENOUGH(30001,"库存不足")
    ;
    private EmBusinessError(int errCode,String errMsg){
        this.errCode = errCode;
        this.errMsg=errMsg;
    }

    private int errCode;
    private String errMsg;
    
    //其他省略
}
```
>类BusinessException继承自Exception通用异常类并实现CommonError接口
>>该方法内部可以通过引用枚举类EmBusinessError的枚举常量来获得相应的错误码与错误方法，此方法还提供构造方法以自定义同一错误码的不同错误信息
```
public class BusinessException extends Exception implements CommonError {
    private CommonError commonError;

    //直接接收EmBusinessError的传参用于构造业务异常
    public BusinessException(CommonError commonError){
        super();
        this.commonError=commonError;
    }
    //接收自定义errMsg的方式构造业务异常
    public BusinessException(CommonError commonError ,String errMsg){
        super();
        this.commonError = commonError;
        this.commonError.setErrMsg(errMsg);
    }
    //getset省略
}
```

>对于controller异常通过springboot统一异常处理ExceptionHandler进行处理


### 模型校验

>对于用户输入数据传递到controller后封装成Model交由service层进行统一处理
>本系统对ItemModel进行校验主要通过Validator类进行，它实现了InitializingBean以对validator进行初始化操作
```
@Component
public class ValidatorImpl implements InitializingBean{

    private Validator validator;

    //实现检验方法并返回检验结果
    public ValidationResult validate(Object bean){
        ValidationResult result = new ValidationResult();
        Set<ConstraintViolation<Object>>constraintViolationSet =validator.validate(bean);
        if(constraintViolationSet.size()>0){
            //有错误
            result.setHasErrors(true);
            constraintViolationSet.forEach(constraintViolation->{
                String errMsg = constraintViolation.getMessage();
                String propertyName = constraintViolation.getPropertyPath().toString();
                result.getErrorMsgMap().put(propertyName,errMsg);
            });
        }
        return result;
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();//初始化
    }
}
```
>对于校验结果统一封装在ValidationResult中，该类两个字段分别记录是否有错误以及错误信息


