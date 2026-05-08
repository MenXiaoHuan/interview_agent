<template>
  <span v-if="iconType === 'font-awesome'" :class="iconValue"></span>
  <span v-else class="unicode-icon">{{ iconValue }}</span>
</template>

<script setup>
import { computed } from 'vue';
import { getSmartIcon } from '@/utils/icons';

const props = defineProps({
  name: {
    type: String,
    required: true
  }
});

const iconData = computed(() => {
  return getSmartIcon(props.name);
});

const iconType = computed(() => iconData.value.type);
const iconValue = computed(() => iconData.value.value);
</script>

<style scoped>
.unicode-icon {
  display: inline-block;
  font-size: 1em;
  line-height: 1;
  vertical-align: middle;
}

/* 为某些特殊图标调整大小 */
.unicode-icon:has(🎤) {
  font-size: 1.2em;
}

.unicode-icon:has(🔴) {
  font-size: 0.8em;
}

.unicode-icon:has(⏰) {
  font-size: 1.1em;
}
</style> 