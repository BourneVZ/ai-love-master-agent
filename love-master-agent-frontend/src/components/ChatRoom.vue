<template>
  <main class="chat-page">
    <header class="chat-header">
      <RouterLink class="icon-button" to="/" aria-label="返回主页">
        <ArrowLeft :size="20" />
      </RouterLink>

      <section class="chat-title-block" :aria-label="title">
        <div class="app-mark" :class="themeClass">
          <component :is="icon" :size="22" />
        </div>
        <div>
          <p class="eyebrow">{{ eyebrow }}</p>
          <h1>{{ title }}</h1>
        </div>
      </section>

      <div class="session-chip" :title="chatId">
        <Hash :size="15" />
        <span>{{ shortChatId }}</span>
      </div>
    </header>

    <section ref="messagePanel" class="message-panel" aria-live="polite">
      <div class="message-list">
        <article
          v-for="message in messages"
          :key="message.id"
          class="message-row"
          :class="message.role === 'user' ? 'message-row-user' : 'message-row-ai'"
        >
          <div class="avatar" :class="message.role === 'user' ? 'avatar-user' : themeClass">
            <UserRound v-if="message.role === 'user'" :size="18" />
            <component v-else :is="icon" :size="18" />
          </div>
          <div class="message-bubble">
            <div class="message-meta">
              <span>{{ message.role === 'user' ? '你' : assistantName }}</span>
              <time>{{ message.time }}</time>
            </div>
            <p>{{ message.content || '正在思考...' }}</p>
          </div>
        </article>
      </div>
    </section>

    <form class="composer" @submit.prevent="sendMessage">
      <label class="sr-only" for="message-input">输入消息</label>
      <textarea
        id="message-input"
        v-model="draft"
        rows="1"
        :placeholder="placeholder"
        :disabled="isStreaming"
        @keydown.enter.exact.prevent="sendMessage"
      />
      <button class="send-button" type="submit" :disabled="!canSend" aria-label="发送消息">
        <LoaderCircle v-if="isStreaming" :size="20" class="spin" />
        <SendHorizontal v-else :size="20" />
      </button>
    </form>

    <p v-if="errorMessage" class="error-message" role="alert">{{ errorMessage }}</p>
  </main>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, ref } from 'vue'
import { RouterLink } from 'vue-router'
import {
  ArrowLeft,
  Hash,
  HeartHandshake,
  LoaderCircle,
  SendHorizontal,
  Sparkles,
  UserRound
} from '@lucide/vue'
import { createLoveAppSse, createManusSse } from '../api/chat'

const props = defineProps({
  mode: {
    type: String,
    required: true,
    validator: (value) => ['love', 'manus'].includes(value)
  }
})

const appConfig = computed(() => {
  if (props.mode === 'love') {
    return {
      title: 'AI 恋爱大师',
      eyebrow: '情感沟通陪伴',
      assistantName: '恋爱大师',
      placeholder: '说说你的关系困惑，按 Enter 发送',
      icon: HeartHandshake,
      themeClass: 'theme-love'
    }
  }

  return {
    title: 'AI 超级智能体',
    eyebrow: '复杂任务协作',
    assistantName: '超级智能体',
    placeholder: '描述你要完成的任务，按 Enter 发送',
    icon: Sparkles,
    themeClass: 'theme-manus'
  }
})

const title = computed(() => appConfig.value.title)
const eyebrow = computed(() => appConfig.value.eyebrow)
const assistantName = computed(() => appConfig.value.assistantName)
const placeholder = computed(() => appConfig.value.placeholder)
const icon = computed(() => appConfig.value.icon)
const themeClass = computed(() => appConfig.value.themeClass)

const chatId = ref(generateChatId())
const draft = ref('')
const messages = ref([
  {
    id: crypto.randomUUID(),
    role: 'assistant',
    content:
      props.mode === 'love'
        ? '你好，我会结合上下文帮你分析情感问题。你可以直接描述现在的情况。'
        : '你好，我可以帮你拆解任务、调用工具并实时返回执行过程。请告诉我目标。',
    time: formatTime()
  }
])
const isStreaming = ref(false)
const errorMessage = ref('')
const messagePanel = ref(null)
let eventSource = null

const shortChatId = computed(() => chatId.value.slice(-8))
const canSend = computed(() => draft.value.trim().length > 0 && !isStreaming.value)

function generateChatId() {
  return `chat_${Date.now()}_${crypto.randomUUID().slice(0, 8)}`
}

function formatTime() {
  return new Intl.DateTimeFormat('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date())
}

function appendMessage(role, content = '') {
  const message = {
    id: crypto.randomUUID(),
    role,
    content,
    time: formatTime()
  }
  messages.value.push(message)
  scrollToBottom()
  return message
}

function appendChunk(message, chunk) {
  message.content += normalizeChunk(chunk)
  scrollToBottom()
}

function normalizeChunk(chunk) {
  if (chunk === '[DONE]') {
    return ''
  }

  return chunk.replaceAll('\\n', '\n')
}

async function scrollToBottom() {
  await nextTick()
  if (messagePanel.value) {
    messagePanel.value.scrollTop = messagePanel.value.scrollHeight
  }
}

function closeStream() {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
}

function sendMessage() {
  const message = draft.value.trim()
  if (!message || isStreaming.value) {
    return
  }

  errorMessage.value = ''
  draft.value = ''
  appendMessage('user', message)
  const aiMessage = appendMessage('assistant')

  closeStream()
  isStreaming.value = true
  eventSource =
    props.mode === 'love' ? createLoveAppSse(message, chatId.value) : createManusSse(message)

  eventSource.onmessage = (event) => {
    appendChunk(aiMessage, event.data)
  }

  eventSource.onerror = () => {
    closeStream()
    isStreaming.value = false

    if (!aiMessage.content) {
      aiMessage.content = '连接已结束或服务暂不可用，请稍后重试。'
    }

    errorMessage.value = '流式连接已关闭。若回复不完整，请重新发送。'
  }

  eventSource.addEventListener('complete', () => {
    closeStream()
    isStreaming.value = false
  })
}

onBeforeUnmount(() => {
  closeStream()
})
</script>
