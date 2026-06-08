import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
    Boxes,
    CalendarCheck,
    ClipboardList,
    Home,
    Laptop,
    LogOut,
    UserRoundCheck,
} from 'lucide-react'
import { ToastContainer } from './ToastContainer'
import { useToast } from '../hooks/useToast'
import { useAuth } from '../auth/AuthContext'
import type { UserRole } from '../types/identity'

interface NavItem {
    to: string
    label: string
    icon: typeof Home
    roles: UserRole[]
}

const navItems: NavItem[] = [
    {
        to: '/',
        label: 'Dashboard',
        icon: Home,
        roles: ['BORROWER', 'LAB_ASSISTANT', 'SYSTEM_ADMIN'],
    },
    {
        to: '/catalog',
        label: 'Equipment catalog',
        icon: Laptop,
        roles: ['BORROWER', 'LAB_ASSISTANT', 'SYSTEM_ADMIN'],
    },
    {
        to: '/my-reservations',
        label: 'My reservations',
        icon: UserRoundCheck,
        roles: ['BORROWER'],
    },
    {
        to: '/inventory',
        label: 'Inventory',
        icon: Boxes,
        roles: ['LAB_ASSISTANT', 'SYSTEM_ADMIN'],
    },
    {
        to: '/reservations',
        label: 'Reservations',
        icon: ClipboardList,
        roles: ['LAB_ASSISTANT', 'SYSTEM_ADMIN'],
    },
    {
        to: '/rentals',
        label: 'Rentals',
        icon: CalendarCheck,
        roles: ['LAB_ASSISTANT', 'SYSTEM_ADMIN'],
    },
]

export function Layout() {
    const toast = useToast()
    const navigate = useNavigate()
    const { user, logout } = useAuth()

    const visibleNavItems = navItems.filter((item) =>
        user ? item.roles.includes(user.role) : false,
    )

    function handleLogout() {
        logout()
        toast.showSuccess('Logged out successfully.')
        navigate('/login')
    }

    return (
        <div className="app-shell">
            <aside className="sidebar">
                <div className="brand">
                    <div className="brand-mark">ER</div>
                    <div>
                        <h1>Equipment Rental</h1>
                        <p>University system</p>
                    </div>
                </div>

                {user && (
                    <div className="current-user-card">
                        <span className="current-user-label">Current user</span>
                        <strong>{user.email}</strong>
                        <span>{user.role}</span>
                        <small>{user.userId.slice(0, 8)}</small>

                        <button
                            type="button"
                            className="logout-button"
                            onClick={handleLogout}
                        >
                            <LogOut size={16} />
                            Logout
                        </button>
                    </div>
                )}

                <nav className="nav">
                    {visibleNavItems.map((item) => {
                        const Icon = item.icon

                        return (
                            <NavLink
                                key={item.to}
                                to={item.to}
                                className={({ isActive }) =>
                                    isActive ? 'nav-link active' : 'nav-link'
                                }
                            >
                                <Icon size={18} />
                                <span>{item.label}</span>
                            </NavLink>
                        )
                    })}
                </nav>
            </aside>

            <main className="content">
                <Outlet context={toast} />
            </main>

            <ToastContainer toasts={toast.toasts} onRemove={toast.removeToast} />
        </div>
    )
}