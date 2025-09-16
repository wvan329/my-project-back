package cn.ai.service.impl;

import cn.dev33.satoken.annotation.SaCheckRole;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class ToolService {
    @Tool
    @SaCheckRole("user")
    public String getBalance(@ToolParam(description = "用户真实姓名") String name){
        System.out.println("1234");
        return "200";
    }
}
