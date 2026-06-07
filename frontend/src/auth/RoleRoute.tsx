import { Navigate, Outlet } from 'react-router-dom'
import type { UserRole } from '../types/identity'
import { useAuth } from './AuthContext'

interface RoleRouteProps {
    allowedRoles: UserRole[]
}

export function RoleRoute({ allowedRoles }: RoleRouteProps) {
    const { user } = useAuth()

    if (!user) {
        return <Navigate to="/login" replace />
    }

    if (!allowedRoles.includes(user.role)) {
        return <Navigate to="/" replace />
    }

    return <Outlet />
}