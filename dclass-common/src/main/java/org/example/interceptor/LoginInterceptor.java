package org.example.interceptor;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.example.enums.BizCodeEnum;
import org.example.model.LoginUser;
import org.example.utils.CommonUtil;
import org.example.utils.JWTUtil;
import org.example.utils.JsonData;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    public ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (HttpMethod.OPTIONS.toString().equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpStatus.SC_NO_CONTENT);
            return true;
        }
        String token = request.getHeader("token");
        if (StringUtils.isBlank(token)) {
            token = request.getParameter("token");
        }
        if (StringUtils.isNotBlank(token)) {
            Claims claims = JWTUtil.checkJWT(token);
            if (claims==null) {
                CommonUtil.sendJsonMessage(response, JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
                return false;
            }

            String accountNo = claims.get("account_no").toString();
            String headImg = claims.get("head_img").toString();
            String username = claims.get("username").toString();
            String mail = claims.get("mail").toString();
            String phone = claims.get("phone").toString();
            String auth = claims.get("auth").toString();

            LoginUser loginUser = LoginUser.builder().
                    accountNo(accountNo)
                    .username(username)
                    .headImg(headImg)
                    .mail(mail)
                    .phone(phone).auth(auth).build();
            // 透传用户信息，两种方式
            // request.setAttribute("user",loginUser);
            /**
             * 将用户信息存储到当前线程的ThreadLocal中
             * 后续的控制器 / 服务可通过threadLocal.get()获取当前用户
             */
            threadLocal.set(loginUser);
            return true;
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        HandlerInterceptor.super.afterCompletion(request, response, handler, ex);
        threadLocal.remove();
    }
}
