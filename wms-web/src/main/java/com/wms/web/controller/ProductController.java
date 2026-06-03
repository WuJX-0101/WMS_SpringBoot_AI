package com.wms.web.controller;

import com.wms.common.core.PageResult;
import com.wms.common.core.R;
import com.wms.model.dto.ProductDTO;
import com.wms.model.entity.WmsProduct;
import com.wms.model.vo.ProductVO;
import com.wms.model.document.ProductDocument;
import com.wms.service.ProductService;
import com.wms.service.SearchService;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品管理接口
 */
@Tag(name = "商品管理接口")
@RestController
@RequestMapping("/api/v1/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final SearchService searchService;

    /**
     * 创建商品
     */
    @Operation(summary = "创建商品")
    @PostMapping
    @SaCheckPermission("product:create")
    public R<WmsProduct> create(@Valid @RequestBody ProductDTO dto) {
        WmsProduct product = productService.create(dto);
        return R.ok(product, "创建成功");
    }

    /**
     * 更新商品
     */
    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    @SaCheckPermission("product:edit")
    public R<WmsProduct> update(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @Valid @RequestBody ProductDTO dto) {
        WmsProduct product = productService.update(id, dto);
        return R.ok(product, "更新成功");
    }

    /**
     * 删除商品
     */
    @Operation(summary = "删除商品")
    @DeleteMapping("/{id}")
    @SaCheckPermission("product:delete")
    public R<Void> delete(@Parameter(description = "商品ID") @PathVariable Long id) {
        productService.delete(id);
        return R.ok(null, "删除成功");
    }

    /**
     * 查询商品详情
     */
    @Operation(summary = "查询商品详情")
    @GetMapping("/{id}")
    @SaCheckPermission("product:view")
    public R<WmsProduct> getById(@Parameter(description = "商品ID") @PathVariable Long id) {
        WmsProduct product = productService.getById(id);
        return R.ok(product);
    }

    /**
     * 分页查询商品列表
     */
    @Operation(summary = "分页查询商品列表")
    @GetMapping("/list")
    @SaCheckPermission("product:view")
    public R<PageResult<ProductVO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {
        PageResult<ProductVO> result = productService.list(page, size, keyword);
        return R.ok(result);
    }

    /**
     * ES 全文搜索商品
     */
    @Operation(summary = "全文搜索商品")
    @GetMapping("/search")
    @SaCheckPermission("product:view")
    public R<PageResult<ProductDocument>> search(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        PageResult<ProductDocument> result = searchService.searchProducts(keyword, page, size);
        return R.ok(result);
    }
}
