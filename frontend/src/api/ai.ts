import http from './http'
import { useAuthStore } from '@/stores/auth'

// ===== 类型 =====
export interface AiSession {
  id: number
  title: string
  createTime?: string
  updateTime?: string
}

export interface AiMessage {
  id: number
  role: 'user' | 'assistant' | 'system' | 'tool'
  content?: string
  toolName?: string
  toolArguments?: string
  toolResult?: string
  createTime?: string
}

// ===== REST =====
export const aiApi = {
  status: () => http.get<any, { enabled: boolean }>('/ai/status'),

  listSessions: () => http.get<any, AiSession[]>('/ai/sessions'),
  createSession: (title?: string) => http.post<any, AiSession>('/ai/sessions', { title }),
  deleteSession: (id: number) => http.delete(`/ai/sessions/${id}`),

  listMessages: (id: number) => http.get<any, AiMessage[]>(`/ai/sessions/${id}/messages`)
}

// ===== SSE: 用 fetch + ReadableStream 解析 =====
export interface StreamCallbacks {
  onDelta: (token: string) => void
  onToolCall?: (data: { name: string; arguments: any }) => void
  onToolResult?: (data: { name: string; result: any }) => void
  onError?: (message: string) => void
  onDone?: () => void
}

export async function chatStream(
  sessionId: number,
  message: string,
  cb: StreamCallbacks,
  signal?: AbortSignal,
  endpoint: 'messages' | 'regenerate' = 'messages'
) {
  const auth = useAuthStore()
  const body = endpoint === 'messages' ? JSON.stringify({ message }) : ''

  const resp = await fetch(`/api/ai/sessions/${sessionId}/${endpoint}`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'text/event-stream',
      Authorization: auth.token ? `Bearer ${auth.token}` : ''
    },
    body,
    signal
  })

  if (!resp.ok || !resp.body) {
    const text = await resp.text().catch(() => '')
    cb.onError?.(text || `HTTP ${resp.status}`)
    return
  }

  const reader = resp.body.getReader()
  const decoder = new TextDecoder('utf-8')
  let buf = ''

  // 标准 SSE 分隔：连续两个换行（\n\n 或 \r\n\r\n）划一个 event
  while (true) {
    const { value, done } = await reader.read()
    if (done) break
    buf += decoder.decode(value, { stream: true })
    let idx
    // 兼容 CRLF
    while ((idx = indexOfDoubleNewline(buf)) >= 0) {
      const raw = buf.slice(0, idx)
      buf = buf.slice(idx + delimiterLength(buf, idx))
      handleEvent(raw, cb)
    }
  }
  cb.onDone?.()
}

function indexOfDoubleNewline(s: string): number {
  const a = s.indexOf('\n\n')
  const b = s.indexOf('\r\n\r\n')
  if (a < 0) return b
  if (b < 0) return a
  return Math.min(a, b)
}
function delimiterLength(s: string, idx: number): number {
  return s.startsWith('\r\n\r\n', idx) ? 4 : 2
}

function handleEvent(raw: string, cb: StreamCallbacks) {
  // 一个 event 内可能有 event:xx 与多行 data:xxx
  let event = 'message'
  const dataLines: string[] = []
  for (const line of raw.split(/\r?\n/)) {
    if (line.startsWith('event:')) event = line.slice(6).trim()
    else if (line.startsWith('data:')) dataLines.push(line.slice(5).replace(/^ /, ''))
  }
  const data = dataLines.join('\n')
  switch (event) {
    case 'delta':       cb.onDelta(data); break
    case 'tool_call':   cb.onToolCall?.(safeJson(data)); break
    case 'tool_result': cb.onToolResult?.(safeJson(data)); break
    case 'error':       cb.onError?.(data); break
    case 'done':        /* 由 reader 自然结束触发 onDone */ break
    default: /* 忽略未知事件 */ break
  }
}

function safeJson(s: string): any {
  try { return JSON.parse(s) } catch { return s }
}
