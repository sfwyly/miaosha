package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dao.UserPasswordDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.dataobject.UserPasswordDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @ClassName UserServiceImpl
 * @Description TODO
 * @Author 逝风无言
 * @Data 2019/6/9 20:51
 * @Version 1.0
 **/
@Service
public class UserServiceImpl  implements UserService{

    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Override
    public UserModel getUserById(Integer id) {
        //调用mapper
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if(userDO ==null){
            return null;
        }
        UserPasswordDO userPasswordDO= userPasswordDOMapper.selectByUserId(id);
        return convertFormDataObject(userDO,userPasswordDO);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException{
        if(userModel == null){
            throw new BusinessException(EmBusinessError.PARAMENER_VALIDATION_ERROR);
        }
//        if(StringUtils.isEmpty(userModel.getName())
//                ||userModel.getGender()==null
//                ||userModel.getAge()==null
//                ||StringUtils.isEmpty(userModel.getTelphone())){
//            throw new BusinessException(EmBusinessError.PARAMENER_VALIDATION_ERROR);
//        }
        ValidationResult result = validator.validate(userModel);
        if(result.isHasErrors()){
            throw new BusinessException(EmBusinessError.PARAMENER_VALIDATION_ERROR,result.getErrMsg());
        }

        //实现model->dataobject
        UserDO userDO = convertFormModel(userModel);
        try{
            userDOMapper.insertSelective(userDO);
        }catch(DuplicateKeyException e){
            throw new BusinessException(EmBusinessError.PARAMENER_VALIDATION_ERROR,"手机号已重复注册");
        }
        userModel.setId(userDO.getId());//设置用户id
        UserPasswordDO userPasswordDO = convertPasswordFormModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);

        return ;
    }

    @Override
    public UserModel validateLogin(String telphone, String encryPassword) throws BusinessException{
        //通过用户手机获取用户登录信息
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        if(userDO ==null){
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = convertFormDataObject(userDO,userPasswordDO);

        //比对用户信息内加密的密码是否和传输进来的密码相匹配
        System.out.println(encryPassword+" "+userModel.getEncryptPassword());

        if(!StringUtils.equals(encryPassword,userModel.getEncryptPassword())){
            throw new BusinessException(EmBusinessError.PARAMENER_VALIDATION_ERROR);
        }

        //
        return userModel;
    }

    private UserDO convertFormModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return userDO;
    }
    private UserPasswordDO convertPasswordFormModel(UserModel userModel){
        if(userModel ==null){
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncryptPassword());
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }

    private UserModel convertFormDataObject(UserDO userDO , UserPasswordDO userPasswordDO){

        if(userDO == null){
            return null;
        }

        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO,userModel);

        if(userPasswordDO != null){
            userModel.setEncryptPassword(userPasswordDO.getEncrptPassword());
        }

        return userModel;
    }
}
