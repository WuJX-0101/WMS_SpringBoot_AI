<template>
  <div class="supplier-list">
    <PageHeader title="供应商管理">
      <template #actions>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          新增供应商
        </el-button>
      </template>
    </PageHeader>

    <SearchForm :model="searchForm" @search="handleSearch" @reset="handleReset">
      <el-form-item label="供应商名称">
        <el-input v-model="searchForm.keyword" placeholder="请输入供应商名称" clearable />
      </el-form-item>
    </SearchForm>

    <el-table :data="tableData" v-loading="loading" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="supplierName" label="供应商名称" min-width="150" />
      <el-table-column prop="supplierCode" label="供应商编码" width="120" />
      <el-table-column prop="contactPerson" label="联系人" width="100" />
      <el-table-column prop="contactPhone" label="电话" width="120" />
      <el-table-column prop="email" label="邮箱" width="150" show-overflow-tooltip />
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px" @close="handleDialogClose">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="供应商名称" prop="supplierName">
          <el-input v-model="form.supplierName" placeholder="请输入供应商名称" />
        </el-form-item>
        <el-form-item label="供应商编码" prop="supplierCode">
          <el-input v-model="form.supplierCode" placeholder="请输入供应商编码" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" placeholder="请输入联系人" />
        </el-form-item>
        <el-form-item label="电话" prop="contactPhone">
          <el-input v-model="form.contactPhone" placeholder="请输入电话" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" placeholder="请输入地址" />
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
import { listSupplier, searchSupplier, createSupplier, updateSupplier, deleteSupplier } from '@/api/supplier'

const loading = ref(false)
const submitLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const editId = ref<number | null>(null)

const searchForm = reactive({ keyword: '' })
const pagination = reactive({ page: 1, size: 10, total: 0 })
const tableData = ref<any[]>([])

const statusMap = {
  1: { label: '启用', type: 'success' },
  0: { label: '禁用', type: 'danger' }
}

const form = reactive({
  supplierName: '',
  supplierCode: '',
  contactPerson: '',
  contactPhone: '',
  email: '',
  address: '',
  status: 1
})

const rules: FormRules = {
  supplierName: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  supplierCode: [{ required: true, message: '请输入供应商编码', trigger: 'blur' }]
}

const handleSearch = async () => {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size, keyword: searchForm.keyword }
    const res: any = searchForm.keyword ? await searchSupplier(params) : await listSupplier(params)
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
  dialogTitle.value = '新增供应商'
  editId.value = null
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑供应商'
  editId.value = row.id
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (row: any) => {
  ElMessageBox.confirm('确定要删除该供应商吗？', '提示', { type: 'warning' }).then(async () => {
    await deleteSupplier(row.id)
    ElMessage.success('删除成功')
    handleSearch()
  }).catch(() => {})
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    if (editId.value) {
      await updateSupplier(editId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createSupplier(form)
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
  handleSearch()
})
</script>

<style scoped>
.el-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
