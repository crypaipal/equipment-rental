import { PageHeader } from '../components/PageHeader'

export function DashboardPage() {
    return (
        <div>
            <PageHeader
                title="Panel główny"
                description="System zarządzania sprzętem i procesem wypożyczania w ramach uczelni."
            />

            <div className="dashboard-grid">
                <section className="card">
                    <h3>Katalog sprzętu</h3>
                    <p>Przeglądaj dostępne modele i egzemplarze sprzętu.</p>
                </section>

                <section className="card">
                    <h3>Rezerwacje</h3>
                    <p>Twórz, zatwierdzaj, odrzucaj i anuluj rezerwacje.</p>
                </section>

                <section className="card">
                    <h3>Wypożyczenia</h3>
                    <p>Obsługuj wydanie sprzętu oraz jego zwrot.</p>
                </section>

                <section className="card">
                    <h3>DDD / EDA</h3>
                    <p>
                        Zwrot po terminie i zwrot uszkodzonego sprzętu są obsługiwane przez
                        zdarzenia domenowe.
                    </p>
                </section>
            </div>
        </div>
    )
}