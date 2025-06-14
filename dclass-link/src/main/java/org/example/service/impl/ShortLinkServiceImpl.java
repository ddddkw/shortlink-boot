package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.ShortLinkDO;
import org.example.mapper.ShortLinkMapper;
import org.example.service.ShortLinkService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.vo.ShortLinkVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dkw
 * @since 2025-06-05
 */
@Service
@Slf4j
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {

    public int addShortLink(ShortLinkDO shortLinkDO){
        int rows = this.baseMapper.insert(shortLinkDO);
        return rows;
    }

    public ShortLinkDO findByShortLinkCode(String shortLinkCode){
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code",shortLinkCode);
        ShortLinkDO shortLinkDO = this.baseMapper.selectOne(queryWrapper);
        return shortLinkDO;
    }

    public int delShortLink(String shortLinkCode, Long accountNo){
        ShortLinkDO shortLinkDO = new ShortLinkDO();
        shortLinkDO.setDel(1);
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code",shortLinkCode);
        int rows = this.baseMapper.update(shortLinkDO,queryWrapper);
        return rows;
    }

    public ShortLinkVo parseShortLinkVo(String shortLinkCode){
        QueryWrapper queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("code",shortLinkCode);
        ShortLinkDO shortLinkDO = this.baseMapper.selectOne(queryWrapper);
        ShortLinkVo shortLinkVo = new ShortLinkVo();
        BeanUtils.copyProperties(shortLinkDO,shortLinkVo);
        return shortLinkVo;
    }

}
