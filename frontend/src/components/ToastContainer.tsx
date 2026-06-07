import type { Toast } from '../types/toast'

interface ToastContainerProps {
    toasts: Toast[]
    onRemove: (id: number) => void
}

export function ToastContainer({ toasts, onRemove }: ToastContainerProps) {
    return (
        <div className="toast-container">
            {toasts.map((toast) => (
                <button
                    key={toast.id}
                    type="button"
                    className={`toast toast-${toast.type}`}
                    onClick={() => onRemove(toast.id)}
                >
                    {toast.message}
                </button>
            ))}
        </div>
    )
}