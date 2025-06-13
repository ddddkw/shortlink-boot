package org.example.controller;


import org.example.service.DomainService;
import org.example.utils.JsonData;
import org.example.vo.DomainVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dkw
 * @since 2025-06-12
 */
@RestController
@RequestMapping("/domain")
public class DomainController {

    @Autowired
    private DomainService domainService;

    @GetMapping("/list")
    public JsonData listAll(){
        List<DomainVo> domainVoList = domainService.listAll();
        return JsonData.buildSuccess();
    }

}

