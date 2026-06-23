<template>
  <main class="home-page">
    <section class="home-shell">
      <div class="home-copy">
        <p class="eyebrow">AI Master Agent</p>
        <h1>选择一个应用开始对话</h1>
        <p class="home-subtitle">
          两个应用都支持实时流式回复。恋爱大师保留独立聊天室 ID，超级智能体适合处理更复杂的任务。
        </p>
      </div>

      <div class="app-grid" aria-label="应用列表">
        <RouterLink class="app-card" to="/love">
          <div class="app-card-icon theme-love">
            <HeartHandshake :size="28" />
          </div>
          <div>
            <h2>AI 恋爱大师</h2>
            <p>面向情感关系、沟通建议和聊天表达的实时咨询。</p>
          </div>
          <ArrowRight :size="22" class="app-card-arrow" />
        </RouterLink>

        <RouterLink class="app-card" to="/manus">
          <div class="app-card-icon theme-manus">
            <Sparkles :size="28" />
          </div>
          <div>
            <h2>AI 超级智能体</h2>
            <p>面向多步骤目标、资料处理和任务拆解的智能体协作。</p>
          </div>
          <ArrowRight :size="22" class="app-card-arrow" />
        </RouterLink>
      </div>
    </section>
  </main>
</template>

<script setup>
import { RouterLink } from 'vue-router'
import { ArrowRight, HeartHandshake, Sparkles } from '@lucide/vue'
</script>
