<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>商品总数</span>
              <el-icon color="#409eff"><Goods /></el-icon>
            </div>
          </template>
          <div class="stat-value">{{ stats.productCount }}</div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>仓库数量</span>
              <el-icon color="#67c23a"><House /></el-icon>
            </div>
          </template>
          <div class="stat-value">{{ stats.warehouseCount }}</div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>今日入库</span>
              <el-icon color="#e6a23c"><Download /></el-icon>
            </div>
          </template>
          <div class="stat-value">{{ stats.todayInboundCount }}</div>
          <div class="stat-sub">数量: {{ stats.todayInboundQuantity }}</div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <template #header>
            <div class="card-header">
              <span>今日出库</span>
              <el-icon color="#f56c6c"><Upload /></el-icon>
            </div>
          </template>
          <div class="stat-value">{{ stats.todayOutboundCount }}</div>
          <div class="stat-sub">数量: {{ stats.todayOutboundQuantity }}</div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近入库单</span>
              <el-button type="primary" link @click="$router.push('/inbound/list')">查看更多</el-button>
            </div>
          </template>
          <el-table :data="recentInbound" style="width: 100%">
            <el-table-column prop="orderNo" label="单号" width="150" />
            <el-table-column prop="relatedName" label="供应商" min-width="120" />
            <el-table-column prop="totalQuantity" label="数量" width="80" />
            <el-table-column prop="orderStatus" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.orderStatus)">
                  {{ getStatusText(row.orderStatus) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>最近出库单</span>
              <el-button type="primary" link @click="$router.push('/outbound/list')">查看更多</el-button>
            </div>
          </template>
          <el-table :data="recentOutbound" style="width: 100%">
            <el-table-column prop="orderNo" label="单号" width="150" />
            <el-table-column prop="relatedName" label="客户" min-width="120" />
            <el-table-column prop="totalQuantity" label="数量" width="80" />
            <el-table-column prop="orderStatus" label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="getStatusType(row.orderStatus)">
                  {{ getStatusText(row.orderStatus) }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
    
    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <span>库存概览</span>
          </template>
          <div class="inventory-overview">
            <div class="inventory-item">
              <div class="inventory-label">库存总量</div>
              <div class="inventory-value">{{ stats.totalInventoryQuantity }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>ES 数据同步</span>
              <el-tag type="info" size="small">运维工具</el-tag>
            </div>
          </template>
          <div class="sync-section">
            <el-button type="primary" :loading="syncLoading.all" @click="handleSync('all')">
              全量同步所有数据
            </el-button>
            <el-divider direction="vertical" />
            <el-button :loading="syncLoading.products" @click="handleSync('products')">同步商品</el-button>
            <el-button :loading="syncLoading.suppliers" @click="handleSync('suppliers')">同步供应商</el-button>
            <el-button :loading="syncLoading.customers" @click="handleSync('customers')">同步客户</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getDashboardStats } from '@/api/dashboard'
import { syncAll, syncProducts, syncSuppliers, syncCustomers } from '@/api/search'

// 统计数据
const stats = reactive({
  productCount: 0,
  warehouseCount: 0,
  todayInboundCount: 0,
  todayOutboundCount: 0,
  todayInboundQuantity: 0,
  todayOutboundQuantity: 0,
  totalInventoryQuantity: 0
})

// 最近入库单
const recentInbound = ref<any[]>([])

// 最近出库单
const recentOutbound = ref<any[]>([])

// 加载状态
const loading = ref(false)

// ES 同步加载状态
const syncLoading = reactive({
  all: false,
  products: false,
  suppliers: false,
  customers: false
})

// ES 同步处理
const handleSync = async (type: 'all' | 'products' | 'suppliers' | 'customers') => {
  syncLoading[type] = true
  try {
    const apiMap = { all: syncAll, products: syncProducts, suppliers: syncSuppliers, customers: syncCustomers }
    const res: any = await apiMap[type]()
    ElMessage.success(res.message || '同步成功')
  } catch (error: any) {
    ElMessage.error(error.message || '同步失败')
  } finally {
    syncLoading[type] = false
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res: any = await getDashboardStats()
    const data = res.data
    
    // 更新统计数据
    Object.assign(stats, {
      productCount: data.productCount || 0,
      warehouseCount: data.warehouseCount || 0,
      todayInboundCount: data.todayInboundCount || 0,
      todayOutboundCount: data.todayOutboundCount || 0,
      todayInboundQuantity: data.todayInboundQuantity || 0,
      todayOutboundQuantity: data.todayOutboundQuantity || 0,
      totalInventoryQuantity: data.totalInventoryQuantity || 0
    })
    
    // 更新最近订单
    recentInbound.value = data.recentInboundOrders || []
    recentOutbound.value = data.recentOutboundOrders || []
  } catch (error) {
    console.error('加载首页数据失败:', error)
  } finally {
    loading.value = false
  }
}

// 获取状态类型
const getStatusType = (status: number) => {
  const types: Record<number, string> = {
    0: 'info',
    1: 'warning',
    2: 'success',
    3: 'danger'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status: number) => {
  const texts: Record<number, string> = {
    0: '待审核',
    1: '已审核',
    2: '已完成',
    3: '已取消'
  }
  return texts[status] || '未知'
}

// 页面加载时获取数据
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.dashboard {
  padding: 10px;
}

.stat-card {
  cursor: pointer;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  text-align: center;
}

.stat-sub {
  font-size: 14px;
  color: #909399;
  text-align: center;
  margin-top: 8px;
}

.inventory-overview {
  display: flex;
  justify-content: center;
  gap: 100px;
}

.inventory-item {
  text-align: center;
}

.inventory-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.inventory-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.sync-section {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
