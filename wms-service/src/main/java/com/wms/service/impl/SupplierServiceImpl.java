package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.core.PageResult;
import com.wms.common.exception.BusinessException;
import com.wms.dao.mapper.WmsSupplierMapper;
import com.wms.model.dto.SupplierDTO;
import com.wms.model.entity.WmsSupplier;
import com.wms.service.SearchService;
import com.wms.service.SupplierService;

import java.util.List;
import com.wms.service.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 供应商服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final WmsSupplierMapper supplierMapper;
    private final SearchService searchService;

    /**
     * 创建供应商
     * 
     * 流程:
     * 1. 检查供应商编码唯一性
     * 2. 插入数据库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheConfig.CACHE_SUPPLIER, key = "'all'")
    public WmsSupplier create(SupplierDTO dto) {
        // 1. 检查供应商编码唯一性
        WmsSupplier exist = supplierMapper.selectOne(
                new LambdaQueryWrapper<WmsSupplier>()
                        .eq(WmsSupplier::getSupplierCode, dto.getSupplierCode())
        );
        if (exist != null) {
            throw new BusinessException("供应商编码已存在");
        }

        // 2. 构建实体
        WmsSupplier supplier = new WmsSupplier();
        BeanUtils.copyProperties(dto, supplier);
        supplier.setStatus(1);

        // 3. 插入数据库
        supplierMapper.insert(supplier);
        log.info("创建供应商成功: {}", supplier.getSupplierCode());

        // 4. 同步 ES 索引
        searchService.indexSupplier(supplier);

        return supplier;
    }

    /**
     * 更新供应商
     * 
     * 流程:
     * 1. 检查供应商是否存在
     * 2. 检查供应商编码唯一性（排除自身）
     * 3. 更新数据库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheConfig.CACHE_SUPPLIER, key = "'all'")
    public WmsSupplier update(Long id, SupplierDTO dto) {
        // 1. 检查供应商是否存在
        WmsSupplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException("供应商不存在");
        }

        // 2. 检查供应商编码唯一性（排除自身）
        WmsSupplier exist = supplierMapper.selectOne(
                new LambdaQueryWrapper<WmsSupplier>()
                        .eq(WmsSupplier::getSupplierCode, dto.getSupplierCode())
                        .ne(WmsSupplier::getId, id)
        );
        if (exist != null) {
            throw new BusinessException("供应商编码已存在");
        }

        // 3. 更新数据库
        BeanUtils.copyProperties(dto, supplier);
        supplierMapper.updateById(supplier);
        log.info("更新供应商成功: {}", supplier.getSupplierCode());

        // 4. 同步 ES 索引
        searchService.indexSupplier(supplier);

        return supplier;
    }

    /**
     * 删除供应商
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheConfig.CACHE_SUPPLIER, key = "'all'")
    public void delete(Long id) {
        WmsSupplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException("供应商不存在");
        }
        supplierMapper.deleteById(id);
        log.info("删除供应商成功: {}", supplier.getSupplierCode());

        // 同步删除 ES 索引
        searchService.deleteSupplierIndex(id);
    }

    /**
     * 根据ID查询供应商
     */
    @Override
    public WmsSupplier getById(Long id) {
        WmsSupplier supplier = supplierMapper.selectById(id);
        if (supplier == null) {
            throw new BusinessException("供应商不存在");
        }
        return supplier;
    }

    /**
     * 分页查询供应商列表
     * 
     * @param page    页码
     * @param size    每页数量
     * @param keyword 搜索关键词（匹配供应商编码、名称、联系人）
     */
    @Override
    public PageResult<WmsSupplier> list(int page, int size, String keyword) {
        LambdaQueryWrapper<WmsSupplier> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(WmsSupplier::getSupplierCode, keyword)
                    .or()
                    .like(WmsSupplier::getSupplierName, keyword)
                    .or()
                    .like(WmsSupplier::getContactPerson, keyword)
            );
        }
        
        wrapper.orderByDesc(WmsSupplier::getGmtCreate);

        Page<WmsSupplier> result = supplierMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent());
    }

    /**
     * 获取所有启用的供应商（用于下拉选择）
     * 缓存：结果缓存10分钟
     */
    @Override
    @Cacheable(value = CacheConfig.CACHE_SUPPLIER, key = "'all'")
    public List<WmsSupplier> listAll() {
        log.debug("从数据库查询所有供应商");
        return supplierMapper.selectList(
                new LambdaQueryWrapper<WmsSupplier>()
                        .eq(WmsSupplier::getStatus, 1)
                        .eq(WmsSupplier::getIsDeleted, 0)
                        .orderByAsc(WmsSupplier::getSupplierCode)
        );
    }
}
