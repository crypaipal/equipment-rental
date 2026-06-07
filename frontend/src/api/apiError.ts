import axios from 'axios'

export function getApiErrorMessage(error: unknown, fallbackMessage: string) {
    if (axios.isAxiosError(error)) {
        const responseData = error.response?.data

        if (
            responseData &&
            typeof responseData === 'object' &&
            'message' in responseData &&
            typeof responseData.message === 'string'
        ) {
            return responseData.message
        }
    }

    return fallbackMessage
}