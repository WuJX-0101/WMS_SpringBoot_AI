import request from '@/utils/request'

/** 全量同步所有数据到 ES */
export function syncAll() {
  return request({ url: '/api/v1/search/sync/all', method: 'post' })
}

/** 全量同步商品到 ES */
export function syncProducts() {
  return request({ url: '/api/v1/search/sync/products', method: 'post' })
}

/** 全量同步供应商到 ES */
export function syncSuppliers() {
  return request({ url: '/api/v1/search/sync/suppliers', method: 'post' })
}

/** 全量同步客户到 ES */
export function syncCustomers() {
  return request({ url: '/api/v1/search/sync/customers', method: 'post' })
}
