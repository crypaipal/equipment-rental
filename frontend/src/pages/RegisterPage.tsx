import { useState } from 'react'
import { registerUser } from '../api/identityApi'
import { PageHeader } from '../components/PageHeader'
import type { UserRole } from '../types/identity'

export function RegisterPage() {
    const [firstName, setFirstName] = useState('Test')
    const [lastName, setLastName] = useState('User')
    const [email, setEmail] = useState(`user-${Date.now()}@test.com`)
    const [password, setPassword] = useState('password123')
    const [role, setRole] = useState<UserRole>('BORROWER')
    const [message, setMessage] = useState<string | null>(null)

    async function handleSubmit(event: React.FormEvent) {
        event.preventDefault()

        try {
            const result = await registerUser({
                firstName,
                lastName,
                email,
                password,
                role,
            })

            setMessage(`Utworzono użytkownika: ${result.email}, ID: ${result.id}`)
        } catch {
            setMessage('Nie udało się utworzyć użytkownika.')
        }
    }

    return (
        <div>
            <PageHeader
                title="Rejestracja użytkownika"
                description="Utwórz konto studenta, pracownika obsługi albo administratora."
            />

            <form className="form-card" onSubmit={handleSubmit}>
                <label>
                    Imię
                    <input value={firstName} onChange={(event) => setFirstName(event.target.value)} />
                </label>

                <label>
                    Nazwisko
                    <input value={lastName} onChange={(event) => setLastName(event.target.value)} />
                </label>

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

                <label>
                    Rola
                    <select value={role} onChange={(event) => setRole(event.target.value as UserRole)}>
                        <option value="BORROWER">BORROWER</option>
                        <option value="LAB_ASSISTANT">LAB_ASSISTANT</option>
                        <option value="SYSTEM_ADMIN">SYSTEM_ADMIN</option>
                    </select>
                </label>

                <button type="submit">Zarejestruj</button>

                {message && <p className="form-message">{message}</p>}
            </form>
        </div>
    )
}