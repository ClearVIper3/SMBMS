<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const form = reactive({ userCode: '', userPassword: '' })

async function submit() {
  if (!form.userCode || !form.userPassword) {
    ElMessage.warning('请输入用户名与密码')
    return
  }
  const r = await authApi.login(form)
  auth.setLogin({
    token: r.token, userId: r.userId, userCode: r.userCode,
    userName: r.userName, roleId: r.roleId
  })
  const redirect = (route.query.redirect as string) || '/users'
  router.replace(redirect)
}
</script>

<template>
  <div class="login-page">
    <el-card style="width: 360px;">
      <h2 style="text-align:center;margin:0 0 16px;">超市订单管理系统</h2>
      <el-form :model="form" @submit.prevent="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.userCode" autocomplete="username" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.userPassword" type="password" autocomplete="current-password"
                    @keyup.enter="submit" />
        </el-form-item>
        <el-button type="primary" style="width:100%;" @click="submit">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-page {
  height: 100vh; display: flex; align-items: center; justify-content: center;
  background: linear-gradient(135deg, #4e54c8 0%, #8f94fb 100%);
}
</style>
