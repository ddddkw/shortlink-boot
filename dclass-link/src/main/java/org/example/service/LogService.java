package org.example.service;

import org.example.utils.JsonData;

import javax.servlet.http.HttpServletRequest;

public interface LogService {

    /**
     * 记住短链码的日志
     * @param request
     * @param shortLinkCode
     * @param accountNo
     * @return
     */
    void recordShortLinkLog(HttpServletRequest request, String shortLinkCode,Long accountNo);
}
