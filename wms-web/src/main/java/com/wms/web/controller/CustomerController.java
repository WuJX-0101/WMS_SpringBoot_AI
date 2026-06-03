package com.wms.web.controller;

import com.wms.common.core.PageResult;
import com.wms.common.core.R;
import com.wms.model.dto.CustomerDTO;
import com.wms.model.entity.WmsCustomer;
import com.wms.model.document.CustomerDocument;
import com.wms.service.CustomerService;
import com.wms.service.SearchService;
import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户管理接口
 */
@Tag(name = "客户管理接口")
@RestController
@RequestMapping("/api/v1/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final SearchService searchService;

    /**
     * 创建客户
     */
    @Operation(summary = "创建客户")
    @PostMapping
    @SaCheckPermission("customer:create")
    public R<WmsCustomer> create(@Valid @RequestBody CustomerDTO dto) {
        WmsCustomer customer = customerService.create(dto);
        return R.ok(customer, "创建成功");
    }

    /**
     * 更新客户
     */
    @Operation(summary = "更新客户")
    @PutMapping("/{id}")
    @SaCheckPermission("customer:edit")
    public R<WmsCustomer> update(
            @Parameter(description = "客户ID") @PathVariable Long id,
            @Valid @RequestBody CustomerDTO dto) {
        WmsCustomer customer = customerService.update(id, dto);
        return R.ok(customer, "更新成功");
    }

    /**
     * 删除客户
     */
    @Operation(summary = "删除客户")
    @DeleteMapping("/{id}")
    @SaCheckPermission("customer:delete")
    public R<Void> delete(@Parameter(description = "客户ID") @PathVariable Long id) {
        customerService.delete(id);
        return R.ok(null, "删除成功");
    }

    /**
     * 查询客户详情
     */
    @Operation(summary = "查询客户详情")
    @GetMapping("/{id}")
    @SaCheckPermission("customer:view")
    public R<WmsCustomer> getById(@Parameter(description = "客户ID") @PathVariable Long id) {
        WmsCustomer customer = customerService.getById(id);
        return R.ok(customer);
    }

    /**
     * 分页查询客户列表
     */
    @Operation(summary = "分页查询客户列表")
    @GetMapping("/list")
    @SaCheckPermission("customer:view")
    public R<PageResult<WmsCustomer>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword) {
        PageResult<WmsCustomer> result = customerService.list(page, size, keyword);
        return R.ok(result);
    }

    @Operation(summary = "获取所有启用的客户（用于下拉选择）")
    @GetMapping("/all")
    @SaCheckPermission("customer:view")
    public R<List<WmsCustomer>> listAll() {
        List<WmsCustomer> result = customerService.listAll();
        return R.ok(result);
    }

    /**
     * ES 全文搜索客户
     */
    @Operation(summary = "全文搜索客户")
    @GetMapping("/search")
    @SaCheckPermission("customer:view")
    public R<PageResult<CustomerDocument>> search(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键词") @RequestParam String keyword) {
        PageResult<CustomerDocument> result = searchService.searchCustomers(keyword, page, size);
        return R.ok(result);
    }
}
