package org.example.controller;


import org.example.enums.BizCodeEnum;
import org.example.params.LinkGroupAddParam;
import org.example.service.LinkGroupService;
import org.example.utils.JsonData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dkw
 * @since 2025-06-05
 */
@RestController
@RequestMapping("/linkGroup")
public class LinkGroupController {

    @Autowired
    private LinkGroupService linkGroupService;

    @PostMapping("/add")
    public JsonData add(@RequestBody LinkGroupAddParam linkGroupAddParam){
        int rows = linkGroupService.add(linkGroupAddParam);
        return rows ==1? JsonData.buildSuccess():JsonData.buildResult(BizCodeEnum.GROUP_OPER_FAIL);
    }

    @DeleteMapping("/del/{group_id}")
    public JsonData del(@PathVariable("group_id") Long groupId){
        int rows = linkGroupService.del(groupId);
        return rows ==1? JsonData.buildSuccess():JsonData.buildResult(BizCodeEnum.GROUP_OPER_FAIL);
    }

}

