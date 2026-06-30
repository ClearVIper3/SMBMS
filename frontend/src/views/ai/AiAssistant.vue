<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  aiApi, chatStream,
  type AiSession, type AiMessage
} from '@/api/ai'

// ============ 状态 ============
const sessions = ref<AiSession[]>([])
const currentSessionId = ref<number | null>(null)
const messages = ref<AiMessage[]>([])
const input = ref('')
const sending = ref(false)
const aiEnabled = ref(true)
const abortCtrl = ref<AbortController | null>(null)
const scrollBox = ref<HTMLElement>()

// 临时缓存当前正在流式接收的 assistant 气泡（不入库，由后端最终入库后下一次拉取替换）
const streamingAssistant = ref<{ text: string; tools: { name: string; arguments?: any; result?: any }[] } | null>(null)

const currentSession = computed(() => sessions.value.find(s => s.id === currentSessionId.value) || null)

// ============ 工具 ============
async function scrollToBottom() {
  await nextTick()
  if (scrollBox.value) scrollBox.value.scrollTop = scrollBox.value.scrollHeight
}

function roleClass(m: AiMessage) {
  return m.role === 'user' ? 'msg msg-user' : 'msg msg-assistant'
}

// ============ 会话 ============
async function loadSessions() {
  sessions.value = await aiApi.listSessions()
  if (!currentSessionId.value && sessions.value.length > 0) {
    await selectSession(sessions.value[0].id)
  }
}

async function selectSession(id: number) {
  currentSessionId.value = id
  messages.value = await aiApi.listMessages(id)
  streamingAssistant.value = null
  scrollToBottom()
}

async function newSession() {
  const s = await aiApi.createSession()
  sessions.value.unshift(s)
  await selectSession(s.id)
}

async function removeSession(id: number) {
  await ElMessageBox.confirm('确定要删除此会话吗？', '提示', { type: 'warning' })
  await aiApi.deleteSession(id)
  sessions.value = sessions.value.filter(s => s.id !== id)
  if (currentSessionId.value === id) {
    currentSessionId.value = null
    messages.value = []
  }
  if (!currentSessionId.value && sessions.value.length > 0) {
    await selectSession(sessions.value[0].id)
  }
  ElMessage.success('已删除')
}

async function clearHistory() {
  if (!currentSessionId.value) return
  await ElMessageBox.confirm('清空将删除本会话所有消息，是否继续？', '清空历史', { type: 'warning' })
  await aiApi.deleteSession(currentSessionId.value)
  // 删除后重新新建一个会话
  await newSession()
}

// ============ 发送/接收 ============
async function send() {
  if (sending.value) return
  const text = input.value.trim()
  if (!text) return
  if (!currentSessionId.value) {
    await newSession()
  }
  const sid = currentSessionId.value!

  // 本地立即追加用户消息气泡
  messages.value.push({ id: Date.now(), role: 'user', content: text })
  input.value = ''
  streamingAssistant.value = { text: '', tools: [] }
  sending.value = true
  scrollToBottom()

  abortCtrl.value = new AbortController()
  try {
    await chatStream(sid, text, {
      onDelta: (token) => {
        if (!streamingAssistant.value) return
        streamingAssistant.value.text += token
        scrollToBottom()
      },
      onToolCall: (d) => {
        streamingAssistant.value?.tools.push({ name: d.name, arguments: d.arguments })
        scrollToBottom()
      },
      onToolResult: (d) => {
        const arr = streamingAssistant.value?.tools
        if (!arr) return
        const last = [...arr].reverse().find(t => t.name === d.name && t.result === undefined)
        if (last) last.result = d.result
        scrollToBottom()
      },
      onError: (msg) => {
        ElMessage.error('AI 出错：' + msg)
      },
      onDone: async () => {
        // 完成后重新拉历史，让 assistant 气泡稳定下来（带 id、时间戳）
        messages.value = await aiApi.listMessages(sid)
        streamingAssistant.value = null
        sending.value = false
        // 会话标题可能被后端改为首问，刷一下列表
        sessions.value = await aiApi.listSessions()
        scrollToBottom()
      }
    }, abortCtrl.value.signal)
  } catch (e: any) {
    if (e?.name !== 'AbortError') ElMessage.error(e?.message || '请求失败')
    sending.value = false
  }
}

function stopGenerate() {
  abortCtrl.value?.abort()
  sending.value = false
  streamingAssistant.value = null
}

async function regenerate() {
  if (!currentSessionId.value || sending.value) return
  const sid = currentSessionId.value
  streamingAssistant.value = { text: '', tools: [] }
  sending.value = true
  abortCtrl.value = new AbortController()
  await chatStream(sid, '', {
    onDelta: (t) => { if (streamingAssistant.value) streamingAssistant.value.text += t; scrollToBottom() },
    onToolCall: (d) => { streamingAssistant.value?.tools.push({ name: d.name, arguments: d.arguments }) },
    onToolResult: (d) => {
      const arr = streamingAssistant.value?.tools
      if (!arr) return
      const last = [...arr].reverse().find(t => t.name === d.name && t.result === undefined)
      if (last) last.result = d.result
    },
    onError: (m) => ElMessage.error('AI 出错：' + m),
    onDone: async () => {
      messages.value = await aiApi.listMessages(sid)
      streamingAssistant.value = null
      sending.value = false
      scrollToBottom()
    }
  }, abortCtrl.value.signal, 'regenerate')
}

// ============ 生命周期 ============
onMounted(async () => {
  try {
    const st = await aiApi.status()
    aiEnabled.value = !!st.enabled
    if (!aiEnabled.value) ElMessage.warning('AI 助手未启用，请联系管理员配置 SMBMS_AI_API_KEY')
  } catch { /* ignore */ }
  await loadSessions()
})
</script>

<template>
  <div class="ai-page">
    <!-- 左侧会话列表 -->
    <div class="ai-side">
      <div class="ai-side-head">
        <el-button type="primary" :icon="undefined" @click="newSession" style="width:100%;">+ 新建会话</el-button>
      </div>
      <div class="ai-side-list">
        <div v-for="s in sessions" :key="s.id"
             class="ai-side-item"
             :class="{ active: s.id === currentSessionId }"
             @click="selectSession(s.id)">
          <span class="ai-side-title">{{ s.title }}</span>
          <el-button link size="small" type="danger" @click.stop="removeSession(s.id)">×</el-button>
        </div>
        <div v-if="sessions.length === 0" class="ai-side-empty">尚无会话</div>
      </div>
    </div>

    <!-- 右侧聊天 -->
    <div class="ai-main">
      <div class="ai-main-head">
        <div>
          <strong>{{ currentSession?.title || '请选择或新建会话' }}</strong>
          <el-tag v-if="!aiEnabled" type="danger" size="small" style="margin-left:8px;">未启用</el-tag>
        </div>
        <div>
          <el-button size="small" @click="regenerate" :disabled="sending || !messages.length">重新生成</el-button>
          <el-button size="small" @click="clearHistory" :disabled="!currentSessionId">清空</el-button>
        </div>
      </div>

      <div class="ai-msg-box" ref="scrollBox">
        <div v-for="m in messages" :key="m.id" :class="roleClass(m)">
          <div class="bubble">
            <!-- 工具调用记录（来自历史） -->
            <div v-if="m.role === 'tool'" class="tool-card">
              <div class="tool-card-title">🔧 {{ m.toolName }}</div>
              <pre v-if="m.toolArguments" class="tool-card-pre">入参: {{ m.toolArguments }}</pre>
              <pre v-if="m.toolResult" class="tool-card-pre">结果: {{ truncate(m.toolResult) }}</pre>
            </div>
            <div v-else>{{ m.content }}</div>
          </div>
        </div>

        <!-- 流式中的 assistant 气泡 -->
        <div v-if="streamingAssistant" class="msg msg-assistant">
          <div class="bubble">
            <div v-for="(t, idx) in streamingAssistant.tools" :key="idx" class="tool-card">
              <div class="tool-card-title">🔧 {{ t.name }} <span v-if="t.result === undefined" class="tool-pending">…</span></div>
              <pre v-if="t.arguments" class="tool-card-pre">入参: {{ JSON.stringify(t.arguments) }}</pre>
              <pre v-if="t.result !== undefined" class="tool-card-pre">结果: {{ truncate(typeof t.result === 'string' ? t.result : JSON.stringify(t.result)) }}</pre>
            </div>
            <div v-if="streamingAssistant.text">{{ streamingAssistant.text }}</div>
            <div v-else-if="streamingAssistant.tools.length === 0" class="ai-thinking">AI 思考中<span class="dot">.</span><span class="dot">.</span><span class="dot">.</span></div>
          </div>
        </div>
      </div>

      <div class="ai-input">
        <el-input
          v-model="input"
          type="textarea"
          :rows="3"
          resize="none"
          placeholder="向 AI 提问，例如：最近7天销售额最高的商品是什么？"
          @keydown.ctrl.enter.prevent="send"
          @keydown.meta.enter.prevent="send"
          :disabled="sending"
        />
        <div class="ai-input-bar">
          <span class="ai-input-tip">Ctrl/⌘ + Enter 发送</span>
          <el-button v-if="!sending" type="primary" @click="send" :disabled="!input.trim()">发送</el-button>
          <el-button v-else type="danger" @click="stopGenerate">停止</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
function truncate(s: string, n = 600) {
  if (!s) return ''
  return s.length > n ? s.slice(0, n) + ' …(已截断)' : s
}
export default { methods: { truncate } }
</script>

<style scoped>
.ai-page { display: flex; height: calc(100vh - 60px - 32px); gap: 0; }
.ai-side {
  width: 240px; background: #f7f7f9; border-right: 1px solid #ebeef5;
  display: flex; flex-direction: column;
}
.ai-side-head { padding: 12px; }
.ai-side-list { flex: 1; overflow-y: auto; }
.ai-side-item {
  padding: 10px 12px; display: flex; align-items: center; justify-content: space-between;
  cursor: pointer; border-left: 3px solid transparent; font-size: 13px;
}
.ai-side-item:hover { background: #ecf5ff; }
.ai-side-item.active { background: #ecf5ff; border-left-color: #409eff; }
.ai-side-title { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }
.ai-side-empty { color: #999; text-align: center; padding: 24px 0; }

.ai-main { flex: 1; display: flex; flex-direction: column; background: #fff; }
.ai-main-head {
  display: flex; justify-content: space-between; align-items: center;
  padding: 10px 16px; border-bottom: 1px solid #ebeef5;
}
.ai-msg-box {
  flex: 1; overflow-y: auto; padding: 16px;
  background: linear-gradient(180deg, #fafbfc 0%, #fff 100%);
}
.msg { margin-bottom: 12px; display: flex; }
.msg-user { justify-content: flex-end; }
.msg-assistant { justify-content: flex-start; }
.bubble {
  max-width: 78%;
  padding: 10px 14px; border-radius: 12px; line-height: 1.6; white-space: pre-wrap; word-break: break-word;
  box-shadow: 0 1px 2px rgba(0,0,0,.05);
}
.msg-user .bubble { background: #409eff; color: #fff; border-top-right-radius: 2px; }
.msg-assistant .bubble { background: #f1f3f5; color: #333; border-top-left-radius: 2px; }

.tool-card {
  background: #fff7e6; border: 1px solid #ffe7ba; border-radius: 8px;
  padding: 8px 10px; margin-bottom: 8px; font-size: 12px;
}
.tool-card-title { font-weight: 600; color: #d46b08; margin-bottom: 4px; }
.tool-card-pre { margin: 4px 0 0; white-space: pre-wrap; word-break: break-all; }
.tool-pending { color: #999; }

.ai-thinking { color: #999; }
.ai-thinking .dot { animation: blink 1.4s infinite; }
.ai-thinking .dot:nth-child(2) { animation-delay: .2s; }
.ai-thinking .dot:nth-child(3) { animation-delay: .4s; }
@keyframes blink { 0%, 80%, 100% { opacity: .2 } 40% { opacity: 1 } }

.ai-input { border-top: 1px solid #ebeef5; padding: 10px 16px 14px; }
.ai-input-bar {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 6px; font-size: 12px; color: #999;
}
</style>
