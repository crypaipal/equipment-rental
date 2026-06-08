import { useEffect, useMemo, useState } from 'react'
import { useOutletContext } from 'react-router-dom'
import { getApiErrorMessage } from '../api/apiError'
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
import type { Reservation, ReservationStatus } from '../types/rental'
import type { ToastContext } from '../types/toastContext'

type ReservationFilter = 'ALL' | 'PENDING' | 'APPROVED' | 'CLOSED'

function formatDateTime(value: string | null | undefined) {
    if (!value) {
        return '-'
    }

    return new Date(value).toLocaleString()
}

function shortId(value: string) {
    return value.slice(0, 8)
}

function isClosedStatus(status: ReservationStatus) {
    return (
        status === 'REJECTED' ||
        status === 'FULFILLED' ||
        status === 'CANCELLED'
    )
}

export function ReservationsPage() {
    const { showSuccess, showError } = useOutletContext<ToastContext>()

    const [reservations, setReservations] = useState<Reservation[]>([])
    const [assets, setAssets] = useState<Asset[]>([])
    const [models, setModels] = useState<EquipmentModel[]>([])
    const [isLoading, setIsLoading] = useState(false)
    const [activeActionId, setActiveActionId] = useState<string | null>(null)
    const [filter, setFilter] = useState<ReservationFilter>('ALL')

    const assetById = useMemo(() => {
        return new Map(assets.map((asset) => [asset.id, asset]))
    }, [assets])

    const modelById = useMemo(() => {
        return new Map(models.map((model) => [model.id, model]))
    }, [models])

    const pendingCount = reservations.filter(
        (reservation) => reservation.status === 'PENDING',
    ).length

    const approvedCount = reservations.filter(
        (reservation) => reservation.status === 'APPROVED',
    ).length

    const fulfilledCount = reservations.filter(
        (reservation) => reservation.status === 'FULFILLED',
    ).length

    const filteredReservations = useMemo(() => {
        if (filter === 'ALL') {
            return reservations
        }

        if (filter === 'CLOSED') {
            return reservations.filter((reservation) =>
                isClosedStatus(reservation.status),
            )
        }

        return reservations.filter((reservation) => reservation.status === filter)
    }, [filter, reservations])

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
        } catch (error) {
            showError(getApiErrorMessage(error, 'Failed to load reservations.'))
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
        fallbackErrorMessage: string,
    ) {
        setActiveActionId(reservationId)

        try {
            await action()
            showSuccess(successMessage)
            await loadData()
        } catch (error) {
            showError(getApiErrorMessage(error, fallbackErrorMessage))
        } finally {
            setActiveActionId(null)
        }
    }

    async function handleApprove(reservation: Reservation) {
        await runReservationAction(
            reservation.id,
            () => approveReservation(reservation.id),
            'Reservation approved. It is ready for equipment checkout.',
            'Failed to approve reservation.',
        )
    }

    async function handleReject(reservation: Reservation) {
        const reason = window.prompt(
            'Rejection reason:',
            'Equipment is not available for this period',
        )

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
        const confirmed = window.confirm(
            'Cancel this reservation? This should be used only before equipment checkout.',
        )

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
        const confirmed = window.confirm(
            'Checkout equipment for this reservation? A new active rental will be created.',
        )

        if (!confirmed) {
            return
        }

        await runReservationAction(
            reservation.id,
            () => checkoutReservation(reservation.id),
            'Equipment checked out. Active rental has been created.',
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
                description="Review requests, approve or reject them, and checkout approved equipment."
            />

            <section className="workflow-grid section-card">
                <div className="workflow-card">
                    <span className="workflow-step">Step 1</span>
                    <strong>Pending requests</strong>
                    <p>New borrower requests waiting for lab assistant decision.</p>
                    <span className="workflow-count">{pendingCount}</span>
                </div>

                <div className="workflow-card">
                    <span className="workflow-step">Step 2</span>
                    <strong>Ready for checkout</strong>
                    <p>Approved reservations that can be physically issued.</p>
                    <span className="workflow-count">{approvedCount}</span>
                </div>

                <div className="workflow-card">
                    <span className="workflow-step">Step 3</span>
                    <strong>Fulfilled</strong>
                    <p>Reservations already converted into active rentals.</p>
                    <span className="workflow-count">{fulfilledCount}</span>
                </div>
            </section>

            <section className="card section-card">
                <div className="section-title-row">
                    <div>
                        <h3>Reservation list</h3>
                        <p>
                            Use this view as the lab assistant panel for reservation
                            review and equipment checkout.
                        </p>
                    </div>

                    <button type="button" onClick={loadData} disabled={isLoading}>
                        {isLoading ? 'Refreshing...' : 'Refresh'}
                    </button>
                </div>

                <div className="filter-tabs">
                    <button
                        type="button"
                        className={filter === 'ALL' ? 'active-filter-tab' : ''}
                        onClick={() => setFilter('ALL')}
                    >
                        All
                    </button>

                    <button
                        type="button"
                        className={filter === 'PENDING' ? 'active-filter-tab' : ''}
                        onClick={() => setFilter('PENDING')}
                    >
                        Pending
                    </button>

                    <button
                        type="button"
                        className={filter === 'APPROVED' ? 'active-filter-tab' : ''}
                        onClick={() => setFilter('APPROVED')}
                    >
                        Ready for checkout
                    </button>

                    <button
                        type="button"
                        className={filter === 'CLOSED' ? 'active-filter-tab' : ''}
                        onClick={() => setFilter('CLOSED')}
                    >
                        Closed
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
                        {filteredReservations.map((reservation) => {
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
                                                    className="success-button"
                                                    disabled={isBusy}
                                                    onClick={() => handleCheckout(reservation)}
                                                >
                                                    Checkout equipment
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

                        {filteredReservations.length === 0 && (
                            <tr>
                                <td colSpan={8}>No reservations found for this filter.</td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    )
}