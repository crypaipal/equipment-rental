import { apiClient } from './apiClient'
import type { LoginResponse, User, UserRole } from '../types/identity'

export async function registerUser(payload: {
    firstName: string
    lastName: string
    email: string
    password: string
    role: UserRole
}) {
    const response = await apiClient.post<User>('/identity/register', payload)
    return response.data
}

export async function loginUser(payload: {
    email: string
    password: string
}) {
    const response = await apiClient.post<LoginResponse>('/identity/login', payload)
    return response.data
}