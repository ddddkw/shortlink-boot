package org.example.service;

import org.example.entity.ProductDO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.example.model.EventMessage;
import org.example.vo.ProductVO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author dkw
 * @since 2025-06-20
 */
public interface ProductService extends IService<ProductDO> {

    List<ProductVO> queryList();

    ProductVO findDetailById(Long id);

}
