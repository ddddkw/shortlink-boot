package org.example.service;

import org.example.entity.AccountDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.params.LoginParam;
import org.example.params.RegisterParam;
import org.example.utils.JsonData;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dkw
 * @since 2025-05-20
 */
public interface AccountService extends IService<AccountDO> {

    JsonData register(RegisterParam registerParam);

    JsonData login(LoginParam loginParam);

}
