import request from '@/utils/request'

export function listCustomer(params: { page?: number; size?: number; keyword?: string }) {
  return request({ url: '/api/v1/customer/list', method: 'get', params })
}

export function searchCustomer(params: { page?: number; size?: number; keyword: string }) {
  return request({ url: '/api/v1/customer/search', method: 'get', params })
}

/** 获取所有客户（带缓存，用于下拉选择） */
export function getAllCustomer() {
  return request({ url: '/api/v1/customer/all', method: 'get' })
}

export function getCustomer(id: number) {
  return request({ url: `/api/v1/customer/${id}`, method: 'get' })
}

export function createCustomer(data: any) {
  return request({ url: '/api/v1/customer', method: 'post', data })
}

export function updateCustomer(id: number, data: any) {
  return request({ url: `/api/v1/customer/${id}`, method: 'put', data })
}

export function deleteCustomer(id: number) {
  return request({ url: `/api/v1/customer/${id}`, method: 'delete' })
}
