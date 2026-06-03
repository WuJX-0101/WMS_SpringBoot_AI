package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.core.PageResult;
import com.wms.common.exception.BusinessException;
import com.wms.dao.mapper.WmsCategoryMapper;
import com.wms.dao.mapper.WmsProductMapper;
import com.wms.model.dto.ProductDTO;
import com.wms.model.entity.WmsCategory;
import com.wms.model.entity.WmsProduct;
import com.wms.model.vo.ProductVO;
import com.wms.service.ProductService;
import com.wms.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final WmsProductMapper productMapper;
    private final WmsCategoryMapper categoryMapper;
    private final SearchService searchService;

    /**
     * 创建商品
     * 
     * 流程:
     * 1. 检查商品编码唯一性
     * 2. 检查条码唯一性（如果提供了条码）
     * 3. 插入数据库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsProduct create(ProductDTO dto) {
        // 1. 检查商品编码唯一性
        WmsProduct exist = productMapper.selectOne(
                new LambdaQueryWrapper<WmsProduct>()
                        .eq(WmsProduct::getProductCode, dto.getProductCode())
        );
        if (exist != null) {
            throw new BusinessException("商品编码已存在");
        }

        // 2. 检查条码唯一性（如果提供了条码）
        if (StringUtils.hasText(dto.getBarcode())) {
            exist = productMapper.selectOne(
                    new LambdaQueryWrapper<WmsProduct>()
                            .eq(WmsProduct::getBarcode, dto.getBarcode())
            );
            if (exist != null) {
                throw new BusinessException("商品条码已存在");
            }
        }

        // 3. 构建实体
        WmsProduct product = new WmsProduct();
        BeanUtils.copyProperties(dto, product);
        product.setStatus(1);

        // 4. 插入数据库
        productMapper.insert(product);
        log.info("创建商品成功: {}", product.getProductCode());

        // 5. 同步 ES 索引
        searchService.indexProduct(product);

        return product;
    }

    /**
     * 更新商品
     * 
     * 流程:
     * 1. 检查商品是否存在
     * 2. 检查商品编码唯一性（排除自身）
     * 3. 检查条码唯一性（排除自身）
     * 4. 更新数据库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public WmsProduct update(Long id, ProductDTO dto) {
        // 1. 检查商品是否存在
        WmsProduct product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }

        // 2. 检查商品编码唯一性（排除自身）
        WmsProduct exist = productMapper.selectOne(
                new LambdaQueryWrapper<WmsProduct>()
                        .eq(WmsProduct::getProductCode, dto.getProductCode())
                        .ne(WmsProduct::getId, id)
        );
        if (exist != null) {
            throw new BusinessException("商品编码已存在");
        }

        // 3. 检查条码唯一性（排除自身）
        if (StringUtils.hasText(dto.getBarcode())) {
            exist = productMapper.selectOne(
                    new LambdaQueryWrapper<WmsProduct>()
                            .eq(WmsProduct::getBarcode, dto.getBarcode())
                            .ne(WmsProduct::getId, id)
            );
            if (exist != null) {
                throw new BusinessException("商品条码已存在");
            }
        }

        // 4. 更新数据库
        BeanUtils.copyProperties(dto, product);
        productMapper.updateById(product);
        log.info("更新商品成功: {}", product.getProductCode());

        // 5. 同步 ES 索引
        searchService.indexProduct(product);

        return product;
    }

    /**
     * 删除商品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        WmsProduct product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        productMapper.deleteById(id);
        log.info("删除商品成功: {}", product.getProductCode());

        // 同步删除 ES 索引
        searchService.deleteProductIndex(id);
    }

    /**
     * 根据ID查询商品
     */
    @Override
    public WmsProduct getById(Long id) {
        WmsProduct product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return product;
    }

    /**
     * 分页查询商品列表
     * 
     * @param page    页码
     * @param size    每页数量
     * @param keyword 搜索关键词（匹配商品编码、名称、条码）
     */
    @Override
    public PageResult<ProductVO> list(int page, int size, String keyword) {
        LambdaQueryWrapper<WmsProduct> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(WmsProduct::getProductCode, keyword)
                    .or()
                    .like(WmsProduct::getProductName, keyword)
                    .or()
                    .like(WmsProduct::getBarcode, keyword)
            );
        }
        
        wrapper.orderByDesc(WmsProduct::getGmtCreate);

        Page<WmsProduct> result = productMapper.selectPage(new Page<>(page, size), wrapper);
        
        // 批量查询关联数据
        List<WmsProduct> records = result.getRecords();
        List<ProductVO> voList = convertToProductVO(records);
        
        return new PageResult<>(voList, result.getTotal(), result.getSize(), result.getCurrent());
    }

    /**
     * 将商品实体列表转换为商品VO列表
     * 
     * 关联查询分类名称
     */
    private List<ProductVO> convertToProductVO(List<WmsProduct> products) {
        if (products.isEmpty()) {
            return List.of();
        }

        // 批量查询分类
        List<Long> categoryIds = products.stream()
                .map(WmsProduct::getCategoryId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, WmsCategory> categoryMap = categoryIds.isEmpty() ? Map.of() :
                categoryMapper.selectBatchIds(categoryIds).stream()
                        .collect(Collectors.toMap(WmsCategory::getId, c -> c));

        // 转换为VO
        return products.stream().map(product -> {
            ProductVO vo = new ProductVO();
            // 复制基础属性
            vo.setId(product.getId());
            vo.setProductCode(product.getProductCode());
            vo.setProductName(product.getProductName());
            vo.setCategoryId(product.getCategoryId());
            vo.setBrand(product.getBrand());
            vo.setSpecification(product.getSpecification());
            vo.setUnit(product.getUnit());
            vo.setBarcode(product.getBarcode());
            vo.setWeight(product.getWeight());
            vo.setVolume(product.getVolume());
            vo.setPurchasePrice(product.getPurchasePrice());
            vo.setSalePrice(product.getSalePrice());
            vo.setSafetyStock(product.getSafetyStock());
            vo.setMinStock(product.getMinStock());
            vo.setMaxStock(product.getMaxStock());
            vo.setImage(product.getImage());
            vo.setStatus(product.getStatus());
            vo.setRemark(product.getRemark());
            vo.setGmtCreate(product.getGmtCreate());
            vo.setGmtModified(product.getGmtModified());
            
            // 设置关联名称
            if (product.getCategoryId() != null) {
                WmsCategory category = categoryMap.get(product.getCategoryId());
                if (category != null) {
                    vo.setCategoryName(category.getCategoryName());
                }
            }
            
            return vo;
        }).collect(Collectors.toList());
    }
}
