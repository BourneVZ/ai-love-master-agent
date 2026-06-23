import { API_BASE_URL } from './http'

function buildSseUrl(path, params) {
  const url = new URL(`${API_BASE_URL}${path}`)

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      url.searchParams.set(key, value)
    }
  })

  return url.toString()
}

export function createLoveAppSse(message, chatId) {
  return new EventSource(
    buildSseUrl('/ai/love_app/chat/sse', {
      message,
      chatId
    })
  )
}

export function createManusSse(message) {
  return new EventSource(
    buildSseUrl('/ai/manus/chat', {
      message
    })
  )
}
