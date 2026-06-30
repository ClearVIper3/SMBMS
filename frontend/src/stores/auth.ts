import { defineStore } from 'pinia'

const STORAGE_KEY = 'smbms.auth'

interface AuthState {
  token: string | null
  userId: number | null
  userCode: string
  userName: string
  roleId: number | null
}

function load(): AuthState {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return { token: null, userId: null, userCode: '', userName: '', roleId: null }
  try { return JSON.parse(raw) }
  catch { return { token: null, userId: null, userCode: '', userName: '', roleId: null } }
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => load(),
  getters: {
    isLogin: (s) => !!s.token
  },
  actions: {
    setLogin(payload: Partial<AuthState>) {
      Object.assign(this.$state, payload)
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.$state))
    },
    clear() {
      this.$state = { token: null, userId: null, userCode: '', userName: '', roleId: null }
      localStorage.removeItem(STORAGE_KEY)
    }
  }
})
