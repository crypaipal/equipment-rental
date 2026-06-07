import { useEffect, useMemo, useState } from 'react'
import { useOutletContext } from 'react-router-dom'
import { getAssets, getModels } from '../api/inventoryApi'
import { getRentals, getReservations } from '../api/rentalApi'
import { PageHeader } from '../components/PageHeader'
import type { Asset, EquipmentModel } from '../types/inventory'
import type { Rental, Reservation } from '../types/rental'
import type { ToastContext } from '../types/toastContext'

export function DashboardPage() {
    const { showError } = useOutletContext<ToastContext>()

    const [models, setModels] = useState<EquipmentModel[]>([])
    const [assets, setAssets] = useState<Asset[]>([])
    const [reservations, setReservations] = useState<Reservation[]>([])
    const [rentals, setRentals] = useState<Rental[]>([])
    const [isLoading, setIsLoading] = useState(false)

    const statistics = useMemo(() => {
        return {
            modelsCount: models.length,
            assetsCount: assets.length,
            operationalAssetsCount: assets.filter(
                (asset) => asset.condition === 'OPERATIONAL',
            ).length,
            damagedAssetsCount: assets.filter((asset) => asset.condition === 'DAMAGED')
                .length,
            pendingReservationsCount: reservations.filter(
                (reservation) => reservation.status === 'PENDING',
            ).length,
            activeRentalsCount: rentals.filter((rental) => rental.status === 'ACTIVE')
                .length,
        }
    }, [models, assets, reservations, rentals])

    async function loadDashboardData() {
        setIsLoading(true)

        try {
            const [modelsResult, assetsResult, reservationsResult, rentalsResult] =
                await Promise.all([
                    getModels(),
                    getAssets(),
                    getReservations(),
                    getRentals(),
                ])

            setModels(modelsResult)
            setAssets(assetsResult)
            setReservations(reservationsResult)
            setRentals(rentalsResult)
        } catch {
            showError('Failed to load dashboard data.')
        } finally {
            setIsLoading(false)
        }
    }

    useEffect(() => {
        loadDashboardData()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    return (
        <div>
            <PageHeader
                title="Dashboard"
                description="University equipment rental management system."
            />

            <div className="section-title-row">
                <div>
                    <h3>System overview</h3>
                    <p>Current state of inventory, reservations and rentals.</p>
                </div>

                <button type="button" onClick={loadDashboardData} disabled={isLoading}>
                    {isLoading ? 'Refreshing...' : 'Refresh'}
                </button>
            </div>

            <div className="stats-grid">
                <section className="stat-card">
                    <span>Equipment models</span>
                    <strong>{statistics.modelsCount}</strong>
                </section>

                <section className="stat-card">
                    <span>Physical assets</span>
                    <strong>{statistics.assetsCount}</strong>
                </section>

                <section className="stat-card">
                    <span>Operational assets</span>
                    <strong>{statistics.operationalAssetsCount}</strong>
                </section>

                <section className="stat-card">
                    <span>Damaged assets</span>
                    <strong>{statistics.damagedAssetsCount}</strong>
                </section>

                <section className="stat-card">
                    <span>Pending reservations</span>
                    <strong>{statistics.pendingReservationsCount}</strong>
                </section>

                <section className="stat-card">
                    <span>Active rentals</span>
                    <strong>{statistics.activeRentalsCount}</strong>
                </section>
            </div>

            <div className="dashboard-grid section-card">
                <section className="card">
                    <h3>Equipment catalog</h3>
                    <p>Browse available equipment models and physical assets.</p>
                </section>

                <section className="card">
                    <h3>Reservations</h3>
                    <p>Create, approve, reject and cancel equipment reservations.</p>
                </section>

                <section className="card">
                    <h3>Rentals</h3>
                    <p>Handle equipment checkout and return operations.</p>
                </section>

                <section className="card">
                    <h3>DDD / EDA</h3>
                    <p>
                        Overdue returns and damaged equipment returns are handled through
                        domain events and context-specific handlers.
                    </p>
                </section>
            </div>
        </div>
    )
}