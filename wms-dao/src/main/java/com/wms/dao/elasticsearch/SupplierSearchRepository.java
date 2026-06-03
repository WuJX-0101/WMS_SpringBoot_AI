package com.wms.dao.elasticsearch;

import com.wms.model.document.SupplierDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 供应商 ES 搜索仓库
 */
public interface SupplierSearchRepository extends ElasticsearchRepository<SupplierDocument, Long> {

    /**
     * 根据供应商编码精确查询
     */
    List<SupplierDocument> findBySupplierCode(String supplierCode);
}
