package cn.satoken.controller;


import cn.hutool.core.util.ObjectUtil;
import cn.satoken.entity.Roles;
import cn.satoken.entity.UserRole;
import cn.satoken.entity.Users;
import cn.satoken.service.IRolesService;
import cn.satoken.service.IUserRoleService;
import cn.satoken.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author
 * @since 2025-05-30
 */
@RestController
@RequestMapping("/roles")
public class RolesController {
    @Autowired
    private IRolesService rolesService;
    @Autowired
    private IUserRoleService userRoleService;

    //获取所有角色
    @GetMapping("/getAll")
    public Result getAll() {
        return Result.data(rolesService.list().stream().map(Roles::getRole).toList());
    }

    //更新角色
    @PostMapping("/updateRole")
    public Result updateRole(@RequestBody Users user) {
        if (ObjectUtil.isEmpty(user.getRoles())) {
            userRoleService.lambdaUpdate().eq(UserRole::getUserId, user.getId()).remove();
            return Result.ok();
        }
        List<Long> idList = rolesService.lambdaQuery().in(Roles::getRole, user.getRoles())
                .list().stream().map(Roles::getId).toList();
        List<UserRole> userRoleList = new ArrayList<>();
        for (Long id : idList) {
            userRoleList.add(new UserRole(user.getId(), id));
        }
        userRoleService.lambdaUpdate().eq(UserRole::getUserId, user.getId()).remove();
        userRoleService.saveBatch(userRoleList);
        return Result.ok();
    }
}
