import { useEffect, useMemo, useState } from 'react'
import { useOutletContext } from 'react-router-dom'
import { getApiErrorMessage } from '../api/apiError'
import { getAssets, getModels } from '../api/inventoryApi'
import { getRentals, returnRental } from '../api/rentalApi'
import { PageHeader } from '../components/PageHeader'
import { StatusBadge } from '../components/StatusBadge'
import type { Asset, EquipmentModel } from '../types/inventory'
import type { Rental } from '../types/rental'
import type { ToastContext } from '../types/toastContext'

type RentalFilter = 'ALL' | 'ACTIVE' | 'CLOSED'

function formatDateTime(value: string | null | undefined) {
    if (!value) {
        return '-'
    }

    return new Date(value).toLocaleString()
}

function shortId(value: string) {
    return value.slice(0, 8)
}

function toDateTimeLocalValue(date: Date) {
    const timezoneOffsetMs = date.getTimezoneOffset() * 60_000
    return new Date(date.getTime() - timezoneOffsetMs).toISOString().slice(0, 16)
}

function toIsoStringFromLocalValue(value: string) {
    return new Date(value).toISOString()
}

function isOverdue(rental: Rental) {
    if (rental.status !== 'ACTIVE') {
        return false
    }

    return new Date(rental.expectedReturnAt).getTime() < Date.now()
}

export function RentalsPage() {
    const { showSuccess, showError } = useOutletContext<ToastContext>()

    const [rentals, setRentals] = useState<Rental[]>([])
    const [assets, setAssets] = useState<Asset[]>([])
    const [models, setModels] = useState<EquipmentModel[]>([])
    const [isLoading, setIsLoading] = useState(false)
    const [activeActionId, setActiveActionId] = useState<string | null>(null)
    const [filter, setFilter] = useState<RentalFilter>('ACTIVE')

    const assetById = useMemo(() => {
        return new Map(assets.map((asset) => [asset.id, asset]))
    }, [assets])

    const modelById = useMemo(() => {
        return new Map(models.map((model) => [model.id, model]))
    }, [models])

    const activeRentals = rentals.filter((rental) => rental.status === 'ACTIVE')
    const closedRentals = rentals.filter((rental) => rental.status === 'CLOSED')
    const overdueRentals = rentals.filter((rental) => isOverdue(rental))

    const filteredRentals = useMemo(() => {
        if (filter === 'ALL') {
            return rentals
        }

        return rentals.filter((rental) => rental.status === filter)
    }, [filter, rentals])

    async function loadData() {
        setIsLoading(true)

        try {
            const [rentalsResult, assetsResult, modelsResult] = await Promise.all([
                getRentals(),
                getAssets(),
                getModels(),
            ])

            setRentals(rentalsResult)
            setAssets(assetsResult)
            setModels(modelsResult)
        } catch (error) {
            showError(getApiErrorMessage(error, 'Failed to load rentals.'))
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

    async function handleNormalReturn(rental: Rental) {
        const returnedAt = window.prompt(
            'Returned at:',
            toDateTimeLocalValue(new Date()),
        )

        if (!returnedAt || returnedAt.trim() === '') {
            showError('Returned at value is required.')
            return
        }

        const returnedAtIso = toIsoStringFromLocalValue(returnedAt)

        await runReturnAction(
            rental.id,
            {
                damaged: false,
                damageReport: null,
                returnedAt: returnedAtIso,
            },
            new Date(returnedAtIso) > new Date(rental.expectedReturnAt)
                ? 'Equipment returned after expected time. User should be blocked by overdue policy.'
                : 'Equipment returned successfully.',
            'Failed to return equipment.',
        )
    }

    async function handleDamagedReturn(rental: Rental) {
        const returnedAt = window.prompt(
            'Returned at:',
            toDateTimeLocalValue(new Date()),
        )

        if (!returnedAt || returnedAt.trim() === '') {
            showError('Returned at value is required.')
            return
        }

        const damageReport = window.prompt(
            'Damage report:',
            'Reported during return',
        )

        if (!damageReport || damageReport.trim() === '') {
            showError('Damage report is required for damaged return.')
            return
        }

        await runReturnAction(
            rental.id,
            {
                damaged: true,
                damageReport: damageReport.trim(),
                returnedAt: toIsoStringFromLocalValue(returnedAt),
            },
            'Damaged equipment return registered. Inventory context should mark the asset as damaged.',
            'Failed to register damaged return.',
        )
    }

    async function handleOverdueReturn(rental: Rental) {
        const expectedReturnDate = new Date(rental.expectedReturnAt)
        const overdueDate = new Date(expectedReturnDate.getTime() + 2 * 60 * 60 * 1000)

        const returnedAt = window.prompt(
            'Returned at after expected return date:',
            toDateTimeLocalValue(overdueDate),
        )

        if (!returnedAt || returnedAt.trim() === '') {
            showError('Returned at value is required.')
            return
        }

        const returnedAtIso = toIsoStringFromLocalValue(returnedAt)

        if (new Date(returnedAtIso) <= new Date(rental.expectedReturnAt)) {
            showError('Returned at must be after expected return date for overdue return.')
            return
        }

        await runReturnAction(
            rental.id,
            {
                damaged: false,
                damageReport: null,
                returnedAt: returnedAtIso,
            },
            'Overdue return registered. Identity context should block the user account.',
            'Failed to register overdue return.',
        )
    }

    async function runReturnAction(
        rentalId: string,
        payload: {
            damaged: boolean
            damageReport?: string | null
            returnedAt?: string | null
        },
        successMessage: string,
        fallbackErrorMessage: string,
    ) {
        setActiveActionId(rentalId)

        try {
            await returnRental(rentalId, payload)
            showSuccess(successMessage)
            await loadData()
        } catch (error) {
            showError(getApiErrorMessage(error, fallbackErrorMessage))
        } finally {
            setActiveActionId(null)
        }
    }

    return (
        <div>
            <PageHeader
                title="Rentals"
                description="Register equipment returns and demonstrate damaged or overdue return policies."
            />

            <section className="workflow-grid section-card">
                <div className="workflow-card">
                    <span className="workflow-step">Active</span>
                    <strong>Currently borrowed</strong>
                    <p>Equipment checked out and not returned yet.</p>
                    <span className="workflow-count">{activeRentals.length}</span>
                </div>

                <div className="workflow-card">
                    <span className="workflow-step">Overdue</span>
                    <strong>After expected return</strong>
                    <p>Returning these rentals should trigger user account block.</p>
                    <span className="workflow-count">{overdueRentals.length}</span>
                </div>

                <div className="workflow-card">
                    <span className="workflow-step">Closed</span>
                    <strong>Returned equipment</strong>
                    <p>Completed rentals after normal or exceptional return.</p>
                    <span className="workflow-count">{closedRentals.length}</span>
                </div>
            </section>

            <section className="card section-card">
                <div className="section-title-row">
                    <div>
                        <h3>Rental list</h3>
                        <p>
                            Register normal returns, damaged returns and overdue returns
                            from this lab assistant panel.
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
                        className={filter === 'ACTIVE' ? 'active-filter-tab' : ''}
                        onClick={() => setFilter('ACTIVE')}
                    >
                        Active
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
                            <th>Checkout at</th>
                            <th>Expected return</th>
                            <th>Returned at</th>
                            <th>Status</th>
                            <th>Return state</th>
                            <th>Actions</th>
                        </tr>
                        </thead>

                        <tbody>
                        {filteredRentals.map((rental) => {
                            const rentalIsActive = rental.status === 'ACTIVE'
                            const rentalIsOverdue = isOverdue(rental)
                            const isBusy = activeActionId === rental.id

                            return (
                                <tr key={rental.id}>
                                    <td className="mono">{shortId(rental.id)}</td>
                                    <td>{getAssetLabel(rental.assetId)}</td>
                                    <td className="mono">{shortId(rental.userId)}</td>
                                    <td>{formatDateTime(rental.checkoutAt)}</td>
                                    <td>{formatDateTime(rental.expectedReturnAt)}</td>
                                    <td>{formatDateTime(rental.returnedAt)}</td>
                                    <td>
                                        <StatusBadge value={rental.status} />
                                    </td>
                                    <td>
                                        {rentalIsOverdue ? (
                                            <span className="return-state overdue-state">
                                                Overdue
                                            </span>
                                        ) : rentalIsActive ? (
                                            <span className="return-state active-state">
                                                On time
                                            </span>
                                        ) : (
                                            <span className="return-state closed-state">
                                                Closed
                                            </span>
                                        )}
                                    </td>
                                    <td>
                                        {rentalIsActive ? (
                                            <div className="actions">
                                                <button
                                                    type="button"
                                                    disabled={isBusy}
                                                    onClick={() => handleNormalReturn(rental)}
                                                >
                                                    Return
                                                </button>

                                                <button
                                                    type="button"
                                                    className="danger-button"
                                                    disabled={isBusy}
                                                    onClick={() => handleDamagedReturn(rental)}
                                                >
                                                    Return damaged
                                                </button>

                                                <button
                                                    type="button"
                                                    className="warning-button"
                                                    disabled={isBusy}
                                                    onClick={() => handleOverdueReturn(rental)}
                                                >
                                                    Return overdue
                                                </button>
                                            </div>
                                        ) : (
                                            <span className="muted-text">Closed</span>
                                        )}
                                    </td>
                                </tr>
                            )
                        })}

                        {filteredRentals.length === 0 && (
                            <tr>
                                <td colSpan={9}>No rentals found for this filter.</td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            </section>

            <section className="card section-card">
                <h3>Event-driven behavior</h3>
                <div className="event-flow-grid">
                    <div>
                        <strong>Overdue return</strong>
                        <p>
                            Rental context publishes <span className="mono">RentalOverdueEvent</span>.
                            Identity context handles it and blocks the user account.
                        </p>
                    </div>

                    <div>
                        <strong>Damaged return</strong>
                        <p>
                            Rental context publishes{' '}
                            <span className="mono">EquipmentReturnedWithDamageEvent</span>.
                            Inventory context handles it and marks the asset as damaged.
                        </p>
                    </div>
                </div>
            </section>
        </div>
    )
}