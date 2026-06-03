package com.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wms.common.core.PageResult;
import com.wms.common.exception.BusinessException;
import com.wms.dao.mapper.WmsCustomerMapper;
import com.wms.model.dto.CustomerDTO;
import com.wms.model.entity.WmsCustomer;
import com.wms.service.CustomerService;
import com.wms.service.SearchService;

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
 * 客户服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final WmsCustomerMapper customerMapper;
    private final SearchService searchService;

    /**
     * 创建客户
     * 
     * 流程:
     * 1. 检查客户编码唯一性
     * 2. 插入数据库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheConfig.CACHE_CUSTOMER, key = "'all'")
    public WmsCustomer create(CustomerDTO dto) {
        // 1. 检查客户编码唯一性
        WmsCustomer exist = customerMapper.selectOne(
                new LambdaQueryWrapper<WmsCustomer>()
                        .eq(WmsCustomer::getCustomerCode, dto.getCustomerCode())
        );
        if (exist != null) {
            throw new BusinessException("客户编码已存在");
        }

        // 2. 构建实体
        WmsCustomer customer = new WmsCustomer();
        BeanUtils.copyProperties(dto, customer);
        customer.setStatus(1);

        // 3. 插入数据库
        customerMapper.insert(customer);
        log.info("创建客户成功: {}", customer.getCustomerCode());

        // 4. 同步 ES 索引
        searchService.indexCustomer(customer);

        return customer;
    }

    /**
     * 更新客户
     * 
     * 流程:
     * 1. 检查客户是否存在
     * 2. 检查客户编码唯一性（排除自身）
     * 3. 更新数据库
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheConfig.CACHE_CUSTOMER, key = "'all'")
    public WmsCustomer update(Long id, CustomerDTO dto) {
        // 1. 检查客户是否存在
        WmsCustomer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }

        // 2. 检查客户编码唯一性（排除自身）
        WmsCustomer exist = customerMapper.selectOne(
                new LambdaQueryWrapper<WmsCustomer>()
                        .eq(WmsCustomer::getCustomerCode, dto.getCustomerCode())
                        .ne(WmsCustomer::getId, id)
        );
        if (exist != null) {
            throw new BusinessException("客户编码已存在");
        }

        // 3. 更新数据库
        BeanUtils.copyProperties(dto, customer);
        customerMapper.updateById(customer);
        log.info("更新客户成功: {}", customer.getCustomerCode());

        // 4. 同步 ES 索引
        searchService.indexCustomer(customer);

        return customer;
    }

    /**
     * 删除客户
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheConfig.CACHE_CUSTOMER, key = "'all'")
    public void delete(Long id) {
        WmsCustomer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        customerMapper.deleteById(id);
        log.info("删除客户成功: {}", customer.getCustomerCode());

        // 同步删除 ES 索引
        searchService.deleteCustomerIndex(id);
    }

    /**
     * 根据ID查询客户
     */
    @Override
    public WmsCustomer getById(Long id) {
        WmsCustomer customer = customerMapper.selectById(id);
        if (customer == null) {
            throw new BusinessException("客户不存在");
        }
        return customer;
    }

    /**
     * 分页查询客户列表
     * 
     * @param page    页码
     * @param size    每页数量
     * @param keyword 搜索关键词（匹配客户编码、名称、联系人）
     */
    @Override
    public PageResult<WmsCustomer> list(int page, int size, String keyword) {
        LambdaQueryWrapper<WmsCustomer> wrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(WmsCustomer::getCustomerCode, keyword)
                    .or()
                    .like(WmsCustomer::getCustomerName, keyword)
                    .or()
                    .like(WmsCustomer::getContactPerson, keyword)
            );
        }
        
        wrapper.orderByDesc(WmsCustomer::getGmtCreate);

        Page<WmsCustomer> result = customerMapper.selectPage(new Page<>(page, size), wrapper);
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getSize(), result.getCurrent());
    }

    /**
     * 获取所有启用的客户（用于下拉选择）
     * 缓存：结果缓存10分钟
     */
    @Override
    @Cacheable(value = CacheConfig.CACHE_CUSTOMER, key = "'all'")
    public List<WmsCustomer> listAll() {
        log.debug("从数据库查询所有客户");
        return customerMapper.selectList(
                new LambdaQueryWrapper<WmsCustomer>()
                        .eq(WmsCustomer::getStatus, 1)
                        .eq(WmsCustomer::getIsDeleted, 0)
                        .orderByAsc(WmsCustomer::getCustomerCode)
        );
    }
}
