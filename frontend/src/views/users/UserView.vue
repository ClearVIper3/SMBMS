<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { userApi } from '@/api'
import type { UserDTO } from '@/api/types'

const route = useRoute()
const router = useRouter()
const user = ref<UserDTO | null>(null)
onMounted(async () => { user.value = await userApi.get(Number(route.params.id)) })
</script>

<template>
  <el-card v-if="user">
    <h3>用户详情</h3>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="编码">{{ user.userCode }}</el-descriptions-item>
      <el-descriptions-item label="姓名">{{ user.userName }}</el-descriptions-item>
      <el-descriptions-item label="性别">{{ user.gender === 1 ? '男' : '女' }}</el-descriptions-item>
      <el-descriptions-item label="年龄">{{ user.age }}</el-descriptions-item>
      <el-descriptions-item label="生日">{{ user.birthday }}</el-descriptions-item>
      <el-descriptions-item label="电话">{{ user.phone }}</el-descriptions-item>
      <el-descriptions-item label="地址">{{ user.address }}</el-descriptions-item>
      <el-descriptions-item label="角色">{{ user.userRoleName }}</el-descriptions-item>
    </el-descriptions>
    <el-button style="margin-top:12px;" @click="router.back()">返回</el-button>
  </el-card>
</template>
