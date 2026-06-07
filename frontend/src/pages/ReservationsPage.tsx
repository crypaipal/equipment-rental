import { useEffect, useMemo, useState } from 'react'
import { useOutletContext } from 'react-router-dom'
import { getAssets, getModels } from '../api/inventoryApi'
import {
    approveReservation,
    cancelReservation,
    checkoutReservation,
    getReservations,
    rejectReservation,
} from '../api/rentalApi'
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

export function ReservationsPage() {
    const { showSuccess, showError } = useOutletContext<ToastContext>()

    const [reservations, setReservations] = useState<Reservation[]>([])
    const [assets, setAssets] = useState<Asset[]>([])
    const [models, setModels] = useState<EquipmentModel[]>([])
    const [isLoading, setIsLoading] = useState(false)
    const [activeActionId, setActiveActionId] = useState<string | null>(null)

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
            showError('Failed to load reservations.')
        } finally {
            setIsLoading(false)
        }
    }

    useEffect(() => {
        loadData()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    async function runReservationAction(
        reservationId: string,
        action: () => Promise<unknown>,
        successMessage: string,
        errorMessage: string,
    ) {
        setActiveActionId(reservationId)

        try {
            await action()
            showSuccess(successMessage)
            await loadData()
        } catch {
            showError(errorMessage)
        } finally {
            setActiveActionId(null)
        }
    }

    async function handleApprove(reservation: Reservation) {
        await runReservationAction(
            reservation.id,
            () => approveReservation(reservation.id),
            'Reservation approved.',
            'Failed to approve reservation.',
        )
    }

    async function handleReject(reservation: Reservation) {
        const reason = window.prompt('Rejection reason:', 'Not available for this period')

        if (!reason || reason.trim() === '') {
            showError('Rejection reason is required.')
            return
        }

        await runReservationAction(
            reservation.id,
            () => rejectReservation(reservation.id, reason.trim()),
            'Reservation rejected.',
            'Failed to reject reservation.',
        )
    }

    async function handleCancel(reservation: Reservation) {
        const confirmed = window.confirm('Cancel this reservation?')

        if (!confirmed) {
            return
        }

        await runReservationAction(
            reservation.id,
            () => cancelReservation(reservation.id),
            'Reservation cancelled.',
            'Failed to cancel reservation.',
        )
    }

    async function handleCheckout(reservation: Reservation) {
        await runReservationAction(
            reservation.id,
            () => checkoutReservation(reservation.id),
            'Equipment checked out. Rental has been created.',
            'Failed to checkout equipment.',
        )
    }

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

    return (
        <div>
            <PageHeader
                title="Reservations"
                description="Review, approve, reject, cancel and checkout equipment reservations."
            />

            <section className="card section-card">
                <div className="section-title-row">
                    <div>
                        <h3>Reservation list</h3>
                        <p>Manage the full reservation lifecycle before equipment checkout.</p>
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
                            <th>User ID</th>
                            <th>Period</th>
                            <th>Status</th>
                            <th>Rejection reason</th>
                            <th>Created at</th>
                            <th>Actions</th>
                        </tr>
                        </thead>

                        <tbody>
                        {reservations.map((reservation) => {
                            const isPending = reservation.status === 'PENDING'
                            const isApproved = reservation.status === 'APPROVED'
                            const canCancel = isPending || isApproved
                            const isBusy = activeActionId === reservation.id

                            return (
                                <tr key={reservation.id}>
                                    <td className="mono">{shortId(reservation.id)}</td>
                                    <td>{getAssetLabel(reservation.assetId)}</td>
                                    <td className="mono">{shortId(reservation.userId)}</td>
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
                                        <div className="actions">
                                            {isPending && (
                                                <>
                                                    <button
                                                        type="button"
                                                        className="success-button"
                                                        disabled={isBusy}
                                                        onClick={() => handleApprove(reservation)}
                                                    >
                                                        Approve
                                                    </button>

                                                    <button
                                                        type="button"
                                                        className="danger-button"
                                                        disabled={isBusy}
                                                        onClick={() => handleReject(reservation)}
                                                    >
                                                        Reject
                                                    </button>
                                                </>
                                            )}

                                            {isApproved && (
                                                <button
                                                    type="button"
                                                    disabled={isBusy}
                                                    onClick={() => handleCheckout(reservation)}
                                                >
                                                    Checkout
                                                </button>
                                            )}

                                            {canCancel && (
                                                <button
                                                    type="button"
                                                    className="secondary-button"
                                                    disabled={isBusy}
                                                    onClick={() => handleCancel(reservation)}
                                                >
                                                    Cancel
                                                </button>
                                            )}

                                            {!isPending && !isApproved && !canCancel && (
                                                <span className="muted-text">No actions</span>
                                            )}
                                        </div>
                                    </td>
                                </tr>
                            )
                        })}

                        {reservations.length === 0 && (
                            <tr>
                                <td colSpan={8}>No reservations found.</td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    )
}