package com.wms.web.controller;

import com.wms.common.core.R;
import com.wms.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 搜索管理接口
 *
 * 提供 ES 全量数据同步能力，用于首次部署或数据修复场景
 */
@Tag(name = "搜索管理接口")
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 全量同步商品到 ES
     */
    @Operation(summary = "全量同步商品到 ES")
    @PostMapping("/sync/products")
    public R<Void> syncProducts() {
        searchService.syncAllProducts();
        return R.ok(null, "商品同步完成");
    }

    /**
     * 全量同步供应商到 ES
     */
    @Operation(summary = "全量同步供应商到 ES")
    @PostMapping("/sync/suppliers")
    public R<Void> syncSuppliers() {
        searchService.syncAllSuppliers();
        return R.ok(null, "供应商同步完成");
    }

    /**
     * 全量同步客户到 ES
     */
    @Operation(summary = "全量同步客户到 ES")
    @PostMapping("/sync/customers")
    public R<Void> syncCustomers() {
        searchService.syncAllCustomers();
        return R.ok(null, "客户同步完成");
    }

    /**
     * 全量同步所有数据到 ES
     */
    @Operation(summary = "全量同步所有数据到 ES")
    @PostMapping("/sync/all")
    public R<Void> syncAll() {
        searchService.syncAllProducts();
        searchService.syncAllSuppliers();
        searchService.syncAllCustomers();
        return R.ok(null, "全量同步完成");
    }
}
