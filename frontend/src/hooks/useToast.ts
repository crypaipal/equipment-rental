import { useCallback, useState } from 'react'
import type { Toast, ToastType } from '../types/toast'

const TOAST_TIMEOUT_MS = 3500

export function useToast() {
    const [toasts, setToasts] = useState<Toast[]>([])

    const removeToast = useCallback((id: number) => {
        setToasts((currentToasts) =>
            currentToasts.filter((toast) => toast.id !== id),
        )
    }, [])

    const showToast = useCallback(
        (type: ToastType, message: string) => {
            const id = Date.now() + Math.random()

            setToasts((currentToasts) => [
                ...currentToasts,
                {
                    id,
                    type,
                    message,
                },
            ])

            window.setTimeout(() => {
                removeToast(id)
            }, TOAST_TIMEOUT_MS)
        },
        [removeToast],
    )

    const showSuccess = useCallback(
        (message: string) => showToast('success', message),
        [showToast],
    )

    const showError = useCallback(
        (message: string) => showToast('error', message),
        [showToast],
    )

    return {
        toasts,
        showSuccess,
        showError,
        removeToast,
    }
}