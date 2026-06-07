import { useState } from 'react'
import { Link, useNavigate, useOutletContext } from 'react-router-dom'
import { registerUser } from '../api/identityApi'
import { PageHeader } from '../components/PageHeader'
import { getApiErrorMessage } from '../api/apiError'
import type { ToastContext } from '../types/toastContext'

export function RegisterPage() {
    const { showSuccess, showError } = useOutletContext<ToastContext>()
    const navigate = useNavigate()

    const [firstName, setFirstName] = useState('Test')
    const [lastName, setLastName] = useState('User')
    const [email, setEmail] = useState(`user-${Date.now()}@test.com`)
    const [password, setPassword] = useState('password123')

    async function handleSubmit(event: React.FormEvent) {
        event.preventDefault()

        try {
            const result = await registerUser({
                firstName,
                lastName,
                email,
                password,
                role: 'BORROWER',
            })

            showSuccess(`User created: ${result.email}. You can now log in.`)
            navigate('/login')
        } catch (error) {
            showError(
                getApiErrorMessage(
                    error,
                    'User registration failed.',
                ),
            )
        }
    }

    return (
        <div>
            <PageHeader
                title="Create account"
                description="Create a borrower account to reserve university equipment."
            />

            <form className="form-card auth-form" onSubmit={handleSubmit}>
                <label>
                    First name
                    <input value={firstName} onChange={(event) => setFirstName(event.target.value)} />
                </label>

                <label>
                    Last name
                    <input value={lastName} onChange={(event) => setLastName(event.target.value)} />
                </label>

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

                <button type="submit">Create account</button>

                <p className="auth-switch">
                    Already have an account? <Link to="/login">Log in</Link>
                </p>
            </form>
        </div>
    )
}