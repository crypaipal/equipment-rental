export type ReservationStatus =
    | 'PENDING'
    | 'APPROVED'
    | 'REJECTED'
    | 'FULFILLED'
    | 'CANCELLED'

export type RentalStatus = 'ACTIVE' | 'CLOSED'

export interface Reservation {
    id: string
    userId: string
    assetId: string
    periodFrom: string
    periodTo: string
    status: ReservationStatus
    rejectionReason?: string | null
    createdAt: string
}

export interface Rental {
    id: string
    reservationId: string
    userId: string
    assetId: string
    checkoutAt: string
    expectedReturnAt: string
    returnedAt?: string | null
    status: RentalStatus
}