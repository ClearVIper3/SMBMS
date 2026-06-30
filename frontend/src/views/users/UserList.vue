<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi, roleApi } from '@/api'
import type { UserDTO, RoleDTO } from '@/api/types'

const router = useRouter()
const query = reactive({ queryName: '', queryUserRole: 0, pageIndex: 1, pageSize: 5 })
const records = ref<UserDTO[]>([])
const total = ref(0)
const roles = ref<RoleDTO[]>([])

async function fetchRoles() { roles.value = await roleApi.list() }

async function fetchList() {
  const r = await userApi.list(query)
  records.value = r.records
  total.value = r.total
}

async function remove(row: UserDTO) {
  await ElMessageBox.confirm(`确认删除用户 ${row.userName} ?`, '提示', { type: 'warning' })
  await userApi.remove(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(() => { fetchRoles(); fetchList() })
</script>

<template>
  <el-card>
    <el-form inline>
      <el-form-item label="用户名">
        <el-input v-model="query.queryName" clearable />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="query.queryUserRole" style="width:160px;">
          <el-option :value="0" label="全部" />
          <el-option v-for="r in roles" :key="r.id" :value="r.id" :label="r.roleName" />
        </el-select>
      </el-form-item>
      <el-button type="primary" @click="query.pageIndex=1; fetchList()">查询</el-button>
      <el-button @click="router.push('/users/new')">添加用户</el-button>
    </el-form>

    <el-table :data="records" border style="margin-top:8px;">
      <el-table-column prop="userCode" label="编码" width="120" />
      <el-table-column prop="userName" label="姓名" width="120" />
      <el-table-column label="性别" width="80">
        <template #default="{row}">{{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '' }}</template>
      </el-table-column>
      <el-table-column prop="age" label="年龄" width="80" />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="userRoleName" label="角色" width="120" />
      <el-table-column label="操作">
        <template #default="{row}">
          <el-button link @click="router.push(`/users/${row.id}`)">查看</el-button>
          <el-button link @click="router.push(`/users/${row.id}/edit`)">修改</el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination style="margin-top:8px;" background layout="prev, pager, next, total"
                   :total="total" :page-size="query.pageSize" :current-page="query.pageIndex"
                   @current-change="(p:number) => { query.pageIndex = p; fetchList() }" />
  </el-card>
</template>
