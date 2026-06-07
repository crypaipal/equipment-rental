import { Navigate, Outlet, useOutletContext } from 'react-router-dom'
import { useAuth } from './AuthContext'
import type { UserRole } from '../types/identity'

interface RoleRouteProps {
    allowedRoles: UserRole[]
}

export function RoleRoute({ allowedRoles }: RoleRouteProps) {
    const { user } = useAuth()
    const outletContext = useOutletContext()

    if (!user) {
        return <Navigate to="/login" replace />
    }

    if (!allowedRoles.includes(user.role)) {
        return <Navigate to="/" replace />
    }

    return <Outlet context={outletContext} />
}