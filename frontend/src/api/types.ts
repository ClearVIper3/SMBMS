// 与后端 DTO 一一对应的 TS 类型

export interface LoginRequest { userCode: string; userPassword: string }
export interface LoginResponse {
  token: string
  userId: number
  userCode: string
  userName: string
  roleId: number
}

export interface PageResponse<T> {
  records: T[]
  total: number
  pageIndex: number
  pageSize: number
  totalPages: number
}

export interface UserDTO {
  id: number
  userCode: string
  userName: string
  gender?: number
  birthday?: string
  phone?: string
  address?: string
  userRole?: number
  userRoleName?: string
  age?: number
  creationDate?: string
}

export interface UserUpsertRequest {
  userCode?: string
  userName?: string
  userPassword?: string
  gender?: number
  birthday?: string
  phone?: string
  address?: string
  userRole?: number
}

export interface BillDTO {
  id: number
  billCode: string
  productName?: string
  productDesc?: string
  productUnit?: string
  productCount?: number
  totalPrice?: number
  isPayment?: number
  providerId?: number
  providerName?: string
  creationDate?: string
}

export interface BillUpsertRequest {
  billCode?: string
  productName?: string
  productDesc?: string
  productUnit?: string
  productCount?: number
  totalPrice?: number
  isPayment?: number
  providerId?: number
}

export interface ProviderDTO {
  id: number
  proCode: string
  proName?: string
  proDesc?: string
  proContact?: string
  proPhone?: string
  userAddress?: string
  userFax?: string
}

export interface ProviderUpsertRequest {
  proCode?: string
  proName?: string
  proDesc?: string
  proContact?: string
  proPhone?: string
  userAddress?: string
  userFax?: string
}

export interface RoleDTO { id: number; roleCode: string; roleName: string }
