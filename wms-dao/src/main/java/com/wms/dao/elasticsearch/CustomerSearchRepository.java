package com.wms.dao.elasticsearch;

import com.wms.model.document.CustomerDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 客户 ES 搜索仓库
 */
public interface CustomerSearchRepository extends ElasticsearchRepository<CustomerDocument, Long> {

    /**
     * 根据客户编码精确查询
     */
    List<CustomerDocument> findByCustomerCode(String customerCode);
}
