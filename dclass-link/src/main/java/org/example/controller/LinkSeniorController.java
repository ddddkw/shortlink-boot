package org.example.controller;

import org.example.entity.ShortLinkDO;
import org.example.params.ShortLinkAddParam;
import org.example.params.ShortLinkDelParam;
import org.example.params.ShortLinkUpdateParam;
import org.example.service.LinkSeniorService;
import org.example.utils.JsonData;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 锻炼相关接口（C端和B端）
 */

@RestController
@RequestMapping("/linkSenior")
public class LinkSeniorController {


    @Autowired
    private LinkSeniorService linkSeniorService;

    /**
     * 新增短链
     * @param shortLinkAddParam
     * @return
     */
    @PostMapping("/add")
    public JsonData add(@RequestBody ShortLinkAddParam shortLinkAddParam){
        ShortLinkDO shortLinkDO = new ShortLinkDO();
        BeanUtils.copyProperties(shortLinkAddParam,shortLinkDO);
        int rows = linkSeniorService.addLink(shortLinkDO);
        return rows==1?JsonData.buildSuccess():JsonData.buildError("新增失败");
    }

    /**
     * 删除短链
     * @return
     */
    @PostMapping("/del")
    public JsonData del(@RequestBody ShortLinkDelParam shortLinkDelParam){
        int rows = linkSeniorService.del(shortLinkDelParam);
        return rows==1?JsonData.buildSuccess():JsonData.buildError("新增失败");
    }

    /**
     * 更新短链
     * @return
     */
    @PostMapping("/update")
    public JsonData update(@RequestBody ShortLinkUpdateParam shortLinkUpdateParam){
        int rows = linkSeniorService.update(shortLinkUpdateParam);
        return rows==1?JsonData.buildSuccess():JsonData.buildError("新增失败");
    }

}
