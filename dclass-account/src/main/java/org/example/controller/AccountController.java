package org.example.controller;


import org.example.service.FileService;
import org.example.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
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
    private FileService fileService;

    @PostMapping("/upload")
    public JsonData uploadUserImage(@RequestPart MultipartFile file){
        String result = fileService.uploadUserImage(file);
        return result!=null?JsonData.buildSuccess(result):JsonData.buildError("文件上传失败");
    }
}

