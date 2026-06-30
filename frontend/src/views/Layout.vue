<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { authApi } from '@/api'

const router = useRouter()
const auth = useAuthStore()

async function logout() {
  try { await authApi.logout() } catch (_) { /* 即便失败也清本地 */ }
  auth.clear()
  router.replace('/login')
}
</script>

<template>
  <el-container style="height: 100vh;">
    <el-aside width="200px" style="background:#304156;color:#fff;">
      <div style="padding:16px;font-weight:bold;">SMBMS</div>
      <el-menu :default-active="$route.path" router background-color="#304156"
               text-color="#bfcbd9" active-text-color="#409EFF">
        <el-menu-item index="/users">用户管理</el-menu-item>
        <el-menu-item index="/bills">订单管理</el-menu-item>
        <el-menu-item index="/providers">供应商管理</el-menu-item>
        <el-menu-item index="/password">修改密码</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="display:flex;align-items:center;justify-content:space-between;background:#fff;border-bottom:1px solid #eee;">
        <span>欢迎，{{ auth.userName }}</span>
        <el-button size="small" @click="logout">退出</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>
