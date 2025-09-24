package cn.satoken.controller;


import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.satoken.entity.Roles;
import cn.satoken.entity.UserRole;
import cn.satoken.entity.Users;
import cn.satoken.mapper.RolePermissionMapper;
import cn.satoken.service.IPermissionsService;
import cn.satoken.service.IRolesService;
import cn.satoken.service.IUserRoleService;
import cn.satoken.util.Result;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    @Autowired
    private IPermissionsService permissionsService;

    //获取角色分页列表
    @SaCheckPermission("role")
    @GetMapping("/getRoleList")
    public Result getUserList(String search, Long pageNo, Long pageSize) {
        LambdaQueryChainWrapper<Roles> wrapper = rolesService.lambdaQuery().like(StrUtil.isNotBlank(search), Roles::getRole, search);
        if (pageNo.equals(-1L)) {
            pageNo = 1L;
            pageSize = wrapper.count();
        }
        Page<Roles> page = wrapper.page(Page.of(pageNo, pageSize));
        return Result.data(page.getTotal(), page.getRecords());
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

    @PostMapping("/addOrUpdateRole")
    public Result addRole(@RequestBody Roles role) {
        rolesService.saveOrUpdate(role);
        return Result.ok();
    }

    @DeleteMapping("/deleteRole")
    public Result deleteRole(@RequestParam List<Long> ids) {
        rolesService.removeByIds(ids);
        return Result.ok();
    }


}
