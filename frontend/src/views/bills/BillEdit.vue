<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { billApi, providerApi } from '@/api'
import type { BillUpsertRequest, ProviderDTO } from '@/api/types'

const route = useRoute(); const router = useRouter()
const id = computed(() => route.params.id ? Number(route.params.id) : null)
const isEdit = computed(() => id.value !== null)

const form = reactive<BillUpsertRequest>({
  billCode: '', productName: '', productDesc: '', productUnit: '',
  productCount: undefined, totalPrice: undefined, isPayment: 1, providerId: undefined
})
const providers = ref<ProviderDTO[]>([])

onMounted(async () => {
  providers.value = await providerApi.list({})
  if (isEdit.value) {
    const b = await billApi.get(id.value!)
    Object.assign(form, b)
  }
})

async function submit() {
  if (isEdit.value) {
    await billApi.update(id.value!, form); ElMessage.success('修改成功')
  } else {
    if (!form.billCode) { ElMessage.warning('订单编码必填'); return }
    await billApi.add(form); ElMessage.success('新增成功')
  }
  router.replace('/bills')
}
</script>

<template>
  <el-card>
    <h3>{{ isEdit ? '修改订单' : '新增订单' }}</h3>
    <el-form :model="form" label-width="100px" style="max-width:560px;">
      <el-form-item label="订单编码" v-if="!isEdit"><el-input v-model="form.billCode" /></el-form-item>
      <el-form-item label="商品名"><el-input v-model="form.productName" /></el-form-item>
      <el-form-item label="商品描述"><el-input v-model="form.productDesc" type="textarea" /></el-form-item>
      <el-form-item label="单位"><el-input v-model="form.productUnit" /></el-form-item>
      <el-form-item label="数量"><el-input-number v-model="form.productCount" :precision="2" :step="1" /></el-form-item>
      <el-form-item label="金额"><el-input-number v-model="form.totalPrice" :precision="2" :step="1" /></el-form-item>
      <el-form-item label="支付状态">
        <el-radio-group v-model="form.isPayment">
          <el-radio :value="1">未付款</el-radio>
          <el-radio :value="2">已付款</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="供应商">
        <el-select v-model="form.providerId" style="width:240px;">
          <el-option v-for="p in providers" :key="p.id" :value="p.id" :label="p.proName" />
        </el-select>
      </el-form-item>
      <el-button type="primary" @click="submit">保存</el-button>
      <el-button @click="router.back()">返回</el-button>
    </el-form>
  </el-card>
</template>
