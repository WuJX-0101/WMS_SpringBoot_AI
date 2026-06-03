package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wms.common.core.PageResult;
import com.wms.dao.elasticsearch.CustomerSearchRepository;
import com.wms.dao.elasticsearch.ProductSearchRepository;
import com.wms.dao.elasticsearch.SupplierSearchRepository;
import com.wms.dao.mapper.WmsCategoryMapper;
import com.wms.dao.mapper.WmsCustomerMapper;
import com.wms.dao.mapper.WmsProductMapper;
import com.wms.dao.mapper.WmsSupplierMapper;
import com.wms.model.document.CustomerDocument;
import com.wms.model.document.ProductDocument;
import com.wms.model.document.SupplierDocument;
import com.wms.model.entity.WmsCategory;
import com.wms.model.entity.WmsCustomer;
import com.wms.model.entity.WmsProduct;
import com.wms.model.entity.WmsSupplier;
import com.wms.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ES 搜索服务实现
 *
 * 使用 ElasticsearchOperations 进行复杂查询
 * 使用 ElasticsearchRepository 进行单条 CRUD
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchOperations elasticsearchOperations;
    private final ProductSearchRepository productSearchRepository;
    private final SupplierSearchRepository supplierSearchRepository;
    private final CustomerSearchRepository customerSearchRepository;
    private final WmsProductMapper productMapper;
    private final WmsSupplierMapper supplierMapper;
    private final WmsCustomerMapper customerMapper;
    private final WmsCategoryMapper categoryMapper;

    // ==================== 搜索 ====================

    @Override
    public PageResult<ProductDocument> searchProducts(String keyword, int page, int size) {
        Query query = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(m -> m
                        .query(keyword)
                        .fields("productCode^2", "productName^3", "barcode^2", "brand", "specification")
                        .fuzziness("AUTO")
                ))
                .withPageable(PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "_score")))
                .build();

        SearchHits<ProductDocument> hits = elasticsearchOperations.search(query, ProductDocument.class);
        List<ProductDocument> records = hits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());

        return new PageResult<>(records, hits.getTotalHits(), size, page);
    }

    @Override
    public PageResult<SupplierDocument> searchSuppliers(String keyword, int page, int size) {
        Query query = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(m -> m
                        .query(keyword)
                        .fields("supplierCode^2", "supplierName^3", "contactPerson^2", "address")
                        .fuzziness("AUTO")
                ))
                .withPageable(PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "_score")))
                .build();

        SearchHits<SupplierDocument> hits = elasticsearchOperations.search(query, SupplierDocument.class);
        List<SupplierDocument> records = hits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());

        return new PageResult<>(records, hits.getTotalHits(), size, page);
    }

    @Override
    public PageResult<CustomerDocument> searchCustomers(String keyword, int page, int size) {
        Query query = NativeQuery.builder()
                .withQuery(q -> q.multiMatch(m -> m
                        .query(keyword)
                        .fields("customerCode^2", "customerName^3", "contactPerson^2", "address")
                        .fuzziness("AUTO")
                ))
                .withPageable(PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "_score")))
                .build();

        SearchHits<CustomerDocument> hits = elasticsearchOperations.search(query, CustomerDocument.class);
        List<CustomerDocument> records = hits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());

        return new PageResult<>(records, hits.getTotalHits(), size, page);
    }

    // ==================== 索引同步（单条） ====================

    @Override
    public void indexProduct(WmsProduct product) {
        try {
            ProductDocument doc = new ProductDocument();
            doc.setId(product.getId());
            doc.setProductCode(product.getProductCode());
            doc.setProductName(product.getProductName());
            doc.setBarcode(product.getBarcode());
            doc.setBrand(product.getBrand());
            doc.setSpecification(product.getSpecification());
            doc.setSalePrice(product.getSalePrice());
            doc.setStatus(product.getStatus());
            doc.setGmtCreate(product.getGmtCreate() != null ? product.getGmtCreate().toString().replace("T", " ") : null);

            if (product.getCategoryId() != null) {
                WmsCategory category = categoryMapper.selectById(product.getCategoryId());
                if (category != null) {
                    doc.setCategoryName(category.getCategoryName());
                }
            }

            productSearchRepository.save(doc);
            log.debug("商品索引同步成功: {}", product.getProductCode());
        } catch (Exception e) {
            log.error("商品索引同步失败: {}", product.getProductCode(), e);
        }
    }

    @Override
    public void indexSupplier(WmsSupplier supplier) {
        try {
            SupplierDocument doc = new SupplierDocument();
            doc.setId(supplier.getId());
            doc.setSupplierCode(supplier.getSupplierCode());
            doc.setSupplierName(supplier.getSupplierName());
            doc.setContactPerson(supplier.getContactPerson());
            doc.setContactPhone(supplier.getContactPhone());
            doc.setAddress(supplier.getAddress());
            doc.setStatus(supplier.getStatus());
            doc.setGmtCreate(supplier.getGmtCreate() != null ? supplier.getGmtCreate().toString().replace("T", " ") : null);

            supplierSearchRepository.save(doc);
            log.debug("供应商索引同步成功: {}", supplier.getSupplierCode());
        } catch (Exception e) {
            log.error("供应商索引同步失败: {}", supplier.getSupplierCode(), e);
        }
    }

    @Override
    public void indexCustomer(WmsCustomer customer) {
        try {
            CustomerDocument doc = new CustomerDocument();
            doc.setId(customer.getId());
            doc.setCustomerCode(customer.getCustomerCode());
            doc.setCustomerName(customer.getCustomerName());
            doc.setContactPerson(customer.getContactPerson());
            doc.setContactPhone(customer.getContactPhone());
            doc.setAddress(customer.getAddress());
            doc.setCustomerType(customer.getCustomerType());
            doc.setStatus(customer.getStatus());
            doc.setGmtCreate(customer.getGmtCreate() != null ? customer.getGmtCreate().toString().replace("T", " ") : null);

            customerSearchRepository.save(doc);
            log.debug("客户索引同步成功: {}", customer.getCustomerCode());
        } catch (Exception e) {
            log.error("客户索引同步失败: {}", customer.getCustomerCode(), e);
        }
    }

    @Override
    public void deleteProductIndex(Long id) {
        try {
            productSearchRepository.deleteById(id);
            log.debug("商品索引删除成功: id={}", id);
        } catch (Exception e) {
            log.error("商品索引删除失败: id={}", id, e);
        }
    }

    @Override
    public void deleteSupplierIndex(Long id) {
        try {
            supplierSearchRepository.deleteById(id);
            log.debug("供应商索引删除成功: id={}", id);
        } catch (Exception e) {
            log.error("供应商索引删除失败: id={}", id, e);
        }
    }

    @Override
    public void deleteCustomerIndex(Long id) {
        try {
            customerSearchRepository.deleteById(id);
            log.debug("客户索引删除成功: id={}", id);
        } catch (Exception e) {
            log.error("客户索引删除失败: id={}", id, e);
        }
    }

    // ==================== 全量同步 ====================

    @Override
    public void syncAllProducts() {
        log.info("开始全量同步商品到 ES");
        List<WmsProduct> products = productMapper.selectList(
                new LambdaQueryWrapper<WmsProduct>().eq(WmsProduct::getIsDeleted, 0)
        );
        List<Long> categoryIds = products.stream()
                .map(WmsProduct::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, WmsCategory> categoryMap = categoryIds.isEmpty() ? Map.of() :
                categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(WmsCategory::getId, c -> c));

        for (WmsProduct product : products) {
            ProductDocument doc = new ProductDocument();
            doc.setId(product.getId());
            doc.setProductCode(product.getProductCode());
            doc.setProductName(product.getProductName());
            doc.setBarcode(product.getBarcode());
            doc.setBrand(product.getBrand());
            doc.setSpecification(product.getSpecification());
            doc.setSalePrice(product.getSalePrice());
            doc.setStatus(product.getStatus());
            doc.setGmtCreate(product.getGmtCreate() != null ? product.getGmtCreate().toString().replace("T", " ") : null);

            if (product.getCategoryId() != null) {
                WmsCategory category = categoryMap.get(product.getCategoryId());
                if (category != null) {
                    doc.setCategoryName(category.getCategoryName());
                }
            }

            productSearchRepository.save(doc);
        }
        log.info("商品全量同步完成，共 {} 条", products.size());
    }

    @Override
    public void syncAllSuppliers() {
        log.info("开始全量同步供应商到 ES");
        List<WmsSupplier> suppliers = supplierMapper.selectList(
                new LambdaQueryWrapper<WmsSupplier>().eq(WmsSupplier::getIsDeleted, 0)
        );
        for (WmsSupplier supplier : suppliers) {
            indexSupplier(supplier);
        }
        log.info("供应商全量同步完成，共 {} 条", suppliers.size());
    }

    @Override
    public void syncAllCustomers() {
        log.info("开始全量同步客户到 ES");
        List<WmsCustomer> customers = customerMapper.selectList(
                new LambdaQueryWrapper<WmsCustomer>().eq(WmsCustomer::getIsDeleted, 0)
        );
        for (WmsCustomer customer : customers) {
            indexCustomer(customer);
        }
        log.info("客户全量同步完成，共 {} 条", customers.size());
    }
}
