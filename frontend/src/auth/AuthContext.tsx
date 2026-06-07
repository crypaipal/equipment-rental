import {
    createContext,
    useContext,
    useEffect,
    useMemo,
    useState,
    type ReactNode,
} from 'react'
import type { UserRole } from '../types/identity'

interface AuthUser {
    userId: string
    email: string
    role: UserRole
    token: string
}

interface AuthContextValue {
    user: AuthUser | null
    isAuthenticated: boolean
    login: (user: AuthUser) => void
    logout: () => void
    hasRole: (...roles: UserRole[]) => boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<AuthUser | null>(null)

    useEffect(() => {
        const userId = localStorage.getItem('currentUserId')
        const email = localStorage.getItem('currentUserEmail')
        const role = localStorage.getItem('currentUserRole') as UserRole | null
        const token = localStorage.getItem('currentUserToken')

        if (userId && email && role && token) {
            setUser({
                userId,
                email,
                role,
                token,
            })
        }
    }, [])

    function login(authUser: AuthUser) {
        localStorage.setItem('currentUserId', authUser.userId)
        localStorage.setItem('currentUserEmail', authUser.email)
        localStorage.setItem('currentUserRole', authUser.role)
        localStorage.setItem('currentUserToken', authUser.token)

        setUser(authUser)
    }

    function logout() {
        localStorage.removeItem('currentUserId')
        localStorage.removeItem('currentUserEmail')
        localStorage.removeItem('currentUserRole')
        localStorage.removeItem('currentUserToken')

        setUser(null)
    }

    function hasRole(...roles: UserRole[]) {
        return user !== null && roles.includes(user.role)
    }

    const value = useMemo<AuthContextValue>(
        () => ({
            user,
            isAuthenticated: user !== null,
            login,
            logout,
            hasRole,
        }),
        [user],
    )

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
    const context = useContext(AuthContext)

    if (!context) {
        throw new Error('useAuth must be used inside AuthProvider')
    }

    return context
}