import { useEffect, useMemo, useState } from 'react'
import { useOutletContext } from 'react-router-dom'
import { getModels } from '../api/inventoryApi'
import { createReservation, findAvailableAssets } from '../api/rentalApi'
import { PageHeader } from '../components/PageHeader'
import type { AvailableAsset, EquipmentModel } from '../types/inventory'
import type { ToastContext } from '../types/toastContext'
import { useAuth } from '../auth/AuthContext'

function toDateTimeLocalValue(date: Date) {
    const timezoneOffsetMs = date.getTimezoneOffset() * 60_000
    return new Date(date.getTime() - timezoneOffsetMs).toISOString().slice(0, 16)
}

function toIsoStringFromLocalValue(value: string) {
    return new Date(value).toISOString()
}

export function EquipmentCatalogPage() {
    const { showSuccess, showError } = useOutletContext<ToastContext>()
    const { user } = useAuth()

    const [models, setModels] = useState<EquipmentModel[]>([])
    const [availableAssets, setAvailableAssets] = useState<AvailableAsset[]>([])

    const [category, setCategory] = useState('Laptop')
    const [periodFrom, setPeriodFrom] = useState(
        toDateTimeLocalValue(new Date(Date.now() + 60 * 60 * 1000)),
    )
    const [periodTo, setPeriodTo] = useState(
        toDateTimeLocalValue(new Date(Date.now() + 3 * 60 * 60 * 1000)),
    )

    const [isLoadingModels, setIsLoadingModels] = useState(false)
    const [isSearching, setIsSearching] = useState(false)
    const [isReservingAssetId, setIsReservingAssetId] = useState<string | null>(null)

    const categories = useMemo(() => {
        const uniqueCategories = new Set(models.map((model) => model.category))
        return Array.from(uniqueCategories).sort()
    }, [models])

    async function loadModels() {
        setIsLoadingModels(true)

        try {
            const result = await getModels()
            setModels(result)

            if (result.length > 0) {
                setCategory((currentCategory) => {
                    const categoryExists = result.some(
                        (model) => model.category === currentCategory,
                    )

                    return categoryExists ? currentCategory : result[0].category
                })
            }
        } catch {
            showError('Failed to load equipment categories.')
        } finally {
            setIsLoadingModels(false)
        }
    }

    useEffect(() => {
        loadModels()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    async function handleSearch(event: React.FormEvent) {
        event.preventDefault()

        if (!category.trim()) {
            showError('Category is required.')
            return
        }

        if (!periodFrom || !periodTo) {
            showError('Rental period is required.')
            return
        }

        if (new Date(periodFrom) >= new Date(periodTo)) {
            showError('Period start must be before period end.')
            return
        }

        setIsSearching(true)

        try {
            const result = await findAvailableAssets({
                category: category.trim(),
                periodFrom: toIsoStringFromLocalValue(periodFrom),
                periodTo: toIsoStringFromLocalValue(periodTo),
            })

            setAvailableAssets(result)

            if (result.length === 0) {
                showError('No available assets found for the selected period.')
            } else {
                showSuccess(`Found ${result.length} available asset(s).`)
            }
        } catch {
            showError('Failed to search available equipment.')
        } finally {
            setIsSearching(false)
        }
    }

    async function handleReserve(asset: AvailableAsset) {
        if (!user) {
            showError('Log in first to create a reservation.')
            return
        }

        setIsReservingAssetId(asset.assetId)

        try {
            await createReservation({
                userId:  user.userId,
                assetId: asset.assetId,
                periodFrom: toIsoStringFromLocalValue(periodFrom),
                periodTo: toIsoStringFromLocalValue(periodTo),
            })

            showSuccess(`Reservation requested for ${asset.inventoryTag}.`)

            setAvailableAssets((currentAssets) =>
                currentAssets.filter((item) => item.assetId !== asset.assetId),
            )
        } catch {
            showError('Failed to create reservation.')
        } finally {
            setIsReservingAssetId(null)
        }
    }

    return (
        <div>
            <PageHeader
                title="Equipment catalog"
                description="Search for available equipment and create reservation requests."
            />

            <section className="card section-card">
                <h3>Search available equipment</h3>
                <p className="section-description">
                    Select an equipment category and rental period. Only operational and
                    available assets should be returned.
                </p>

                <form className="catalog-search-form" onSubmit={handleSearch}>
                    <label>
                        Category
                        <select
                            value={category}
                            onChange={(event) => setCategory(event.target.value)}
                            disabled={isLoadingModels}
                        >
                            {categories.length === 0 && (
                                <option value={category}>{category}</option>
                            )}

                            {categories.map((item) => (
                                <option key={item} value={item}>
                                    {item}
                                </option>
                            ))}
                        </select>
                    </label>

                    <label>
                        From
                        <input
                            type="datetime-local"
                            value={periodFrom}
                            onChange={(event) => setPeriodFrom(event.target.value)}
                        />
                    </label>

                    <label>
                        To
                        <input
                            type="datetime-local"
                            value={periodTo}
                            onChange={(event) => setPeriodTo(event.target.value)}
                        />
                    </label>

                    <button type="submit" disabled={isSearching}>
                        {isSearching ? 'Searching...' : 'Search'}
                    </button>
                </form>
            </section>

            <section className="card section-card">
                <div className="section-title-row">
                    <div>
                        <h3>Available assets</h3>
                        <p>Assets that can be reserved for the selected rental period.</p>
                    </div>
                </div>

                <div className="table-wrapper">
                    <table>
                        <thead>
                        <tr>
                            <th>Inventory tag</th>
                            <th>Model</th>
                            <th>Category</th>
                            <th>Manufacturer</th>
                            <th>Asset ID</th>
                            <th>Action</th>
                        </tr>
                        </thead>

                        <tbody>
                        {availableAssets.map((asset) => (
                            <tr key={asset.assetId}>
                                <td className="mono">{asset.inventoryTag}</td>
                                <td>{asset.modelName}</td>
                                <td>{asset.category}</td>
                                <td>{asset.manufacturer}</td>
                                <td className="mono">{asset.assetId}</td>
                                <td>
                                    <button
                                        type="button"
                                        disabled={isReservingAssetId === asset.assetId}
                                        onClick={() => handleReserve(asset)}
                                    >
                                        {isReservingAssetId === asset.assetId
                                            ? 'Reserving...'
                                            : 'Reserve'}
                                    </button>
                                </td>
                            </tr>
                        ))}

                        {availableAssets.length === 0 && (
                            <tr>
                                <td colSpan={6}>
                                    No available assets loaded. Use the search form above.
                                </td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    )
}