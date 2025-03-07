package com.zbx.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import com.zbx.lease.common.exception.LeaseException;
import com.zbx.lease.common.result.ResultCodeEnum;
import com.zbx.lease.common.utils.JwtUtil;
import com.zbx.lease.model.entity.SystemUser;
import com.zbx.lease.model.enums.BaseStatus;
import com.zbx.lease.web.admin.mapper.SystemUserMapper;
import com.zbx.lease.web.admin.service.LoginService;
import com.zbx.lease.web.admin.vo.login.CaptchaVo;
import com.zbx.lease.web.admin.vo.login.LoginVo;
import com.zbx.lease.web.admin.vo.system.user.SystemUserInfoVo;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.zbx.lease.common.constant.RedisConstant;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SystemUserMapper systemUserMapper;

    @Override
    public CaptchaVo getCaptcha() {
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);
        specCaptcha.setCharType(Captcha.TYPE_DEFAULT);

        String code = specCaptcha.text().toLowerCase();
        String key = RedisConstant.ADMIN_LOGIN_PREFIX + UUID.randomUUID();
        String image = specCaptcha.toBase64();
        redisTemplate.opsForValue().set(key, code, RedisConstant.ADMIN_LOGIN_CAPTCHA_TTL_SEC, TimeUnit.SECONDS);

        return new CaptchaVo(image, key);
    }

    @Override
    public String login(LoginVo loginVo) {

        if(loginVo.getCaptchaCode()==null){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_NOT_FOUND);
        }
        String code = redisTemplate.opsForValue().get(loginVo.getCaptchaKey());
        System.out.println(code);
        if(code==null){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);
        }
        if(!code.equals(loginVo.getCaptchaCode().toLowerCase())){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR);
        }

        SystemUser systemUser = systemUserMapper.selectOneByUserName(loginVo.getUsername());
        if(systemUser==null){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);
        }

        if(systemUser.getStatus() == BaseStatus.DISABLE){
            throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
        }

        if(systemUser.getPassword().equals(DigestUtils.md5Hex(loginVo.getPassword()))){}

        return JwtUtil.createToken(systemUser.getId(), loginVo.getUsername());
    }

    @Override
    public SystemUserInfoVo getLoginUserInfoById(Long userId) {
        SystemUser systemUser = systemUserMapper.selectById(userId);
        SystemUserInfoVo systemUserInfoVo = new SystemUserInfoVo();
        systemUserInfoVo.setName(systemUser.getName());
        systemUserInfoVo.setAvatarUrl(systemUser.getAvatarUrl());
        return systemUserInfoVo;
    }
}
