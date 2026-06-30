<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { providerApi } from '@/api'
import type { ProviderDTO } from '@/api/types'

const route = useRoute(); const router = useRouter()
const p = ref<ProviderDTO | null>(null)
onMounted(async () => { p.value = await providerApi.get(Number(route.params.id)) })
</script>

<template>
  <el-card v-if="p">
    <h3>供应商详情</h3>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="编码">{{ p.proCode }}</el-descriptions-item>
      <el-descriptions-item label="名称">{{ p.proName }}</el-descriptions-item>
      <el-descriptions-item label="联系人">{{ p.proContact }}</el-descriptions-item>
      <el-descriptions-item label="电话">{{ p.proPhone }}</el-descriptions-item>
      <el-descriptions-item label="地址">{{ p.userAddress }}</el-descriptions-item>
      <el-descriptions-item label="传真">{{ p.userFax }}</el-descriptions-item>
      <el-descriptions-item label="描述" :span="2">{{ p.proDesc }}</el-descriptions-item>
    </el-descriptions>
    <el-button style="margin-top:12px;" @click="router.back()">返回</el-button>
  </el-card>
</template>
