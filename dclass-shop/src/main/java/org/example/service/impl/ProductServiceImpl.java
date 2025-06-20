package org.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.ProductDO;
import org.example.mapper.ProductMapper;
import org.example.service.ProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.vo.ProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author dkw
 * @since 2025-06-20
 */
@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, ProductDO> implements ProductService {

    public List<ProductVO> queryList(){
        QueryWrapper queryWrapper = new QueryWrapper<>();
        List<ProductDO> list = this.baseMapper.selectList(queryWrapper);
        List<ProductVO> collect  = list.stream().map(obj->
             beanProcess(obj)).collect(Collectors.toList());
        return collect;
    }

    public ProductVO findDetailById(Long id){
        ProductDO productDO = this.baseMapper.selectById(id);
        ProductVO productVO = beanProcess(productDO);
        return productVO;
    }

    private ProductVO beanProcess(ProductDO productDO){
        ProductVO productVO = new ProductVO();
        BeanUtils.copyProperties(productDO,productVO);
        return productVO;
    }
}
