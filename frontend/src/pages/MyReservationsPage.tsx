import { useEffect, useMemo, useState } from 'react'
import { useOutletContext } from 'react-router-dom'
import { getAssets, getModels } from '../api/inventoryApi'
import { cancelReservation, getReservations } from '../api/rentalApi'
import { useAuth } from '../auth/AuthContext'
import { PageHeader } from '../components/PageHeader'
import { StatusBadge } from '../components/StatusBadge'
import type { Asset, EquipmentModel } from '../types/inventory'
import type { Reservation } from '../types/rental'
import type { ToastContext } from '../types/toastContext'

function formatDateTime(value: string | null | undefined) {
    if (!value) {
        return '-'
    }

    return new Date(value).toLocaleString()
}

function shortId(value: string) {
    return value.slice(0, 8)
}

export function MyReservationsPage() {
    const { user } = useAuth()
    const { showSuccess, showError } = useOutletContext<ToastContext>()

    const [reservations, setReservations] = useState<Reservation[]>([])
    const [assets, setAssets] = useState<Asset[]>([])
    const [models, setModels] = useState<EquipmentModel[]>([])
    const [isLoading, setIsLoading] = useState(false)
    const [activeActionId, setActiveActionId] = useState<string | null>(null)

    const myReservations = useMemo(() => {
        if (!user) {
            return []
        }

        return reservations.filter((reservation) => reservation.userId === user.userId)
    }, [reservations, user])

    const assetById = useMemo(() => {
        return new Map(assets.map((asset) => [asset.id, asset]))
    }, [assets])

    const modelById = useMemo(() => {
        return new Map(models.map((model) => [model.id, model]))
    }, [models])

    async function loadData() {
        setIsLoading(true)

        try {
            const [reservationsResult, assetsResult, modelsResult] = await Promise.all([
                getReservations(),
                getAssets(),
                getModels(),
            ])

            setReservations(reservationsResult)
            setAssets(assetsResult)
            setModels(modelsResult)
        } catch {
            showError('Failed to load your reservations.')
        } finally {
            setIsLoading(false)
        }
    }

    useEffect(() => {
        loadData()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    function getAssetLabel(assetId: string) {
        const asset = assetById.get(assetId)

        if (!asset) {
            return assetId
        }

        const model = modelById.get(asset.equipmentModelId)

        if (!model) {
            return asset.inventoryTag
        }

        return `${asset.inventoryTag} · ${model.manufacturer} ${model.name}`
    }

    async function handleCancel(reservation: Reservation) {
        const confirmed = window.confirm('Cancel this reservation?')

        if (!confirmed) {
            return
        }

        setActiveActionId(reservation.id)

        try {
            await cancelReservation(reservation.id)
            showSuccess('Reservation cancelled.')
            await loadData()
        } catch {
            showError('Failed to cancel reservation.')
        } finally {
            setActiveActionId(null)
        }
    }

    return (
        <div>
            <PageHeader
                title="My reservations"
                description="Track your reservation requests and cancel pending or approved reservations before checkout."
            />

            <section className="card section-card">
                <div className="section-title-row">
                    <div>
                        <h3>Reservation history</h3>
                        <p>
                            This view is dedicated to the borrower role. It shows only
                            reservations created by the currently logged-in user.
                        </p>
                    </div>

                    <button type="button" onClick={loadData} disabled={isLoading}>
                        {isLoading ? 'Refreshing...' : 'Refresh'}
                    </button>
                </div>

                <div className="table-wrapper">
                    <table>
                        <thead>
                        <tr>
                            <th>ID</th>
                            <th>Equipment</th>
                            <th>Period</th>
                            <th>Status</th>
                            <th>Rejection reason</th>
                            <th>Created at</th>
                            <th>Action</th>
                        </tr>
                        </thead>

                        <tbody>
                        {myReservations.map((reservation) => {
                            const canCancel =
                                reservation.status === 'PENDING' ||
                                reservation.status === 'APPROVED'

                            const isBusy = activeActionId === reservation.id

                            return (
                                <tr key={reservation.id}>
                                    <td className="mono">{shortId(reservation.id)}</td>
                                    <td>{getAssetLabel(reservation.assetId)}</td>
                                    <td>
                                        <div className="date-range">
                                            <span>{formatDateTime(reservation.periodFrom)}</span>
                                            <span>{formatDateTime(reservation.periodTo)}</span>
                                        </div>
                                    </td>
                                    <td>
                                        <StatusBadge value={reservation.status} />
                                    </td>
                                    <td>{reservation.rejectionReason ?? '-'}</td>
                                    <td>{formatDateTime(reservation.createdAt)}</td>
                                    <td>
                                        {canCancel ? (
                                            <button
                                                type="button"
                                                className="secondary-button"
                                                disabled={isBusy}
                                                onClick={() => handleCancel(reservation)}
                                            >
                                                {isBusy ? 'Cancelling...' : 'Cancel'}
                                            </button>
                                        ) : (
                                            <span className="muted-text">No actions</span>
                                        )}
                                    </td>
                                </tr>
                            )
                        })}

                        {myReservations.length === 0 && (
                            <tr>
                                <td colSpan={7}>You have no reservations yet.</td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    )
}