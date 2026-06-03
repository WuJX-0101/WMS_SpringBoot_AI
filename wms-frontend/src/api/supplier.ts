import request from '@/utils/request'

export function listSupplier(params: { page?: number; size?: number; keyword?: string }) {
  return request({ url: '/api/v1/supplier/list', method: 'get', params })
}

export function searchSupplier(params: { page?: number; size?: number; keyword: string }) {
  return request({ url: '/api/v1/supplier/search', method: 'get', params })
}

/** 获取所有供应商（带缓存，用于下拉选择） */
export function getAllSupplier() {
  return request({ url: '/api/v1/supplier/all', method: 'get' })
}

export function getSupplier(id: number) {
  return request({ url: `/api/v1/supplier/${id}`, method: 'get' })
}

export function createSupplier(data: any) {
  return request({ url: '/api/v1/supplier', method: 'post', data })
}

export function updateSupplier(id: number, data: any) {
  return request({ url: `/api/v1/supplier/${id}`, method: 'put', data })
}

export function deleteSupplier(id: number) {
  return request({ url: `/api/v1/supplier/${id}`, method: 'delete' })
}
