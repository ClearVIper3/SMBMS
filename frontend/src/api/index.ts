import http from './http'
import type {
  LoginRequest, LoginResponse,
  PageResponse, UserDTO, UserUpsertRequest,
  BillDTO, BillUpsertRequest,
  ProviderDTO, ProviderUpsertRequest,
  RoleDTO
} from './types'

// ---------- auth ----------
export const authApi = {
  login: (body: LoginRequest) => http.post<any, LoginResponse>('/auth/login', body),
  logout: () => http.post('/auth/logout'),
  me: () => http.get<any, LoginResponse>('/auth/me'),
  changePassword: (oldPassword: string, newPassword: string) =>
    http.post('/auth/password', { oldPassword, newPassword })
}

// ---------- users ----------
export const userApi = {
  list: (params: { queryName?: string; queryUserRole?: number; pageIndex?: number; pageSize?: number }) =>
    http.get<any, PageResponse<UserDTO>>('/users', { params }),
  get: (id: number | string) => http.get<any, UserDTO>(`/users/${id}`),
  exists: (userCode: string) => http.get<any, boolean>('/users/exists', { params: { userCode } }),
  add: (body: UserUpsertRequest) => http.post<any, UserDTO>('/users', body),
  update: (id: number, body: UserUpsertRequest) => http.put(`/users/${id}`, body),
  remove: (id: number) => http.delete(`/users/${id}`)
}

// ---------- bills ----------
export const billApi = {
  list: (params: { productName?: string; providerId?: number; isPayment?: number }) =>
    http.get<any, BillDTO[]>('/bills', { params }),
  get: (id: number | string) => http.get<any, BillDTO>(`/bills/${id}`),
  add: (body: BillUpsertRequest) => http.post<any, BillDTO>('/bills', body),
  update: (id: number, body: BillUpsertRequest) => http.put(`/bills/${id}`, body),
  remove: (id: number) => http.delete(`/bills/${id}`)
}

// ---------- providers ----------
export const providerApi = {
  list: (params: { proName?: string; proCode?: string }) =>
    http.get<any, ProviderDTO[]>('/providers', { params }),
  get: (id: number | string) => http.get<any, ProviderDTO>(`/providers/${id}`),
  add: (body: ProviderUpsertRequest) => http.post<any, ProviderDTO>('/providers', body),
  update: (id: number, body: ProviderUpsertRequest) => http.put(`/providers/${id}`, body),
  remove: (id: number) => http.delete(`/providers/${id}`)
}

// ---------- roles ----------
export const roleApi = {
  list: () => http.get<any, RoleDTO[]>('/roles')
}
