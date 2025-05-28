package org.example.controller;


import org.example.params.LoginParam;
import org.example.params.RegisterParam;
import org.example.service.AccountService;
import org.example.service.FileService;
import org.example.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dkw
 * @since 2025-05-20
 */
@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public JsonData uploadUserImage(@RequestPart MultipartFile file){
        String result = fileService.uploadUserImage(file);
        return result!=null?JsonData.buildSuccess(result):JsonData.buildError("文件上传失败");
    }

    /**
     *用户注册接口
     */
    @PostMapping("/register")
    public JsonData register(@RequestBody RegisterParam registerParam){
        JsonData jsonData = accountService.register(registerParam);
        return jsonData;
    }

    /**
     *用户登录接口
     */
    @PostMapping("/login")
    public JsonData login(@RequestBody LoginParam loginParam){
        JsonData jsonData = accountService.login(loginParam);
        return jsonData;
    }
}

