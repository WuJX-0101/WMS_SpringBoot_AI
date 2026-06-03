package com.wms.service;

import com.wms.common.core.PageResult;
import com.wms.model.document.CustomerDocument;
import com.wms.model.document.ProductDocument;
import com.wms.model.document.SupplierDocument;
import com.wms.model.entity.WmsCustomer;
import com.wms.model.entity.WmsProduct;
import com.wms.model.entity.WmsSupplier;

/**
 * ES 搜索服务接口
 *
 * 提供全文搜索和数据同步能力
 */
public interface SearchService {

    // ==================== 搜索 ====================

    /**
     * 搜索商品
     *
     * @param keyword 关键词
     * @param page    页码
     * @param size    每页数量
     * @return 搜索结果
     */
    PageResult<ProductDocument> searchProducts(String keyword, int page, int size);

    /**
     * 搜索供应商
     *
     * @param keyword 关键词
     * @param page    页码
     * @param size    每页数量
     * @return 搜索结果
     */
    PageResult<SupplierDocument> searchSuppliers(String keyword, int page, int size);

    /**
     * 搜索客户
     *
     * @param keyword 关键词
     * @param page    页码
     * @param size    每页数量
     * @return 搜索结果
     */
    PageResult<CustomerDocument> searchCustomers(String keyword, int page, int size);

    // ==================== 索引同步（单条） ====================

    /**
     * 索引商品（创建/更新时调用）
     */
    void indexProduct(WmsProduct product);

    /**
     * 索引供应商（创建/更新时调用）
     */
    void indexSupplier(WmsSupplier supplier);

    /**
     * 索引客户（创建/更新时调用）
     */
    void indexCustomer(WmsCustomer customer);

    /**
     * 删除商品索引
     */
    void deleteProductIndex(Long id);

    /**
     * 删除供应商索引
     */
    void deleteSupplierIndex(Long id);

    /**
     * 删除客户索引
     */
    void deleteCustomerIndex(Long id);

    // ==================== 全量同步 ====================

    /**
     * 全量同步商品到 ES
     */
    void syncAllProducts();

    /**
     * 全量同步供应商到 ES
     */
    void syncAllSuppliers();

    /**
     * 全量同步客户到 ES
     */
    void syncAllCustomers();
}
