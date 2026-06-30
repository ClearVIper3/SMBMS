<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi, roleApi } from '@/api'
import type { RoleDTO, UserUpsertRequest } from '@/api/types'

const route = useRoute()
const router = useRouter()
const id = computed(() => route.params.id ? Number(route.params.id) : null)
const isEdit = computed(() => id.value !== null)

const form = reactive<UserUpsertRequest>({
  userCode: '', userName: '', userPassword: '', gender: 1,
  birthday: '', phone: '', address: '', userRole: undefined
})
const roles = ref<RoleDTO[]>([])

onMounted(async () => {
  roles.value = await roleApi.list()
  if (isEdit.value) {
    const u = await userApi.get(id.value!)
    Object.assign(form, {
      userName: u.userName, gender: u.gender, birthday: u.birthday,
      phone: u.phone, address: u.address, userRole: u.userRole
    })
  }
})

async function submit() {
  if (isEdit.value) {
    await userApi.update(id.value!, form)
    ElMessage.success('修改成功')
  } else {
    if (!form.userCode || !form.userPassword) { ElMessage.warning('编码与密码必填'); return }
    await userApi.add(form)
    ElMessage.success('新增成功')
  }
  router.replace('/users')
}
</script>

<template>
  <el-card>
    <h3>{{ isEdit ? '修改用户' : '新增用户' }}</h3>
    <el-form :model="form" label-width="100px" style="max-width:560px;">
      <el-form-item label="编码" v-if="!isEdit"><el-input v-model="form.userCode" /></el-form-item>
      <el-form-item label="姓名"><el-input v-model="form.userName" /></el-form-item>
      <el-form-item label="密码" v-if="!isEdit"><el-input v-model="form.userPassword" type="password" /></el-form-item>
      <el-form-item label="性别">
        <el-radio-group v-model="form.gender"><el-radio :value="1">男</el-radio><el-radio :value="2">女</el-radio></el-radio-group>
      </el-form-item>
      <el-form-item label="生日"><el-date-picker v-model="form.birthday" value-format="YYYY-MM-DD" /></el-form-item>
      <el-form-item label="电话"><el-input v-model="form.phone" /></el-form-item>
      <el-form-item label="地址"><el-input v-model="form.address" /></el-form-item>
      <el-form-item label="角色">
        <el-select v-model="form.userRole" style="width:200px;">
          <el-option v-for="r in roles" :key="r.id" :value="r.id" :label="r.roleName" />
        </el-select>
      </el-form-item>
      <el-button type="primary" @click="submit">保存</el-button>
      <el-button @click="router.back()">返回</el-button>
    </el-form>
  </el-card>
</template>
