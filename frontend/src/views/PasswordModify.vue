<script setup lang="ts">
import { reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { authApi } from '@/api'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const form = reactive({ oldPassword: '', newPassword: '', confirm: '' })

async function submit() {
  if (!form.oldPassword || !form.newPassword) {
    ElMessage.warning('请填写完整')
    return
  }
  if (form.newPassword !== form.confirm) {
    ElMessage.warning('两次新密码不一致')
    return
  }
  await authApi.changePassword(form.oldPassword, form.newPassword)
  ElMessage.success('修改成功，请重新登录')
  auth.clear()
  router.replace('/login')
}
</script>

<template>
  <el-card style="max-width:480px;">
    <h3>修改密码</h3>
    <el-form :model="form" label-width="80px">
      <el-form-item label="原密码"><el-input v-model="form.oldPassword" type="password" /></el-form-item>
      <el-form-item label="新密码"><el-input v-model="form.newPassword" type="password" /></el-form-item>
      <el-form-item label="确认"><el-input v-model="form.confirm" type="password" /></el-form-item>
      <el-button type="primary" @click="submit">提交</el-button>
    </el-form>
  </el-card>
</template>
