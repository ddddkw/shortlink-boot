package org.example.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.example.model.LoginUser;

import java.util.Date;

@Slf4j
public class JWTUtil {

    // 加密密钥
    private static final String SUBJECT = "DD";

    // 加密密钥
    private static final String SECRET = "DD.168";

    // 令牌前缀
    private static final String TOKEN_PREFIX = "dd-short-link";

    // token过期时间
    private static final long EXPIRED = 1000 * 60 * 60 * 24 * 7;


    /**
     * 根据用户信息生成jwt token
     * @param loginUser
     * @return
     */
    public static String genreJsonWebToken(LoginUser loginUser){
        if (loginUser==null){
            throw new NullPointerException("对象为空");
        }
        String token = Jwts.builder().setSubject(SUBJECT)
                .claim("head_img",loginUser.getHeadImg())
                .claim("account_no",loginUser.getAccountNo())
                .claim("username",loginUser.getUsername())
                .claim("phone",loginUser.getPhone())
                .claim("mail",loginUser.getMail())
                .claim("auth",loginUser.getAuth())
                .setIssuedAt(new Date())
                .setExpiration(new Date(CommonUtil.getCurrentTimestamp()+EXPIRED))
                .signWith(SignatureAlgorithm.HS256,SECRET).compact();
        token =TOKEN_PREFIX +token;
        return token;
    }

    /**
     * 解密jwt
     * @param token
     * @return
     */
    public static Claims checkJWT(String token){
        try {
            Claims body = Jwts.parser().setSigningKey(SECRET).parseClaimsJwt(token.replace(TOKEN_PREFIX,"")).getBody();
            return body;
        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

}
