import { Link, isRouteErrorResponse, useRouteError } from 'react-router-dom'

function getErrorMessage(error: unknown) {
    if (isRouteErrorResponse(error)) {
        return `${error.status} ${error.statusText}`
    }

    if (error instanceof Error) {
        return error.message
    }

    return 'Unexpected application error.'
}

export function ErrorPage() {
    const error = useRouteError()

    return (
        <div className="error-page">
            <div className="error-card">
                <span className="error-label">Application error</span>

                <h1>Something went wrong</h1>

                <p>
                    The application encountered an unexpected problem. You can
                    safely return to the dashboard and continue working.
                </p>

                <pre>{getErrorMessage(error)}</pre>

                <div className="error-actions">
                    <Link to="/">Back to dashboard</Link>
                    <button type="button" onClick={() => window.location.reload()}>
                        Reload page
                    </button>
                </div>
            </div>
        </div>
    )
}