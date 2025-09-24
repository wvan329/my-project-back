package cn.satoken.service.impl;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.satoken.entity.Roles;
import cn.satoken.entity.UserRole;
import cn.satoken.entity.Users;
import cn.satoken.mapper.UserRoleMapper;
import cn.satoken.mapper.UsersMapper;
import cn.satoken.service.IRolesService;
import cn.satoken.service.IUserRoleService;
import cn.satoken.service.IUsersService;
import cn.satoken.util.CaptchaController;
import cn.satoken.util.Login;
import cn.satoken.util.MyException;
import cn.satoken.util.Result;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author
 * @since 2025-05-29
 */
@Service
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {
    @Autowired
    private CaptchaController captchaController;
    @Autowired
    private IRolesService rolesService;
    @Autowired
    private IUserRoleService userRoleService;
    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Result register(Login login) {
        // 校验验证码
        captchaController.verifyCaptcha(login.getCaptchaId(), login.getCaptcha());
        //用户名是否存在
        if (lambdaQuery().eq(Users::getUsername, login.getUsername()).exists()) {
            throw new MyException("用户名已存在");
        }
        //注册用户
        Users user = Users.builder().username(login.getUsername())
                .password(BCrypt.hashpw(login.getPassword(), BCrypt.gensalt()))
                .birthday(login.getBirthday())
                .build();
        save(user);
        //保存用户-角色信息
        userRoleService.save(UserRole.builder()
                .userId(user.getId())
                .roleId(rolesService.lambdaQuery().eq(Roles::getRole, "user").one().getId())
                .build());
        return Result.ok();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Result login(Login login) {
        // 校验验证码
        captchaController.verifyCaptcha(login.getCaptchaId(), login.getCaptcha());
        Users user = lambdaQuery().eq(Users::getUsername, login.getUsername()).one();
        if (ObjUtil.isEmpty(user)) {
            throw new MyException("用户名不存在");
        }
        setRoleAndPermissions(user);
        //校验用户名密码
        if (BCrypt.checkpw(login.getPassword(), user.getPassword())) {
            StpUtil.login(user.getId());
            SaSession session = StpUtil.getSession();
            session.set("user", user);
            return Result.data(user.setToken(StpUtil.getTokenValue()));
        }
        throw new MyException("密码错误");
    }

    public void setRoleAndPermissions(Users user) {
        List<UserRole> roleIds = userRoleService.lambdaQuery().eq(UserRole::getUserId, user.getId()).list();
        List<Roles> roleList = new ArrayList<>();
        if (ObjUtil.isNotEmpty(roleIds)) {
            roleList = rolesService.lambdaQuery().in(Roles::getId, roleIds.stream().map(UserRole::getRoleId).toList()).list();
        }
        List<String> roles = roleList.stream().map(Roles::getRole).toList();
        Set<String> permissions = new HashSet<>();
        for (Roles role : roleList) {
            if (ObjUtil.isNotEmpty(role.getPermissions())) {
                permissions.addAll(role.getPermissions());
            }
        }
        user.setRoles(roles).setPermissions(new ArrayList<>(permissions));
    }

    @Override
    public Result openSafe(String password) {
        if (BCrypt.checkpw(password, ((Users) StpUtil.getSession().get("user")).getPassword())) {
            // 比对成功，为当前会话打开二级认证，有效期为120秒
            StpUtil.openSafe(120);
            return Result.ok();
        }
        // 如果密码校验失败，则二级认证也会失败
        throw new MyException("二级校验失败");
    }

    @Override
//    @MyPage
    public Result getUserList(String search, Long pageNo, Long pageSize) {
        LambdaQueryChainWrapper<Users> wrapper = lambdaQuery().like(StrUtil.isNotBlank(search), Users::getUsername, search);
        if (pageNo.equals(-1L)) {
            pageNo = 1L;
            pageSize = wrapper.count();
        }
        Page<Users> page = wrapper.page(Page.of(pageNo, pageSize));
        for (Users user : page.getRecords()) {
            setRoleAndPermissions(user);
        }
        return Result.data(page.getTotal(), page.getRecords());
    }

}
