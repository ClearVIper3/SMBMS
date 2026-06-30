import { createRouter, createWebHashHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  { path: '/login', component: () => import('@/views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/users',
    children: [
      { path: 'users', component: () => import('@/views/users/UserList.vue') },
      { path: 'users/new', component: () => import('@/views/users/UserEdit.vue') },
      { path: 'users/:id/edit', component: () => import('@/views/users/UserEdit.vue') },
      { path: 'users/:id', component: () => import('@/views/users/UserView.vue') },

      { path: 'bills', component: () => import('@/views/bills/BillList.vue') },
      { path: 'bills/new', component: () => import('@/views/bills/BillEdit.vue') },
      { path: 'bills/:id/edit', component: () => import('@/views/bills/BillEdit.vue') },
      { path: 'bills/:id', component: () => import('@/views/bills/BillView.vue') },

      { path: 'providers', component: () => import('@/views/providers/ProviderList.vue') },
      { path: 'providers/new', component: () => import('@/views/providers/ProviderEdit.vue') },
      { path: 'providers/:id/edit', component: () => import('@/views/providers/ProviderEdit.vue') },
      { path: 'providers/:id', component: () => import('@/views/providers/ProviderView.vue') },

      { path: 'password', component: () => import('@/views/PasswordModify.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/users' }
]

const router = createRouter({ history: createWebHashHistory(), routes })

router.beforeEach((to) => {
  if (to.meta.public) return true
  const auth = useAuthStore()
  if (!auth.isLogin) return { path: '/login', query: { redirect: to.fullPath } }
  return true
})

export default router
