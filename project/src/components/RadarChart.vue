<template>
  <div ref="chartRef" :style="{ width: width, height: height }"></div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  indicators: Array, // [{ name: '流畅度', max: 100 }, ...]
  values: Array,     // [80, 90, 70, ...]
  width: { type: String, default: '360px' },
  height: { type: String, default: '320px' }
})

const chartRef = ref(null)
let chartInstance = null

function renderChart() {
  if (!chartRef.value) return
  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value)
  }
  chartInstance.setOption({
    radar: {
      indicator: props.indicators,
      radius: '55%', // 进一步减小半径，为文字留出更多空间
      splitNumber: 5,
      axisName: {
        color: '#00ffff', 
        fontSize: 12, // 进一步减小字体大小
        padding: [0, 5, 0, 5] // 添加内边距，避免文字重叠
      },
      splitLine: { lineStyle: { color: '#00ffff44' } },
      splitArea: { areaStyle: { color: ['#02101e', '#022b3a'] } },
      axisLine: { lineStyle: { color: '#00ffff88' } }
    },
    tooltip: { show: false },
    series: [{
      type: 'radar',
      data: [{ value: props.values, areaStyle: { color: 'rgba(0,255,255,0.2)' }, lineStyle: { color: '#00ffff', width: 5 }, symbol: 'circle', symbolSize: 8 }],
      smooth: true,
      silent: true
    }]
  })
}

onMounted(renderChart)
watch(() => [props.indicators, props.values], renderChart, { deep: true })
</script>

<style scoped>
</style>