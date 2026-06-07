import { useState } from 'react'
import { loginUser } from '../api/identityApi'
import { PageHeader } from '../components/PageHeader'

export function LoginPage() {
    const [email, setEmail] = useState('rental-test@test.com')
    const [password, setPassword] = useState('password123')
    const [message, setMessage] = useState<string | null>(null)

    async function handleSubmit(event: React.FormEvent) {
        event.preventDefault()

        try {
            const result = await loginUser({ email, password })
            localStorage.setItem('currentUserId', result.userId)
            localStorage.setItem('currentUserRole', result.role)
            localStorage.setItem('currentUserEmail', result.email)

            setMessage(`Zalogowano jako ${result.email}`)
        } catch {
            setMessage('Nie udało się zalogować. Konto może być zablokowane albo dane są błędne.')
        }
    }

    return (
        <div>
            <PageHeader
                title="Logowanie"
                description="Zaloguj użytkownika, żeby zapisać jego identyfikator do tworzenia rezerwacji."
            />

            <form className="form-card" onSubmit={handleSubmit}>
                <label>
                    Email
                    <input value={email} onChange={(event) => setEmail(event.target.value)} />
                </label>

                <label>
                    Hasło
                    <input
                        type="password"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                    />
                </label>

                <button type="submit">Zaloguj</button>

                {message && <p className="form-message">{message}</p>}
            </form>
        </div>
    )
}