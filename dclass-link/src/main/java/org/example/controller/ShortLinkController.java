package org.example.controller;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.example.entity.ShortLinkDO;
import org.example.params.ShortLinkAddParam;
import org.example.service.ShortLinkService;
import org.example.utils.JsonData;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dkw
 * @since 2025-06-05
 */
@RestController
@RequestMapping("/shortLink")
public class ShortLinkController {

    @Autowired
    private ShortLinkService shortLinkService;

    @PostMapping("/add")
    public JsonData add(ShortLinkAddParam shortLinkAddParam){
        ShortLinkDO shortLinkDO = new ShortLinkDO();
        BeanUtils.copyProperties(shortLinkAddParam,shortLinkDO);
        shortLinkService.addShortLink(shortLinkDO);
        return JsonData.buildSuccess();
    }
}

