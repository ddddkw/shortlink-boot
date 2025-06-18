package org.example.controller;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.example.config.RabbitMQConfig;
import org.example.entity.ShortLinkDO;
import org.example.params.ShortLinkAddParam;
import org.example.service.LinkSeniorService;
import org.example.service.ShortLinkService;
import org.example.utils.JsonData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author dkw
 * @since 2025-06-05
 */
@RestController
@RequestMapping("/shortLink")
public class ShortLinkController {

//    @Autowired
//    private LinkSeniorService linkSeniorService;
//
//    /**
//     * 新增短链
//     * @param shortLinkAddParam
//     * @return
//     */
//    @PostMapping("/add")
//    public JsonData add(@RequestBody ShortLinkAddParam shortLinkAddParam){
//        ShortLinkDO shortLinkDO = new ShortLinkDO();
//        BeanUtils.copyProperties(shortLinkAddParam,shortLinkDO);
//        int rows = linkSeniorService.addLink(shortLinkDO);
//        return rows==1?JsonData.buildSuccess():JsonData.buildError("新增失败");
//    }
}

