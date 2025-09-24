package cn.satoken.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaCheckSafe;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import cn.satoken.entity.Users;
import cn.satoken.mapper.UserRoleMapper;
import cn.satoken.service.IUsersService;
import cn.satoken.util.Login;
import cn.satoken.util.Result;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author
 * @since 2025-05-29
 */
@Validated
@RestController
@RequestMapping
public class UsersController {
    @Autowired
    private IUsersService usersService;
    @Autowired
    private UserRoleMapper userRoleMapper;

    //删除用户
    @DeleteMapping("/deleteUser")
    public Result deleteUser(@RequestParam List<Long> ids) {
        usersService.removeBatchByIds(ids);
        return Result.ok();
    }

    //获取用户分页列表
    @SaCheckPermission("user")
    @GetMapping("/getUserList")
    public Result getUserList(String search, Long pageNo, Long pageSize) {
        return usersService.getUserList(search, pageNo, pageSize);
    }

    // 新增或修改用户信息
    @PostMapping("/addOrUpdateUser")
    public Result addOrUpdateUser(@Valid @RequestBody Users user) {
        if (user.getId() == null) {
            // 新增用户，才加密密码
            user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        } else {
            // 修改用户，不加密密码
            user.setPassword(null);
        }
        usersService.saveOrUpdate(user);
        return Result.ok();
    }

    // 注册
    @PostMapping("/register")
    public Result register(@Valid @RequestBody Login login) {
        return usersService.register(login);
    }

    // 登录
    @PostMapping("/login")
    public Result login(@Valid @RequestBody Login login) {
        return usersService.login(login);
    }

    // 登出
    @GetMapping("/logout")
    public Result logout() {
        StpUtil.logout();
        return Result.ok();
    }

    // 开启二级认证
    @GetMapping("/openSafe")
    public Result openSafe(String password) {
        StpUtil.getTokenSession().set("password", password);
        return null;
//        return usersService.openSafe(password);
    }


    // 测试二级认证
    @SaCheckSafe
    @PostMapping("/important")
    public Result important() {
        return Result.ok();
    }

    // 测试权限
    @GetMapping("/role")
    @SaCheckRole("user")
    public Result role() {
        return Result.ok();
    }
}
