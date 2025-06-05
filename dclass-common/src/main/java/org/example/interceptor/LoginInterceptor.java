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

/**
 * HandlerInterceptor：Spring MVC 提供的拦截器接口，用于在请求处理的不同阶段（预处理、后处理、完成后）添加自定义逻辑。
 * LoginInterceptor：自定义登录验证拦截器，自定义实现HandlerInterceptor中的preHandle、postHandle、afterCompletion三个方法
 * Spring MVC 在处理请求时，会触发 LoginInterceptor 的 preHandle 方法进行验证。
 * 验证通过后，用户信息存入 ThreadLocal，供后续组件使用。
 * 请求处理完成后，afterCompletion 清理 ThreadLocal，释放资源。
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {

    public static ThreadLocal<LoginUser> threadLocal = new ThreadLocal<>();
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
                    accountNo(Long.valueOf(accountNo))
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
        CommonUtil.sendJsonMessage(response,JsonData.buildResult(BizCodeEnum.ACCOUNT_UNLOGIN));
        return false;
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
