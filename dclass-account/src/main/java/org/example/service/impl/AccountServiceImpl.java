package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.Md5Crypt;
import org.apache.commons.lang3.StringUtils;
import org.example.config.RabbitMQConfig;
import org.example.entity.AccountDO;
import org.example.enums.AuthTypeEnum;
import org.example.enums.BizCodeEnum;
import org.example.enums.EventMessageType;
import org.example.enums.SendCodeEnum;
import org.example.mapper.AccountMapper;
import org.example.model.EventMessage;
import org.example.model.LoginUser;
import org.example.params.LoginParam;
import org.example.params.RegisterParam;
import org.example.service.AccountService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.service.NotifyService;
import org.example.utils.CommonUtil;
import org.example.utils.IdUtil;
import org.example.utils.JWTUtil;
import org.example.utils.JsonData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dkw
 * @since 2025-05-20
 */
@Service
@Slf4j
public class AccountServiceImpl extends ServiceImpl<AccountMapper, AccountDO> implements AccountService {

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    //免费流量包的id都是1
    private static final Long FREE_TRAFFIC_PRODUCT_ID = 1L;

    /**
     * 注册账号时，会赠送免费流量包
     * @param registerParam
     * @return
     */
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRED)
    public JsonData register(RegisterParam registerParam){
        boolean checkCode = false;
        if (StringUtils.isNotBlank(registerParam.getPhone())) {
            checkCode = notifyService.checkCode(SendCodeEnum.USER_REGISTER,registerParam.getCode(), registerParam.getPhone());
        }
        if (!checkCode) {
            return JsonData.buildError("验证码错误");
        }
        AccountDO accountDO = new AccountDO();
        BeanUtils.copyProperties(registerParam,accountDO);

        // 认证级别
        accountDO.setAuth(AuthTypeEnum.DEFAULT.name());

        // 生成唯一的账号
        accountDO.setAccountNo(Long.valueOf(IdUtil.generateSnowFlakeKey().toString()));

        // 密码加密 密钥
        accountDO.setSecret("$1$"+ CommonUtil.getStringNumRandom(8));
        String cryptPwd = Md5Crypt.md5Crypt(registerParam.getPwd().getBytes(),accountDO.getSecret());
        accountDO.setPwd(cryptPwd);

        this.baseMapper.insert(accountDO);

        log.info("注册成功:{}",accountDO);

        userRegisterInitTask(accountDO);

        return JsonData.buildSuccess();
    }

    private void userRegisterInitTask(AccountDO accountDO){

        EventMessage eventMessage = EventMessage.builder()
                .messageId(IdUtil.generateSnowFlakeKey().toString())
                .accountNo(accountDO.getAccountNo())
                .eventMessageType(EventMessageType.TRAFFIC_FREE_INIT.name())
                .bizId(FREE_TRAFFIC_PRODUCT_ID.toString())
                .build();
        // 发送消息时，只需要三个参数，交换机，routingkey以及消息本体
        rabbitTemplate.convertAndSend(rabbitMQConfig.getTrafficEventExchange(),rabbitMQConfig.getTrafficFreeInitRoutingKey(),eventMessage);

    }

    public JsonData login(LoginParam loginParam){
        LambdaQueryWrapper<AccountDO> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AccountDO::getPhone,loginParam.getPhone());
        AccountDO accountDO = this.baseMapper.selectOne(lambdaQueryWrapper);
        if (accountDO==null){
            return JsonData.buildResult(BizCodeEnum.ACCOUNT_UNREGISTER);
        } else {
            String cryptPwd = Md5Crypt.md5Crypt(loginParam.getPwd().getBytes(),accountDO.getSecret());
            if (cryptPwd.equalsIgnoreCase(accountDO.getPwd())) {
                LoginUser loginUser = LoginUser.builder().build();
                BeanUtils.copyProperties(accountDO,loginUser);
                // 生成token
                String token = JWTUtil.genreJsonWebToken(loginUser);
                return JsonData.buildSuccess(token);
            } else {
                return JsonData.buildResult(BizCodeEnum.ACCOUNT_PWD_ERROR);
            }
        }
    }
}
