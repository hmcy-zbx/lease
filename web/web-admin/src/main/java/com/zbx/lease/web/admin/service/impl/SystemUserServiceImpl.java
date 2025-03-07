package com.zbx.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zbx.lease.model.entity.BaseEntity;
import com.zbx.lease.model.entity.SystemUser;
import com.zbx.lease.web.admin.mapper.SystemPostMapper;
import com.zbx.lease.web.admin.mapper.SystemUserMapper;
import com.zbx.lease.web.admin.service.SystemUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zbx.lease.web.admin.vo.system.user.SystemUserItemVo;
import com.zbx.lease.web.admin.vo.system.user.SystemUserQueryVo;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author liubo
 * @description 针对表【system_user(员工信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class SystemUserServiceImpl extends ServiceImpl<SystemUserMapper, SystemUser>
        implements SystemUserService {

    @Autowired
    private SystemUserMapper systemUserMapper;

    @Autowired
    private SystemPostMapper systemPostMapper;

    @Override
    public IPage<SystemUserItemVo> pageSystemUserByQuery(IPage<SystemUser> page, SystemUserQueryVo queryVo) {
        return systemUserMapper.pageSystemUserByQuery(page,queryVo);
    }

    @Override
    public SystemUserItemVo getSystemUserById(Long id) {
        SystemUser systemUser = systemUserMapper.selectById(id);
        String postName = systemPostMapper.selectById(systemUser.getPostId()).getName();
        SystemUserItemVo systemUserItemVo = new SystemUserItemVo();
        BeanUtils.copyProperties(systemUser, systemUserItemVo);
        systemUserItemVo.setPostName(postName);
        return systemUserItemVo;
    }
}




