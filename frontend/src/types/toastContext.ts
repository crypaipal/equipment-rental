import type { Toast } from './toast'

export interface ToastContext {
    toasts: Toast[]
    showSuccess: (message: string) => void
    showError: (message: string) => void
    removeToast: (id: number) => void
}