package org.example.service;

import org.example.entity.ShortLinkDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.vo.ShortLinkVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dkw
 * @since 2025-06-05
 */
public interface ShortLinkService extends IService<ShortLinkDO> {

    int addShortLink(ShortLinkDO shortLinkDO);

    ShortLinkDO findByShortLinkCode(String shortLinkCode);

    int delShortLink(String shortLinkCode, Long accountNo);

    ShortLinkVo parseShortLinkVo(String shortLinkCode);

}
