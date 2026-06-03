package com.wms.web.controller;

import com.wms.common.core.PageResult;
import com.wms.common.core.R;
import com.wms.model.dto.SupplierDTO;
import com.wms.model.entity.WmsSupplier;
import com.wms.model.document.SupplierDocument;
import com.wms.service.SearchService;
import com.wms.service.SupplierService;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应商管理接口
 */
@Tag(name = "供应商管理接口")
@RestController
@RequestMapping("/api/v1/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;
    private final SearchService searchService;

    /**
     * 创建供应商
     */
    @Operation(summary = "创建供应商")
    @PostMapping
    @SaCheckPermission("supplier:create")
    public R<WmsSupplier> create(@Valid @RequestBody SupplierDTO dto) {
        WmsSupplier supplier = supplierService.create(dto);
        return R.ok(supplier, "创建成功");
    }

    /**
     * 更新供应商
     */
    @Operation(summary = "更新供应商")
    @PutMapping("/{id}")
    @SaCheckPermission("supplier:edit")
    public R<WmsSupplier> update(
            @Parameter(description = "供应商ID") @PathVariable Long id,
            @Valid @RequestBody SupplierDTO dto) {
        WmsSupplier supplier = supplierService.update(id, dto);
        return R.ok(supplier, "更新成功");
    }

    /**
     * 删除供应商
     */
    @Operation(summary = "删除供应商")
    @DeleteMapping("/{id}")
    @SaCheckPermission("supplier:delete")
    public R<Void> delete(@Parameter(description = "供应商ID") @PathVariable Long id) {
        supplierService.delete(id);
        return R.ok(null, "删除成功");
    }

    /**
     * 查询供应商详情
     */
    @Operation(summary = "查询供应商详情")
    @GetMapping("/{id}")
    @SaCheckPermission("supplier:view")
    public R<WmsSupplier> getById(@Parameter(description = "供应商ID") @PathVariable Long id) {
        WmsSupplier supplier = supplierService.getById(id);
        return R.ok(supplier);
    }

    /**
     * 分页查询供应商列表
     */
    @Operation(summary = "分页查询供应商列表")
    @GetMapping("/list")
    @SaCheckPermission("supplier:view")
    public R<PageResult<WmsSupplier>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {
        PageResult<WmsSupplier> result = supplierService.list(page, size, keyword);
        return R.ok(result);
    }

    @Operation(summary = "获取所有启用的供应商（用于下拉选择）")
    @GetMapping("/all")
    @SaCheckPermission("supplier:view")
    public R<List<WmsSupplier>> listAll() {
        List<WmsSupplier> result = supplierService.listAll();
        return R.ok(result);
    }

    /**
     * ES 全文搜索供应商
     */
    @Operation(summary = "全文搜索供应商")
    @GetMapping("/search")
    @SaCheckPermission("supplier:view")
    public R<PageResult<SupplierDocument>> search(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        PageResult<SupplierDocument> result = searchService.searchSuppliers(keyword, page, size);
        return R.ok(result);
    }
}
