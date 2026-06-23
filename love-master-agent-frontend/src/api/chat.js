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

async function streamSse(url, { signal, onChunk }) {
  const response = await fetch(url, {
    method: 'GET',
    headers: {
      Accept: 'text/event-stream'
    },
    signal
  })

  if (!response.ok) {
    throw new Error(`SSE request failed: ${response.status}`)
  }

  if (!response.body) {
    throw new Error('Current browser does not support readable streams.')
  }

  const reader = response.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let lineBuffer = ''
  let eventDataLines = []

  const dispatchEvent = () => {
    if (eventDataLines.length === 0) {
      return
    }

    const value = eventDataLines.join('\n')
    eventDataLines = []

    if (value && value !== '[DONE]') {
      onChunk(value)
    }
  }

  const handleLine = (line) => {
    const normalizedLine = line.endsWith('\r') ? line.slice(0, -1) : line

    if (!normalizedLine) {
      dispatchEvent()
      return
    }

    if (normalizedLine.startsWith(':')) {
      return
    }

    if (!normalizedLine.startsWith('data:')) {
      eventDataLines.push(normalizedLine)
      return
    }

    const value = normalizedLine.slice(5).replace(/^ /, '')
    eventDataLines.push(value)
  }

  const handleText = (text) => {
    lineBuffer += text.replaceAll('\r\n', '\n').replaceAll('\r', '\n')

    let lineEndIndex = lineBuffer.indexOf('\n')
    while (lineEndIndex !== -1) {
      const line = lineBuffer.slice(0, lineEndIndex)
      lineBuffer = lineBuffer.slice(lineEndIndex + 1)
      handleLine(line)
      lineEndIndex = lineBuffer.indexOf('\n')
    }
  }

  while (true) {
    const { done, value } = await reader.read()

    if (done) {
      break
    }

    handleText(decoder.decode(value, { stream: true }))
  }

  const lastChunk = decoder.decode()
  if (lastChunk) {
    handleText(lastChunk)
  }

  const tail = lineBuffer.trim()
  if (tail && tail !== '[DONE]') {
    handleLine(tail)
  }
  dispatchEvent()
}

export function streamLoveAppChat({ message, chatId, signal, onChunk }) {
  return streamSse(
    buildSseUrl('/ai/love_app/chat/sse', {
      message,
      chatId
    }),
    { signal, onChunk }
  )
}

export function streamManusChat({ message, signal, onChunk }) {
  return streamSse(
    buildSseUrl('/ai/manus/chat', {
      message
    }),
    { signal, onChunk }
  )
}
