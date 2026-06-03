package com.wms.dao.elasticsearch;

import com.wms.model.document.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 商品 ES 搜索仓库
 */
public interface ProductSearchRepository extends ElasticsearchRepository<ProductDocument, Long> {

    /**
     * 根据商品编码精确查询
     */
    List<ProductDocument> findByProductCode(String productCode);
}
