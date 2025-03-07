package com.zbx.lease.web.admin.service;

import com.zbx.lease.web.admin.vo.login.CaptchaVo;
import com.zbx.lease.web.admin.vo.login.LoginVo;
import com.zbx.lease.web.admin.vo.system.user.SystemUserInfoVo;

public interface LoginService {

    CaptchaVo getCaptcha();

    String login(LoginVo loginVo);

    SystemUserInfoVo getLoginUserInfoById(Long userId);
}
