import { apiClient } from './apiClient'
import type { Asset, AssetCondition, EquipmentModel } from '../types/inventory'

export async function getModels() {
    const response = await apiClient.get<EquipmentModel[]>('/inventory/models')
    return response.data
}

export async function createModel(payload: {
    name: string
    category: string
    manufacturer: string
}) {
    const response = await apiClient.post<EquipmentModel>('/inventory/models', payload)
    return response.data
}

export async function getAssets() {
    const response = await apiClient.get<Asset[]>('/inventory/assets')
    return response.data
}

export async function createAsset(payload: {
    equipmentModelId: string
    inventoryTag: string
}) {
    const response = await apiClient.post<Asset>('/inventory/assets', payload)
    return response.data
}

export async function changeAssetCondition(
    assetId: string,
    payload: {
        condition: AssetCondition
        damageReport?: string | null
    },
) {
    const response = await apiClient.patch<Asset>(
        `/inventory/assets/${assetId}/condition`,
        payload,
    )

    return response.data
}