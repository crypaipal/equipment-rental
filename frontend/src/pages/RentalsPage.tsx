import { useEffect, useMemo, useState } from 'react'
import { useOutletContext } from 'react-router-dom'
import { getAssets, getModels } from '../api/inventoryApi'
import { getRentals, returnRental } from '../api/rentalApi'
import { PageHeader } from '../components/PageHeader'
import { StatusBadge } from '../components/StatusBadge'
import type { Asset, EquipmentModel } from '../types/inventory'
import type { Rental } from '../types/rental'
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

function toDateTimeLocalValue(date: Date) {
    const timezoneOffsetMs = date.getTimezoneOffset() * 60_000
    return new Date(date.getTime() - timezoneOffsetMs).toISOString().slice(0, 16)
}

function toIsoStringFromLocalValue(value: string) {
    return new Date(value).toISOString()
}

export function RentalsPage() {
    const { showSuccess, showError } = useOutletContext<ToastContext>()

    const [rentals, setRentals] = useState<Rental[]>([])
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
            const [rentalsResult, assetsResult, modelsResult] = await Promise.all([
                getRentals(),
                getAssets(),
                getModels(),
            ])

            setRentals(rentalsResult)
            setAssets(assetsResult)
            setModels(modelsResult)
        } catch {
            showError('Failed to load rentals.')
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

        await runReturnAction(
            rental.id,
            {
                damaged: false,
                damageReport: null,
                returnedAt: toIsoStringFromLocalValue(returnedAt),
            },
            'Equipment returned successfully.',
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
            'Damaged equipment return registered. Asset condition should be updated through domain event.',
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

        await runReturnAction(
            rental.id,
            {
                damaged: false,
                damageReport: null,
                returnedAt: toIsoStringFromLocalValue(returnedAt),
            },
            'Overdue return registered. User should be blocked through domain event.',
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
        errorMessage: string,
    ) {
        setActiveActionId(rentalId)

        try {
            await returnRental(rentalId, payload)
            showSuccess(successMessage)
            await loadData()
        } catch {
            showError(errorMessage)
        } finally {
            setActiveActionId(null)
        }
    }

    return (
        <div>
            <PageHeader
                title="Rentals"
                description="View active and closed rentals and register equipment returns."
            />

            <section className="card section-card">
                <div className="section-title-row">
                    <div>
                        <h3>Rental list</h3>
                        <p>
                            Register normal returns, damaged returns and overdue returns to
                            demonstrate domain event handling.
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
                            <th>User ID</th>
                            <th>Checkout at</th>
                            <th>Expected return</th>
                            <th>Returned at</th>
                            <th>Status</th>
                            <th>Actions</th>
                        </tr>
                        </thead>

                        <tbody>
                        {rentals.map((rental) => {
                            const isActive = rental.status === 'ACTIVE'
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
                                        {isActive ? (
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

                        {rentals.length === 0 && (
                            <tr>
                                <td colSpan={8}>No rentals found.</td>
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