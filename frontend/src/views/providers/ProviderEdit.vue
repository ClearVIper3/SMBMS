<script setup lang="ts">
import { computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { providerApi } from '@/api'
import type { ProviderUpsertRequest } from '@/api/types'

const route = useRoute(); const router = useRouter()
const id = computed(() => route.params.id ? Number(route.params.id) : null)
const isEdit = computed(() => id.value !== null)

const form = reactive<ProviderUpsertRequest>({
  proCode: '', proName: '', proDesc: '',
  proContact: '', proPhone: '', userAddress: '', userFax: ''
})

onMounted(async () => {
  if (isEdit.value) {
    const p = await providerApi.get(id.value!)
    Object.assign(form, p)
  }
})

async function submit() {
  if (isEdit.value) {
    await providerApi.update(id.value!, form); ElMessage.success('修改成功')
  } else {
    if (!form.proCode || !form.proName) { ElMessage.warning('编码与名称必填'); return }
    await providerApi.add(form); ElMessage.success('新增成功')
  }
  router.replace('/providers')
}
</script>

<template>
  <el-card>
    <h3>{{ isEdit ? '修改供应商' : '新增供应商' }}</h3>
    <el-form :model="form" label-width="100px" style="max-width:560px;">
      <el-form-item label="编码" v-if="!isEdit"><el-input v-model="form.proCode" /></el-form-item>
      <el-form-item label="名称"><el-input v-model="form.proName" /></el-form-item>
      <el-form-item label="联系人"><el-input v-model="form.proContact" /></el-form-item>
      <el-form-item label="电话"><el-input v-model="form.proPhone" /></el-form-item>
      <el-form-item label="地址"><el-input v-model="form.userAddress" /></el-form-item>
      <el-form-item label="传真"><el-input v-model="form.userFax" /></el-form-item>
      <el-form-item label="描述"><el-input v-model="form.proDesc" type="textarea" /></el-form-item>
      <el-button type="primary" @click="submit">保存</el-button>
      <el-button @click="router.back()">返回</el-button>
    </el-form>
  </el-card>
</template>
