import request from '@/utils/request'

export function listProduct(params: { page?: number; size?: number; keyword?: string }) {
  return request({ url: '/api/v1/product/list', method: 'get', params })
}

export function searchProduct(params: { page?: number; size?: number; keyword: string }) {
  return request({ url: '/api/v1/product/search', method: 'get', params })
}

export function getProduct(id: number) {
  return request({ url: `/api/v1/product/${id}`, method: 'get' })
}

export function createProduct(data: any) {
  return request({ url: '/api/v1/product', method: 'post', data })
}

export function updateProduct(id: number, data: any) {
  return request({ url: `/api/v1/product/${id}`, method: 'put', data })
}

export function deleteProduct(id: number) {
  return request({ url: `/api/v1/product/${id}`, method: 'delete' })
}
