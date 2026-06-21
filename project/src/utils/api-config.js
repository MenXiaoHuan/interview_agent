const configuredBaseUrl = String(import.meta.env.VITE_API_BASE_URL || '').trim()
const sameOriginBaseUrl = typeof window !== 'undefined' ? window.location.origin : ''

export const BASE_URL = configuredBaseUrl || sameOriginBaseUrl
