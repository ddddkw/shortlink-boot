package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.enums.ShortLinkEnum;
import org.example.service.LogService;
import org.example.service.ShortLinkService;
import org.example.utils.CommonUtil;
import org.example.vo.ShortLinkVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@Slf4j
public class LinkApiController {

    @Autowired
    private ShortLinkService shortLinkService;

    @Autowired
    private LogService logService;

    @GetMapping(path = "/{shortLinkCode}")
    public void dispatch(@PathVariable(name = "shortLinkCode")String shortLinkCode,
                         HttpServletRequest request, HttpServletResponse response){
        try{
            // 判断短链码是否合规
            if (isLetterDigit(shortLinkCode)) {
                // 查找短链 TODO
                ShortLinkVo shortLinkVo = shortLinkService.parseShortLinkCode(shortLinkCode);
//                logService.recordShortLinkLog(request,shortLinkCode,shortLinkVo.getAccountNo());
                // 判断是否过期和可用
                if (isVisible(shortLinkVo)) {
                    String originUrl = CommonUtil.removeUrlPrefix(shortLinkVo.getOriginalUrl());
                    if (!originUrl.startsWith("http://") && !originUrl.startsWith("https://")) {
                        originUrl = "https://" + originUrl; // 或根据业务需求使用http
                    }
                    response.setHeader("Location",originUrl);
                    // 302跳转
                    response.setStatus(HttpStatus.FOUND.value());
                } else {
                    response.setStatus(HttpStatus.NOT_FOUND.value());
                }
            }
        }catch (Exception e){
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private static boolean isVisible(ShortLinkVo shortLinkVo){
        if (shortLinkVo!=null && shortLinkVo.getExpired().getTime()> CommonUtil.getCurrentTimestamp()||shortLinkVo.getExpired().getTime()==-1){
            if (ShortLinkEnum.ACTIVE.name().equals(shortLinkVo.getState())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isLetterDigit(String str){
        String regx = "^[a-z0-9A-Z]+$";
        return str.matches(regx);
    }

}
