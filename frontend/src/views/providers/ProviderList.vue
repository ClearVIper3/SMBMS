<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { providerApi } from '@/api'
import type { ProviderDTO } from '@/api/types'

const router = useRouter()
const query = reactive({ proName: '', proCode: '' })
const records = ref<ProviderDTO[]>([])

async function fetchList() { records.value = await providerApi.list(query) }

async function remove(row: ProviderDTO) {
  await ElMessageBox.confirm(`确认删除供应商 ${row.proName} ?`, '提示', { type: 'warning' })
  await providerApi.remove(row.id); ElMessage.success('删除成功'); fetchList()
}

onMounted(fetchList)
</script>

<template>
  <el-card>
    <el-form inline>
      <el-form-item label="名称"><el-input v-model="query.proName" clearable /></el-form-item>
      <el-form-item label="编码"><el-input v-model="query.proCode" clearable /></el-form-item>
      <el-button type="primary" @click="fetchList">查询</el-button>
      <el-button @click="router.push('/providers/new')">添加供应商</el-button>
    </el-form>
    <el-table :data="records" border style="margin-top:8px;">
      <el-table-column prop="proCode" label="编码" width="120" />
      <el-table-column prop="proName" label="名称" />
      <el-table-column prop="proContact" label="联系人" width="120" />
      <el-table-column prop="proPhone" label="电话" width="160" />
      <el-table-column prop="userAddress" label="地址" />
      <el-table-column label="操作" width="220">
        <template #default="{row}">
          <el-button link @click="router.push(`/providers/${row.id}`)">查看</el-button>
          <el-button link @click="router.push(`/providers/${row.id}/edit`)">修改</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>
