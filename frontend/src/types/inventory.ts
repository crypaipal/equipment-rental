export type AssetCondition = 'OPERATIONAL' | 'DAMAGED' | 'IN_REPAIR'

export interface EquipmentModel {
    id: string
    name: string
    category: string
    manufacturer: string
}

export interface Asset {
    id: string
    equipmentModelId: string
    inventoryTag: string
    condition: AssetCondition
    damageReport?: string | null
}

export interface AvailableAsset {
    assetId: string
    equipmentModelId: string
    inventoryTag: string
    modelName: string
    category: string
    manufacturer: string
}