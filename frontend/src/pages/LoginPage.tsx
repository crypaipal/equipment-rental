import { useState } from 'react'
import { Link, useNavigate, useOutletContext } from 'react-router-dom'
import { loginUser } from '../api/identityApi'
import { PageHeader } from '../components/PageHeader'
import { useAuth } from '../auth/AuthContext'
import { getApiErrorMessage } from '../api/apiError'
import type { ToastContext } from '../types/toastContext'

export function LoginPage() {
    const { showSuccess, showError } = useOutletContext<ToastContext>()
    const { login } = useAuth()
    const navigate = useNavigate()

    const [email, setEmail] = useState('rental-test@test.com')
    const [password, setPassword] = useState('password123')

    async function handleSubmit(event: React.FormEvent) {
        event.preventDefault()

        try {
            const result = await loginUser({ email, password })

            login({
                userId: result.userId,
                email: result.email,
                role: result.role,
                token: result.token,
            })

            showSuccess(`Logged in as ${result.email}.`)
            navigate('/')
        } catch (error) {
            showError(
                getApiErrorMessage(
                    error,
                    'Login failed. The account may be locked or credentials are invalid.',
                ),
            )
        }
    }

    return (
        <div>
            <PageHeader
                title="Login"
                description="Sign in to access the equipment rental system."
            />

            <form className="form-card auth-form" onSubmit={handleSubmit}>
                <label>
                    Email
                    <input value={email} onChange={(event) => setEmail(event.target.value)} />
                </label>

                <label>
                    Password
                    <input
                        type="password"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                    />
                </label>

                <button type="submit">Log in</button>

                <p className="auth-switch">
                    Do not have an account? <Link to="/register">Create one</Link>
                </p>
            </form>
        </div>
    )
}