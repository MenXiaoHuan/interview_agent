import { afterEach, describe, expect, it, vi } from 'vitest'

async function loadApiConfig() {
  vi.resetModules()
  return import('../../utils/api/api-config.js')
}

describe('api config', () => {
  afterEach(() => {
    vi.unstubAllEnvs()
  })

  it('uses trimmed explicit api base url when configured', async () => {
    vi.stubEnv('VITE_API_BASE_URL', ' https://api.example.test ')

    const { BASE_URL } = await loadApiConfig()

    expect(BASE_URL).toBe('https://api.example.test')
  })

  it('defaults to current origin when api base url is not configured', async () => {
    vi.stubEnv('VITE_API_BASE_URL', '')
    window.history.pushState({}, '', '/api-config-test')

    const { BASE_URL } = await loadApiConfig()

    expect(BASE_URL).toBe(window.location.origin)
  })
})
