export type UserRole = 'BORROWER' | 'LAB_ASSISTANT' | 'SYSTEM_ADMIN'

export interface User {
    id: string
    firstName: string
    lastName: string
    email: string
    role: UserRole
    lockedUntil?: string | null
    lockReason?: string | null
}

export interface LoginResponse {
    token: string
    userId: string
    email: string
    role: UserRole
    expiresAt: string
}