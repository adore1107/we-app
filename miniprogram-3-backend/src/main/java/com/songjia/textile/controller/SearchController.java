package com.songjia.textile.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.songjia.textile.common.Result;
import com.songjia.textile.entity.Product;
import com.songjia.textile.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;

/**
 * 商品搜索控制器
 */
@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final ProductService productService;

    /**
     * 搜索商品 - 商品名称模糊搜索
     */
    @GetMapping("/products")
    public Result<IPage<Product>> searchProducts(
            @RequestParam @NotBlank String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "20") int pageSize) {
        try {
            log.info("搜索商品: keyword={}, pageNum={}, pageSize={}", keyword, pageNum, pageSize);

            IPage<Product> result = productService.searchProductsByName(keyword, pageNum, pageSize);

            return Result.success("搜索成功", result);
        } catch (Exception e) {
            log.error("搜索商品失败: keyword={}", keyword, e);
            return Result.error("搜索失败");
        }
    }

    /**
     * 获取热门搜索关键词
     */
    @GetMapping("/hot-keywords")
    public Result<String[]> getHotKeywords() {
        try {
            String[] hotKeywords = {
                "天丝", "四件套", "床上用品", "纺织面料",
                "床上三件套", "床单", "被套", "枕套",
                "纯棉", "蚕丝", "羽绒", "床垫"
            };
            return Result.success("获取成功", hotKeywords);
        } catch (Exception e) {
            log.error("获取热门关键词失败", e);
            return Result.error("获取热门关键词失败");
        }
    }
}