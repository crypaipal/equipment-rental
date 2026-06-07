import { apiClient } from './apiClient'
import type { AvailableAsset } from '../types/inventory'
import type { Rental, Reservation } from '../types/rental'

export async function findAvailableAssets(params: {
    category: string
    periodFrom: string
    periodTo: string
}) {
    const response = await apiClient.get<AvailableAsset[]>(
        '/rental/available-assets',
        { params },
    )

    return response.data
}

export async function createReservation(payload: {
    userId: string
    assetId: string
    periodFrom: string
    periodTo: string
}) {
    const response = await apiClient.post<Reservation>(
        '/rental/reservations',
        payload,
    )

    return response.data
}

export async function getReservations() {
    const response = await apiClient.get<Reservation[]>('/rental/reservations')
    return response.data
}

export async function approveReservation(reservationId: string) {
    const response = await apiClient.post<Reservation>(
        `/rental/reservations/${reservationId}/approve`,
    )

    return response.data
}

export async function rejectReservation(
    reservationId: string,
    rejectionReason: string,
) {
    const response = await apiClient.post<Reservation>(
        `/rental/reservations/${reservationId}/reject`,
        { rejectionReason },
    )

    return response.data
}

export async function cancelReservation(reservationId: string) {
    const response = await apiClient.post<Reservation>(
        `/rental/reservations/${reservationId}/cancel`,
    )

    return response.data
}

export async function checkoutReservation(reservationId: string) {
    const response = await apiClient.post<Rental>(
        `/rental/reservations/${reservationId}/checkout`,
    )

    return response.data
}

export async function getRentals() {
    const response = await apiClient.get<Rental[]>('/rental/rentals')
    return response.data
}

export async function returnRental(
    rentalId: string,
    payload: {
        damaged: boolean
        damageReport?: string | null
        returnedAt?: string | null
    },
) {
    const response = await apiClient.post<Rental>(
        `/rental/rentals/${rentalId}/return`,
        payload,
    )

    return response.data
}