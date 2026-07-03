<template>
  <div class="landing-container">
    

  <section class="hero-slider" :class="{ ready: sliderReady }">
    <div v-for="(src,i) in slides" :key="i" class="slide" :style="{ backgroundImage: `url(${src})` }" :class="{ active: i === slideIndex }"></div>
    <div id="stars"></div>
    <div id="stars2"></div>
    <div id="stars3"></div>
    <div class="hero-label"><span>功能界面展示</span></div>
    
    <div class="scroll-hint" @click="scrollNext" aria-label="下滑查看更多">
      <svg class="scroll-hint-ico" width="52" height="52" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M8 10l4 4 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        <path d="M8 14l4 4 4-4" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </div>
    <div class="scroll-hint-text">下滑查看更多</div>
  </section>

    <section class="content-page">
      <div class="stars"></div>
      <div class="stars2"></div>
      <div class="stars3"></div>
      <div class="content-center">
        <div class="content-title">
          <h1 class="hero-title">全新版本 · 智能面试</h1>
          <p class="hero-sub">多维度能力评估 · 实战场景还原 · 数据报告驱动提升</p>
        </div>
        <div class="content-cta">
          <router-link to="/pages/login/index" class="cta-primary" @mouseenter="startHoverFlow" @mouseleave="cancelHoverFlow" :style="ctaStyle">登录体验</router-link>
        </div>
        <div class="platforms-block">
          <div class="platforms-row">
            <div class="plat">
              <svg class="plat-ico" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="3" y="5" width="9" height="7" fill="currentColor"/>
                <rect x="14" y="6" width="7" height="6" fill="currentColor"/>
                <rect x="3" y="13" width="9" height="7" fill="currentColor"/>
                <rect x="14" y="14" width="7" height="6" fill="currentColor"/>
              </svg>
              <span>Windows</span>
            </div>
            <div class="plat">
              <svg class="plat-ico" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M16.2 2.1c-.9 0-1.8.5-2.4 1.2-.6.7-1.2 1.7-1 2.7 1-.1 1.9-.7 2.6-1.5.8-.9 1.3-1.9 1-2.4Z" fill="currentColor"/>
                <path d="M19.6 15.6c-.3.7-.7 1.4-1.2 2.1-1 1.3-2 2.6-3.6 2.6-1.4 0-1.9-.9-3.4-.9s-2 .9-3.4.9c-1.6 0-2.6-1.4-3.6-2.8-1.2-1.8-2.3-4.5-1-6.6.8-1.2 2.2-2 3.6-2.1 1.7-.2 3.3.9 4.1.9.8 0 2.5-1.1 4.1-1 1.4 0 2.7.7 3.5 1.9-2 .1-3.6 2.2-3.5 4.3.1 1.8 1.1 3.2 2.6 3.6Z" fill="currentColor"/>
              </svg>
              <span>macOS</span>
            </div>
          </div>
          <div class="platforms-copy">Copyright © 2026 梦之队 · All Rights Reserved</div>
        </div>
      </div>
    </section>

    
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { SHOWCASE_IMAGE_URLS } from '@/utils/static-showcase-images'

const shuffleSlides = (items) => {
  const list = [...items]
  for (let i = list.length - 1; i > 0; i -= 1) {
    const randomIndex = Math.floor(Math.random() * (i + 1))
    ;[list[i], list[randomIndex]] = [list[randomIndex], list[i]]
  }
  return list
}

const slides = shuffleSlides(SHOWCASE_IMAGE_URLS)
const slideIndex = ref(0)
const sliderReady = ref(false)
let slideTimer = null
const SLIDE_INTERVAL_MS = 6400

const preloadHeroSlides = async (sources) => {
  if (typeof Image === 'undefined') return
  await Promise.allSettled((sources || []).map((src) => new Promise((resolve) => {
    const img = new Image()
    img.decoding = 'async'
    img.loading = 'eager'
    img.fetchPriority = 'high'
    img.src = src
    const finish = () => resolve()
    img.onload = () => {
      if (typeof img.decode === 'function') {
        img.decode().catch(() => {}).finally(finish)
        return
      }
      finish()
    }
    img.onerror = finish
    if (img.complete) {
      if (typeof img.decode === 'function') {
        img.decode().catch(() => {}).finally(finish)
        return
      }
      finish()
    }
  })))
}

let hoverRaf = 0
const gp = ref(0)
let lastTs = 0
let startHoverTs = 0
let scrollHandler = null
let wheelLock = false

function onHeroWheel(e) {
  const dy = e.deltaY || 0
  if (e.cancelable) e.preventDefault()
  if (wheelLock) return
  if (dy > 0) {
    wheelLock = true
    scrollNext()
    setTimeout(() => { wheelLock = false }, 900)
  }
}

function scrollPrev() {
  const target = document.querySelector('.hero-slider')
  if (target && typeof target.scrollIntoView === 'function') target.scrollIntoView({ behavior: 'smooth', block: 'start' })
  else window.scrollTo({ top: 0, behavior: 'smooth' })
}

function onContentWheel(e) {
  const dy = e.deltaY || 0
  if (e.cancelable) e.preventDefault()
  if (wheelLock) return
  if (dy < 0) {
    wheelLock = true
    scrollPrev()
    setTimeout(() => { wheelLock = false }, 900)
  }
}

function startHoverFlow() {
  startHoverTs = performance.now()
  lastTs = startHoverTs
  cancelHoverFlow()
  const tick = (ts) => {
    const dt = (ts - lastTs) / 1000
    const dtClamped = Math.min(0.06, Math.max(0.0, dt))
    lastTs = ts
    const elapsed = ts - startHoverTs
    const min = 14
    const max = 70
    const accel = 1000
    const t = Math.min(1, elapsed / accel)
    const ease = 1 - Math.pow(1 - t, 2)
    const speed = min + (max - min) * ease
    gp.value += speed * dtClamped
    if (gp.value > 300) gp.value -= 300
    hoverRaf = requestAnimationFrame(tick)
  }
  hoverRaf = requestAnimationFrame(tick)
}

function cancelHoverFlow() {
  if (hoverRaf) { cancelAnimationFrame(hoverRaf); hoverRaf = 0 }
  gp.value = 0
}

const scrollNext = () => {
  const target = document.querySelector('.content-page')
  if (target && typeof target.scrollIntoView === 'function') target.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

onMounted(async () => {
  const els = Array.from(document.querySelectorAll('.reveal'))
  const io = new IntersectionObserver((entries) => {
    entries.forEach(e => { if (e.isIntersecting) e.target.classList.add('visible') })
  }, { rootMargin: '0px 0px -10% 0px', threshold: 0.1 })
  els.forEach(el => io.observe(el))

  await preloadHeroSlides(slides)
  sliderReady.value = true
  if (slides.length > 1) {
    slideTimer = setInterval(() => {
      slideIndex.value = (slideIndex.value + 1) % slides.length
    }, SLIDE_INTERVAL_MS)
  }

  scrollHandler = () => {
    const doc = document.documentElement
    const atBottom = (window.innerHeight + window.scrollY + 2) >= (doc.scrollHeight)
    if (atBottom) {
      const el = document.querySelector('.platforms-block')
      if (el) el.classList.add('visible')
    } else {
      const el = document.querySelector('.platforms-block')
      if (el) el.classList.remove('visible')
    }
    const top = window.scrollY
    const vh = window.innerHeight || 1
    const p = Math.max(0, Math.min(1, top / vh))
    const root = document.querySelector('.landing-container')
    if (root) {
      root.style.setProperty('--contentFade', String(p))
      root.style.setProperty('--heroFade', String(1 - p))
      const btnScale = 0.94 + 0.12 * p
      root.style.setProperty('--btnScale', String(btnScale))
    }
  }
  window.addEventListener('scroll', scrollHandler)
  const hero = document.querySelector('.hero-slider')
  if (hero) hero.addEventListener('wheel', onHeroWheel, { passive: false })
  const content = document.querySelector('.content-page')
  if (content) content.addEventListener('wheel', onContentWheel, { passive: false })
  // initial check
  scrollHandler()
})

onUnmounted(() => {
  if (slideTimer) { clearInterval(slideTimer); slideTimer = null }
  cancelHoverFlow()
  if (scrollHandler) { window.removeEventListener('scroll', scrollHandler); scrollHandler = null }
  const hero = document.querySelector('.hero-slider')
  if (hero) hero.removeEventListener('wheel', onHeroWheel)
  const content = document.querySelector('.content-page')
  if (content) content.removeEventListener('wheel', onContentWheel)
})

const ctaStyle = computed(() => ({ '--gp': `${gp.value}%` }))
</script>

<style scoped lang="scss">
@use 'sass:math';
@use 'sass:string';
@function multiple-box-shadow($n) {
  $value: '#{math.random(2560)}px #{math.random(2560)}px #FFF';
  @for $i from 2 through $n { $value: '#{$value}, #{math.random(2560)}px #{math.random(2560)}px #FFF'; }
  @return string.unquote($value);
}
$shadows-small: multiple-box-shadow(1000);
$shadows-medium: multiple-box-shadow(300);
$shadows-large: multiple-box-shadow(150);
#stars, #stars2, #stars3, .stars, .stars2, .stars3 { position: absolute; top: 0; left: 0; right: 0; bottom: 0; width: auto; height: auto; display: block; transform: translateZ(0); background: transparent; pointer-events: none; }
#stars, .stars { z-index: 1; width: 1px; height: 1px; box-shadow: $shadows-small; animation: animStar 150s linear infinite; }
#stars2, .stars2 { z-index: 1; width: 2px; height: 2px; box-shadow: $shadows-medium; animation: animStar 100s linear infinite; }
#stars3, .stars3 { z-index: 1; width: 3px; height: 3px; box-shadow: $shadows-large; animation: animStar 50s linear infinite; }
@keyframes animStar { from { transform: translateY(0px); } to { transform: translateY(-2560px); } }
.landing-container { min-height: 100vh; width: 100%; background: radial-gradient(1200px 700px at 50% 0%, #12203a, #0a1929); color: #eaf2ff; position: relative; overflow-x: hidden; overflow-y: auto; }
.hero-slider { position: relative; min-height: 100vh; width: 100%; overflow: hidden; display: flex; align-items: stretch; }
.hero-slider::after { content: ''; position: absolute; inset: 0; pointer-events: none; background: radial-gradient(800px 500px at 50% 10%, rgba(255,255,255,0.06), rgba(0,0,0,0)); }
.slide {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  opacity: 0;
  transform: scale(1.018);
  will-change: opacity, transform;
  transition: opacity 1.35s cubic-bezier(0.22, 1, 0.36, 1), transform 6.4s ease-out;
  backface-visibility: hidden;
  transform-origin: center center;
}
.slide.active {
  opacity: 1;
  transform: scale(1);
}
.hero-slider:not(.ready) .slide {
  transition: none;
}
.hero-overlay { display: none; }
.content-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; flex-direction: column; text-align: center; background: linear-gradient(135deg, #0a1929 0%, #1a1f35 100%); padding: 40px 20px 56px; box-sizing: border-box; }
.content-center { position: relative; z-index: 2; width: min(100%, 1100px); min-height: calc(100vh - 96px); display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 32px; }
.content-title { position: static; transform: none; max-width: 900px; }
.content-cta { position: static; transform: none; }
.platforms-block { position: static; transform: none; text-align: center; }
.platforms-block { opacity: 0; transform: translateY(16px); transition: opacity .6s ease, transform .6s ease; will-change: opacity, transform; }
.platforms-block.visible { opacity: 1; transform: translateY(0); }
.platforms-inline { position: absolute; left: 50%; bottom: 10%; transform: translateX(-50%); display: flex; flex-direction: column; align-items: center; gap: 8px; }
.scroll-hint { position: absolute; left: 50%; bottom: 8%; transform: translate(-50%, 0); z-index: 2; animation: hintBounce 1.6s ease-in-out infinite; cursor: pointer; }
.scroll-hint-ico { color: #ffffff; filter: drop-shadow(0 0 8px rgba(255,255,255,0.35)); }
@keyframes hintBounce { 0% { transform: translate(-50%, 0); } 50% { transform: translate(-50%, 6px); } 100% { transform: translate(-50%, 0); } }
.scroll-hint-text { position: absolute; left: 50%; bottom: 5%; transform: translate(-50%, 8px); color: #ffffff; font-size: 14px; opacity: 0; transition: opacity .25s ease, transform .25s ease; text-shadow: 0 1px 6px rgba(0,0,0,0.35); }
.hero-slider:hover .scroll-hint-text { opacity: 1; transform: translate(-50%, 0); }
.hero-label { position: absolute; top: 3.8%; left: 3.2%; z-index: 2; padding: 6px 12px; border-radius: 12px; backdrop-filter: blur(6px); background: linear-gradient(135deg, rgba(14,20,34,0.45), rgba(24,30,50,0.35)); border: 1px solid rgba(124,77,255,0.30); box-shadow: 0 8px 24px rgba(0,0,0,0.22), 0 0 12px rgba(124,77,255,0.18); }
.hero-label span { display: inline-block; font-weight: 700; letter-spacing: .5px; font-size: clamp(12px, 1.6vw, 18px); background: linear-gradient(102deg, #4a6fff 0%, #7c4dff 50%, #00c8ff 100%); background-clip: text; -webkit-background-clip: text; color: transparent; -webkit-text-fill-color: transparent; }
.hero { position: relative; padding-top: 120px; padding-bottom: 40px; display: flex; align-items: center; justify-content: center; }
.hero-bg { position: absolute; inset: 0; background: radial-gradient(1200px 700px at 50% 20%, rgba(120,150,255,0.15), rgba(0,0,0,0)); animation: hueShift 12s ease-in-out infinite alternate; filter: blur(50px); }
@keyframes hueShift { from { opacity: .7; } to { opacity: .4; } }
.hero-inner { text-align: center; max-width: 960px; padding: 0 20px; position: relative; z-index: 1; }
.hero-title { font-size: 64px; font-weight: 900; letter-spacing: 1px; background: linear-gradient(102deg, #4a6fff 0%, #7c4dff 48%, #00c8ff 100%); background-clip: text; -webkit-background-clip: text; -webkit-text-fill-color: transparent; -webkit-text-stroke: 0.8px rgba(255,255,255,0.06); text-shadow: 0 6px 28px rgba(106,118,255,0.35), 0 0 24px rgba(124,77,255,0.25), 0 3px 12px rgba(0,229,255,0.18); opacity: var(--contentFade, 0); transform: translateY(calc((1 - var(--contentFade, 0)) * 18px)); will-change: opacity, transform; transition: opacity .25s ease, transform .25s ease; }
.hero-sub { margin-top: 8px; font-size: 18px; color: #a8c6ff; opacity: var(--contentFade, 0); transform: translateY(calc((1 - var(--contentFade, 0)) * 22px)); will-change: opacity, transform; transition: opacity .25s ease, transform .25s ease; }
.hero-cta { margin-top: 18px; }
.cta-primary { display: inline-block; padding: 18px 52px; border-radius: 26px; color: #fff; text-decoration: none; font-size: 17px; background: repeating-linear-gradient(90deg, #ff4fd8 0%, #ff9100 33%, #00e5ff 66%, #ff4fd8 100%); background-size: 300% 100%; background-position: var(--gp, 0%) 50%; box-shadow: none; position: relative; overflow: hidden; transition: box-shadow .3s ease, transform .18s ease; transform: scale(var(--btnScale, 1)); will-change: transform; }
.cta-primary::before { content: ''; position: absolute; left: -60%; top: 0; width: 60%; height: 100%; background: linear-gradient(120deg, rgba(255,255,255,0.28) 0%, rgba(255,255,255,0.08) 100%); filter: blur(4px); opacity: 0; transition: left .6s ease, opacity .3s ease; pointer-events: none; }
.cta-primary::after { content: ''; position: absolute; inset: -30%; border-radius: 999px; background: radial-gradient(closest-side, rgba(255,170,64,0.35), rgba(255,170,64,0.0)); filter: blur(18px); opacity: 0; transition: opacity .3s ease; pointer-events: none; }
.cta-primary:hover { box-shadow: 0 0 24px rgba(255,77,216,0.55), 0 0 48px rgba(255,145,0,0.45); transform: scale(calc(var(--btnScale, 1) * 1.02)); }
.cta-primary:hover::before { animation: sheenSweep 2.4s linear infinite; }
@keyframes sheenSweep { 0% { left: -60%; } 100% { left: 110%; } }
.cta-primary { will-change: background-position; }
.cta-primary { will-change: background-position; }
.cta-primary:hover::before { left: 110%; opacity: 1; }
.cta-primary:hover::after { opacity: 1; }
.platforms { margin-top: 22px; display: inline-grid; grid-auto-flow: column; gap: 14px; align-items: center; }
.platform { display: inline-flex; align-items: center; gap: 8px; color: #eaf2ff; padding: 6px 10px; border-radius: 10px; border: 1px solid rgba(124,77,255,0.25); background: rgba(255,255,255,0.06); }
.plat-ico { font-size: 16px; }

.features { display: none; }
.reveal { display: none; }
.platforms-row { max-width: 1040px; margin: 0 auto; display: grid; grid-auto-flow: column; gap: 32px; align-items: end; justify-content: center; }
.plat { display: inline-flex; align-items: center; gap: 0; color: #eaf2ff; }
.plat-ico { width: 28px; height: 28px; display: block; color: #eaf2ff; filter: drop-shadow(0 0 6px rgba(234,242,255,0.25)); }
.plat:hover .plat-ico { filter: drop-shadow(0 0 10px rgba(234,242,255,0.35)); }
.plat span { font-size: 14px; margin-left: 10px; }
.platforms-copy { margin-top: 12px; text-align: center; color: #c9dcff; font-size: 13px; }
.grid { max-width: 1040px; margin: 0 auto; display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; }
.card { background: linear-gradient(135deg, rgba(22,26,44,0.96), rgba(28,32,58,0.92)); border-radius: 16px; border: 1px solid rgba(106,118,255,0.25); padding: 20px; box-shadow: 0 10px 28px rgba(0,0,0,0.22); display: flex; flex-direction: column; gap: 10px; }
.card-icon { color: #6a76ff; filter: drop-shadow(0 0 6px rgba(106,118,255,0.35)); }
.card-title { font-weight: 800; font-size: 16px; }
.card-desc { color: #c9dcff; font-size: 14px; }
.card:hover { transform: translateY(-4px); box-shadow: 0 16px 36px rgba(0,0,0,0.26); border-color: rgba(124,77,255,0.35); }

.showcase { padding: 24px 20px 40px; }
.showcase-inner { max-width: 1040px; margin: 0 auto; display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.pane { background: linear-gradient(135deg, rgba(20,24,40,0.96), rgba(28,32,58,0.92)); border: 1px solid rgba(124,77,255,0.25); border-radius: 16px; padding: 18px; box-shadow: 0 10px 28px rgba(0,0,0,0.24); }
.pane-title { font-weight: 800; margin-bottom: 6px; }
.pane-desc { color: #c9dcff; font-size: 14px; }

.section-title { max-width: 1040px; margin: 0 auto 12px; display: flex; align-items: center; gap: 8px; color: #eaf2ff; font-weight: 800; }
.section-ico { color: #6a76ff; }
.flow { padding: 8px 20px 18px; }
.flow-grid { max-width: 1040px; margin: 0 auto; display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px; }
.step { display: flex; align-items: center; gap: 10px; padding: 12px 14px; border-radius: 12px; border: 1px solid rgba(106,118,255,0.25); background: rgba(255,255,255,0.06); }
.step-icon { color: #6a76ff; filter: drop-shadow(0 0 6px rgba(106,118,255,0.35)); }
.step-title { font-weight: 700; color: #eaf2ff; }

.site-footer { border-top: 1px solid rgba(124,77,255,0.18); padding: 18px 20px; backdrop-filter: blur(6px); background: linear-gradient(180deg, rgba(10,16,26,0.35), rgba(10,16,26,0.2)); }
.footer-inner { max-width: 1040px; margin: 0 auto; display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; align-items: start; }
.footer-title { font-weight: 800; margin-bottom: 6px; }
.footer-text { color: #c9dcff; font-size: 14px; }
.footer-link { display: inline-flex; align-items: center; gap: 8px; color: #a8c6ff; text-decoration: none; border: 1px solid rgba(124,77,255,0.25); border-radius: 12px; padding: 6px 12px; }

@media (max-width: 1100px) { .grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 700px) {
  .grid { grid-template-columns: 1fr; }
  .hero-title { font-size: 42px; }
  .site-header { grid-template-columns: auto auto auto; }
  .site-nav { display: none; }
  .content-page { padding: 32px 16px 44px; }
  .content-center { min-height: auto; gap: 24px; }
  .platforms-row { grid-auto-flow: row; gap: 18px; }
}
@media (max-width: 500px) {
  .hero-slider { min-height: 88vh; }
  .hero-title { font-size: 34px; }
  .hero-sub { font-size: 15px; }
  .cta-primary { padding: 15px 34px; font-size: 15px; }
  .hero-label { top: 18px; left: 14px; }
  .scroll-hint { bottom: 10%; }
  .scroll-hint-text { bottom: 6%; }
}
</style>
