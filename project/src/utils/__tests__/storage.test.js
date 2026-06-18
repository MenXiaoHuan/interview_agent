import { beforeEach, describe, expect, it } from 'vitest'
import {
  getStorage,
  getToken,
  removeStorage,
  removeToken,
  setStorage,
  setToken,
  setUserInfo
} from '../storage'

describe('storage helpers', () => {
  beforeEach(() => {
    window.sessionStorage.clear()
    window.localStorage.clear()
  })

  it('stores token in session storage through typed helpers', () => {
    setToken('abc')

    expect(getToken()).toBe('abc')
    expect(getStorage('token')).toBe('abc')
    expect(window.sessionStorage.getItem('token')).toBe('abc')
  })

  it('removes token from session storage', () => {
    setStorage('token', 'abc')

    removeToken()

    expect(getStorage('token')).toBeNull()
    expect(window.sessionStorage.getItem('token')).toBeNull()
  })

  it('stores active ai conversation id in session storage', () => {
    setStorage('activeAiConversationChatId', 'chat-1')

    expect(getStorage('activeAiConversationChatId')).toBe('chat-1')
    expect(window.sessionStorage.getItem('activeAiConversationChatId')).toBe('chat-1')
  })

  it('serializes user info objects before storage', () => {
    setUserInfo({ id: 1, username: 'alice' })

    expect(getStorage('userInfo')).toBe('{"id":1,"username":"alice"}')
  })
})
