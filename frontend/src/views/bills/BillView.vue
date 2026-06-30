<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { billApi } from '@/api'
import type { BillDTO } from '@/api/types'

const route = useRoute(); const router = useRouter()
const bill = ref<BillDTO | null>(null)
onMounted(async () => { bill.value = await billApi.get(Number(route.params.id)) })
</script>

<template>
  <el-card v-if="bill">
    <h3>订单详情</h3>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="订单编码">{{ bill.billCode }}</el-descriptions-item>
      <el-descriptions-item label="商品名">{{ bill.productName }}</el-descriptions-item>
      <el-descriptions-item label="单位">{{ bill.productUnit }}</el-descriptions-item>
      <el-descriptions-item label="数量">{{ bill.productCount }}</el-descriptions-item>
      <el-descriptions-item label="金额">{{ bill.totalPrice }}</el-descriptions-item>
      <el-descriptions-item label="支付状态">{{ bill.isPayment === 2 ? '已付款' : '未付款' }}</el-descriptions-item>
      <el-descriptions-item label="供应商">{{ bill.providerName }}</el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ bill.creationDate }}</el-descriptions-item>
    </el-descriptions>
    <el-button style="margin-top:12px;" @click="router.back()">返回</el-button>
  </el-card>
</template>
