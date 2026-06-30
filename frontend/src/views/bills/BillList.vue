<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { billApi, providerApi } from '@/api'
import type { BillDTO, ProviderDTO } from '@/api/types'

const router = useRouter()
const query = reactive({ productName: '', providerId: 0, isPayment: 0 })
const records = ref<BillDTO[]>([])
const providers = ref<ProviderDTO[]>([])

async function fetchProviders() { providers.value = await providerApi.list({}) }
async function fetchList() { records.value = await billApi.list(query) }

async function remove(row: BillDTO) {
  await ElMessageBox.confirm(`确认删除订单 ${row.billCode} ?`, '提示', { type: 'warning' })
  await billApi.remove(row.id); ElMessage.success('删除成功'); fetchList()
}

onMounted(() => { fetchProviders(); fetchList() })
</script>

<template>
  <el-card>
    <el-form inline>
      <el-form-item label="商品名">
        <el-input v-model="query.productName" clearable />
      </el-form-item>
      <el-form-item label="供应商">
        <el-select v-model="query.providerId" style="width:200px;">
          <el-option :value="0" label="全部" />
          <el-option v-for="p in providers" :key="p.id" :value="p.id" :label="p.proName" />
        </el-select>
      </el-form-item>
      <el-form-item label="支付状态">
        <el-select v-model="query.isPayment" style="width:140px;">
          <el-option :value="0" label="全部" />
          <el-option :value="1" label="未付款" />
          <el-option :value="2" label="已付款" />
        </el-select>
      </el-form-item>
      <el-button type="primary" @click="fetchList">查询</el-button>
      <el-button @click="router.push('/bills/new')">添加订单</el-button>
    </el-form>

    <el-table :data="records" border style="margin-top:8px;">
      <el-table-column prop="billCode" label="订单编码" width="120" />
      <el-table-column prop="productName" label="商品" />
      <el-table-column prop="productUnit" label="单位" width="80" />
      <el-table-column prop="productCount" label="数量" width="100" />
      <el-table-column prop="totalPrice" label="金额" width="120" />
      <el-table-column label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.isPayment === 2 ? 'success' : 'warning'">
            {{ row.isPayment === 2 ? '已付款' : '未付款' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="providerName" label="供应商" width="160" />
      <el-table-column label="操作">
        <template #default="{row}">
          <el-button link @click="router.push(`/bills/${row.id}`)">查看</el-button>
          <el-button link @click="router.push(`/bills/${row.id}/edit`)">修改</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>
