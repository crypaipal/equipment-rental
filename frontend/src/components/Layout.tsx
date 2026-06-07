import { NavLink, Outlet } from 'react-router-dom'
import {
    Boxes,
    CalendarCheck,
    ClipboardList,
    Home,
    Laptop,
    LogIn,
    UserPlus,
} from 'lucide-react'

const navItems = [
    { to: '/', label: 'Dashboard', icon: Home },
    { to: '/catalog', label: 'Katalog sprzętu', icon: Laptop },
    { to: '/inventory', label: 'Administracja katalogiem', icon: Boxes },
    { to: '/reservations', label: 'Rezerwacje', icon: ClipboardList },
    { to: '/rentals', label: 'Wypożyczenia', icon: CalendarCheck },
    { to: '/login', label: 'Logowanie', icon: LogIn },
    { to: '/register', label: 'Rejestracja', icon: UserPlus },
]

export function Layout() {
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

                <nav className="nav">
                    {navItems.map((item) => {
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
                <Outlet />
            </main>
        </div>
    )
}