<template>
  <div class="product-list">
    <PageHeader title="商品管理">
      <template #actions>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增商品
        </el-button>
      </template>
    </PageHeader>

    <SearchForm :model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="商品名称">
        <el-input v-model="searchForm.keyword" placeholder="请输入商品名称" clearable />
      </el-form-item>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="productName" label="商品名称" min-width="150" />
      <el-table-column prop="productCode" label="商品编码" width="120" />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="unit" label="单位" width="80" />
      <el-table-column prop="salePrice" label="单价" width="100">
        <template #default="{ row }">¥{{ row.salePrice }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <StatusTag :status="row.status" :status-map="statusMap" />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="pagination.page"
      v-model:page-size="pagination.size"
      :page-sizes="[10, 20, 50, 100]"
      :total="pagination.total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSearch"
      @current-change="handleSearch"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="商品名称" prop="productName">
          <el-input v-model="form.productName" placeholder="请输入商品名称" />
        </el-form-item>
        <el-form-item label="商品编码" prop="productCode">
          <el-input v-model="form.productCode" placeholder="请输入商品编码" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-tree-select
            v-model="form.categoryId"
            :data="categoryTree"
            :props="{ label: 'categoryName', value: 'id', children: 'children' }"
            placeholder="请选择分类"
            check-strictly
          />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="请输入单位" />
        </el-form-item>
        <el-form-item label="单价" prop="salePrice">
          <el-input-number v-model="form.salePrice" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="规格" prop="specification">
          <el-input v-model="form.specification" placeholder="请输入规格" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import SearchForm from '@/components/SearchForm.vue'
import StatusTag from '@/components/StatusTag.vue'
import { listProduct, searchProduct, createProduct, updateProduct, deleteProduct } from '@/api/product'
import { getCategoryTree } from '@/api/category'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const editId = ref<number | null>(null)

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref<any[]>([])
const categoryTree = ref<any[]>([])

const statusMap = {
  1: { label: '启用', type: 'success' },
  0: { label: '禁用', type: 'danger' }
}

const form = reactive({
  productName: '',
  productCode: '',
  categoryId: undefined as number | undefined,
  unit: '',
  salePrice: 0,
  specification: '',
  status: 1
})

const rules: FormRules = {
  productName: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  productCode: [{ required: true, message: '请输入商品编码', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

const loadCategoryTree = async () => {
  const res: any = await getCategoryTree()
  categoryTree.value = res.data
}

const handleSearch = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size, keyword: searchForm.keyword }
    const res: any = searchForm.keyword ? await searchProduct(params) : await listProduct(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.keyword = ''
  pagination.page = 1
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增商品'
  editId.value = null
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑商品'
  editId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该商品吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteProduct(row.id)
    ElMessage.success('删除成功')
    handleSearch()
  }).catch(() => {})
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (editId.value) {
      await updateProduct(editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createProduct(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    handleSearch()
  } finally {
    submitLoading.value = false
  }
}

const handleDialogClose = () => {
  formRef.value?.resetFields()
}

onMounted(() => {
  loadCategoryTree()
  handleSearch()
})
</script>

<style scoped>
.el-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
