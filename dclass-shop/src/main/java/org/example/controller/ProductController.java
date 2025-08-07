package org.example.controller;


import org.example.service.ProductService;
import org.example.utils.JsonData;
import org.example.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author dkw
 * @since 2025-06-20
 */
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 查询商品列表
     * @return
     */
    @GetMapping("/list")
    public JsonData list(){
        List<ProductVO> list = productService.queryList();
        return JsonData.buildSuccess(list);
    }

    /**
     * 查询商品详情
     * @return
     */
    @GetMapping("/detail/{product_id}")
    public JsonData detail(@PathVariable("product_id") Long id){
        ProductVO productVO = productService.findDetailById(id);
        return JsonData.buildSuccess(productVO);
    }

}

