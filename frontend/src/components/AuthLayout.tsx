import { Outlet } from 'react-router-dom'
import { ToastContainer } from './ToastContainer'
import { useToast } from '../hooks/useToast'

export function AuthLayout() {
    const toast = useToast()

    return (
        <div className="auth-page">
            <div className="auth-card-shell">
                <div className="auth-brand">
                    <div className="brand-mark">ER</div>
                    <div>
                        <h1>Equipment Rental</h1>
                        <p>University equipment management system</p>
                    </div>
                </div>

                <Outlet context={toast} />
            </div>

            <ToastContainer toasts={toast.toasts} onRemove={toast.removeToast} />
        </div>
    )
}