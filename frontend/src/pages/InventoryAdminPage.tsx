import { useEffect, useMemo, useState } from 'react'
import { useOutletContext } from 'react-router-dom'
import {
    changeAssetCondition,
    createAsset,
    createModel,
    getAssets,
    getModels,
} from '../api/inventoryApi'
import { PageHeader } from '../components/PageHeader'
import { StatusBadge } from '../components/StatusBadge'
import type { Asset, AssetCondition, EquipmentModel } from '../types/inventory'
import type { ToastContext } from '../types/toastContext'

export function InventoryAdminPage() {
    const { showSuccess, showError } = useOutletContext<ToastContext>()

    const [models, setModels] = useState<EquipmentModel[]>([])
    const [assets, setAssets] = useState<Asset[]>([])
    const [isLoading, setIsLoading] = useState(false)

    const [modelName, setModelName] = useState('Dell XPS 13')
    const [category, setCategory] = useState('Laptop')
    const [manufacturer, setManufacturer] = useState('Dell')

    const [selectedModelId, setSelectedModelId] = useState('')
    const [inventoryTag, setInventoryTag] = useState(`LAP-${Date.now()}`)

    const modelById = useMemo(() => {
        return new Map(models.map((model) => [model.id, model]))
    }, [models])

    async function loadInventoryData() {
        setIsLoading(true)

        try {
            const [modelsResult, assetsResult] = await Promise.all([
                getModels(),
                getAssets(),
            ])

            setModels(modelsResult)
            setAssets(assetsResult)

            if (!selectedModelId && modelsResult.length > 0) {
                setSelectedModelId(modelsResult[0].id)
            }
        } catch {
            showError('Failed to load inventory data.')
        } finally {
            setIsLoading(false)
        }
    }

    useEffect(() => {
        loadInventoryData()
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    async function handleCreateModel(event: React.FormEvent) {
        event.preventDefault()

        if (!modelName.trim() || !category.trim() || !manufacturer.trim()) {
            showError('Model name, category and manufacturer are required.')
            return
        }

        try {
            const createdModel = await createModel({
                name: modelName.trim(),
                category: category.trim(),
                manufacturer: manufacturer.trim(),
            })

            showSuccess(`Equipment model created: ${createdModel.name}.`)

            setModelName('')
            setCategory('')
            setManufacturer('')
            setSelectedModelId(createdModel.id)

            await loadInventoryData()
        } catch {
            showError('Failed to create equipment model.')
        }
    }

    async function handleCreateAsset(event: React.FormEvent) {
        event.preventDefault()

        if (!selectedModelId) {
            showError('Select an equipment model first.')
            return
        }

        if (!inventoryTag.trim()) {
            showError('Inventory tag is required.')
            return
        }

        try {
            const createdAsset = await createAsset({
                equipmentModelId: selectedModelId,
                inventoryTag: inventoryTag.trim(),
            })

            showSuccess(`Asset created: ${createdAsset.inventoryTag}.`)
            setInventoryTag(`ASSET-${Date.now()}`)

            await loadInventoryData()
        } catch {
            showError('Failed to create asset. Inventory tag may already exist.')
        }
    }

    async function handleConditionChange(
        assetId: string,
        condition: AssetCondition,
    ) {
        const damageReport =
            condition === 'DAMAGED'
                ? window.prompt('Damage report:', 'Reported from inventory panel')
                : null

        if (condition === 'DAMAGED' && (!damageReport || damageReport.trim() === '')) {
            showError('Damage report is required for DAMAGED condition.')
            return
        }

        try {
            await changeAssetCondition(assetId, {
                condition,
                damageReport: damageReport?.trim() ?? null,
            })

            showSuccess('Asset condition updated.')
            await loadInventoryData()
        } catch {
            showError('Failed to update asset condition.')
        }
    }

    return (
        <div>
            <PageHeader
                title="Inventory management"
                description="Manage equipment models, physical assets and technical condition."
            />

            <div className="two-column-grid">
                <section className="card">
                    <h3>Create equipment model</h3>
                    <p className="section-description">
                        Define a reusable model such as laptop, camera or laboratory device.
                    </p>

                    <form className="stack-form" onSubmit={handleCreateModel}>
                        <label>
                            Model name
                            <input
                                value={modelName}
                                onChange={(event) => setModelName(event.target.value)}
                                placeholder="e.g. Dell XPS 13"
                            />
                        </label>

                        <label>
                            Category
                            <input
                                value={category}
                                onChange={(event) => setCategory(event.target.value)}
                                placeholder="e.g. Laptop"
                            />
                        </label>

                        <label>
                            Manufacturer
                            <input
                                value={manufacturer}
                                onChange={(event) => setManufacturer(event.target.value)}
                                placeholder="e.g. Dell"
                            />
                        </label>

                        <button type="submit">Create model</button>
                    </form>
                </section>

                <section className="card">
                    <h3>Create physical asset</h3>
                    <p className="section-description">
                        Register a concrete physical device with a unique inventory tag.
                    </p>

                    <form className="stack-form" onSubmit={handleCreateAsset}>
                        <label>
                            Equipment model
                            <select
                                value={selectedModelId}
                                onChange={(event) => setSelectedModelId(event.target.value)}
                            >
                                <option value="">Select model</option>
                                {models.map((model) => (
                                    <option key={model.id} value={model.id}>
                                        {model.manufacturer} {model.name} / {model.category}
                                    </option>
                                ))}
                            </select>
                        </label>

                        <label>
                            Inventory tag
                            <input
                                value={inventoryTag}
                                onChange={(event) => setInventoryTag(event.target.value)}
                                placeholder="e.g. LAP-001"
                            />
                        </label>

                        <button type="submit">Create asset</button>
                    </form>
                </section>
            </div>

            <section className="card section-card">
                <div className="section-title-row">
                    <div>
                        <h3>Equipment models</h3>
                        <p>Reusable model definitions stored in the inventory catalog.</p>
                    </div>

                    <button type="button" onClick={loadInventoryData}>
                        Refresh
                    </button>
                </div>

                {isLoading ? (
                    <p>Loading inventory data...</p>
                ) : (
                    <div className="table-wrapper">
                        <table>
                            <thead>
                            <tr>
                                <th>Name</th>
                                <th>Category</th>
                                <th>Manufacturer</th>
                                <th>ID</th>
                            </tr>
                            </thead>
                            <tbody>
                            {models.map((model) => (
                                <tr key={model.id}>
                                    <td>{model.name}</td>
                                    <td>{model.category}</td>
                                    <td>{model.manufacturer}</td>
                                    <td className="mono">{model.id}</td>
                                </tr>
                            ))}

                            {models.length === 0 && (
                                <tr>
                                    <td colSpan={4}>No equipment models found.</td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                )}
            </section>

            <section className="card section-card">
                <h3>Physical assets</h3>
                <p className="section-description">
                    Damaged assets and assets under repair should not be available for
                    reservation.
                </p>

                <div className="table-wrapper">
                    <table>
                        <thead>
                        <tr>
                            <th>Inventory tag</th>
                            <th>Model</th>
                            <th>Category</th>
                            <th>Condition</th>
                            <th>Damage report</th>
                            <th>Actions</th>
                        </tr>
                        </thead>

                        <tbody>
                        {assets.map((asset) => {
                            const model = modelById.get(asset.equipmentModelId)

                            return (
                                <tr key={asset.id}>
                                    <td className="mono">{asset.inventoryTag}</td>
                                    <td>
                                        {model
                                            ? `${model.manufacturer} ${model.name}`
                                            : asset.equipmentModelId}
                                    </td>
                                    <td>{model?.category ?? '-'}</td>
                                    <td>
                                        <StatusBadge value={asset.condition} />
                                    </td>
                                    <td>{asset.damageReport ?? '-'}</td>
                                    <td>
                                        <div className="actions">
                                            <button
                                                type="button"
                                                className="secondary-button"
                                                onClick={() =>
                                                    handleConditionChange(asset.id, 'OPERATIONAL')
                                                }
                                            >
                                                Operational
                                            </button>

                                            <button
                                                type="button"
                                                className="secondary-button"
                                                onClick={() =>
                                                    handleConditionChange(asset.id, 'IN_REPAIR')
                                                }
                                            >
                                                In repair
                                            </button>

                                            <button
                                                type="button"
                                                className="danger-button"
                                                onClick={() =>
                                                    handleConditionChange(asset.id, 'DAMAGED')
                                                }
                                            >
                                                Damaged
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            )
                        })}

                        {assets.length === 0 && (
                            <tr>
                                <td colSpan={6}>No physical assets found.</td>
                            </tr>
                        )}
                        </tbody>
                    </table>
                </div>
            </section>
        </div>
    )
}