package org.example.controller;

import org.example.entity.ShortLinkDO;
import org.example.params.ShortLinkAddParam;
import org.example.params.ShortLinkDelParam;
import org.example.params.ShortLinkUpdateParam;
import org.example.service.LinkSeniorService;
import org.example.service.ShortLinkService;
import org.example.utils.JsonData;
import org.example.vo.ShortLinkVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * 锻炼相关接口（C端和B端）
 */

@RestController
@RequestMapping("/linkSenior")
public class LinkSeniorController {


    @Autowired
    private LinkSeniorService linkSeniorService;

    @Autowired
    private ShortLinkService shortLinkService;

    @Value("${rpc.token}")
    private String rpcToken;

    /**
     * 新增短链
     * @param shortLinkAddParam
     * @return
     */
    @PostMapping("/add")
    public JsonData add(@RequestBody ShortLinkAddParam shortLinkAddParam){
        return linkSeniorService.addLink(shortLinkAddParam);
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

    /**
     * 查询短链是否存在
     * @return
     */
    @PostMapping("/check")
    public JsonData check(@RequestParam("shortLinkCode") String shortLinkCode, HttpServletRequest request){
        String token = request.getHeader("rpc-token");
        if (rpcToken.equalsIgnoreCase(token)) {
            ShortLinkVo shortLinkVo = shortLinkService.parseShortLinkCode(shortLinkCode);
            return shortLinkVo==null?JsonData.buildError("短链不存在"):JsonData.buildSuccess();
        } else {
            return JsonData.buildError("非法访问");
        }
    }

}
