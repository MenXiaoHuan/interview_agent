<template>
  <div class="fancy-select" :class="{ open: isOpen, disabled, 'eye-care': isEyeCareMode || eyeCareMode }" @click="toggleDropdown" tabindex="0" @blur="closeDropdown">
    <div class="fancy-select__selected">
      <span>{{ selectedLabel || placeholder }}</span>
      <svg class="arrow" width="20" height="20" viewBox="0 0 24 24"><path d="M6 9l6 6 6-6" stroke="#00e5ff" stroke-width="2" fill="none"/></svg>
    </div>
    <ul v-if="isOpen" class="fancy-select__dropdown">
      <li v-for="option in options" :key="option.id" :class="{ selected: option.id === modelValue }"
          @click.stop="select(option)">
        {{ option.name }}
      </li>
    </ul>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';

const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);
const props = defineProps({
  modelValue: [String, Number],
  options: Array,
  placeholder: String,
  disabled: Boolean,
  eyeCareMode: Boolean
});
const emit = defineEmits(['update:modelValue']);
const isOpen = ref(false);
const selectedLabel = computed(() => {
  const found = props.options?.find(opt => opt.id === props.modelValue);
  return found ? found.name : '';
});
function toggleDropdown() {
  if (props.disabled) return;
  isOpen.value = !isOpen.value;
}
function closeDropdown() {
  isOpen.value = false;
}
function select(option) {
  emit('update:modelValue', option.id);
  isOpen.value = false;
}
</script>

<style scoped>
.fancy-select {
  position: relative;
  width: 100%;
  user-select: none;
  outline: none;
}
.fancy-select__selected {
  background: rgba(30, 40, 70, 0.92);
  border: 2px solid #7c4dff;
  border-radius: 20px;
  color: #eaf2ff;
  font-weight: 600;
  padding: 14px 48px 14px 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 4px 24px #7c4dff22, 0 1.5px 6px #00e5ff22;
  transition: border 0.3s, box-shadow 0.3s, background 0.3s;
}
.fancy-select.open .fancy-select__selected,
.fancy-select__selected:hover {
  border-color: #00e5ff;
  box-shadow: 0 0 0 4px #00e5ff44, 0 4px 24px #7c4dff44;
  background: rgba(30, 40, 90, 0.98);
}
.fancy-select__selected .arrow {
  margin-left: 12px;
  transition: transform 0.3s;
}
.fancy-select.open .arrow {
  transform: rotate(180deg);
}

/* 下拉浮层动画和美化 */
.fancy-select__dropdown {
  position: absolute;
  left: 0; right: 0; top: 110%;
  background: rgba(35, 42, 74, 0.98);
  border-radius: 18px;
  box-shadow: 0 8px 32px 0 rgba(124,77,255,0.18), 0 2px 12px rgba(0,229,255,0.10);
  z-index: 10;
  margin: 0;
  padding: 10px 0;
  list-style: none;
  max-height: 240px;
  overflow-y: auto;
  animation: fadeIn 0.22s;
  transition: opacity 0.18s, transform 0.18s;
  opacity: 1;
  transform: translateY(0);
  scrollbar-width: thin;
  scrollbar-color: #7c4dff #232a4a;
}
.fancy-select__dropdown::-webkit-scrollbar {
  width: 6px;
  background: transparent;
}
.fancy-select__dropdown::-webkit-scrollbar-thumb {
  background: #7c4dff55;
  border-radius: 6px;
}

/* 单个选项优化 */
.fancy-select__dropdown li {
  color: #eaf2ff;
  cursor: pointer;
  border-radius: 12px;
  transition: background 0.18s, color 0.18s, box-shadow 0.18s;
  min-height: 44px;
  display: flex;
  align-items: center;
  width: 96%;
  margin: 0 auto 4px auto;
  font-size: 1.08rem;
  padding: 0 24px;
  line-height: 1.7;
  letter-spacing: 0.02em;
  font-weight: 500;
  background: transparent;
  box-sizing: border-box;
}
.fancy-select__dropdown li.selected,
.fancy-select__dropdown li:hover {
  background: linear-gradient(90deg, #7c4dff 0%, #00e5ff 100%);
  color: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px 0 rgba(30,207,255,0.10);
  font-weight: 600;
}

.fancy-select.disabled .fancy-select__selected {
  opacity: 0.5;
  cursor: not-allowed;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(-8px);}
  to   { opacity: 1; transform: translateY(0);}
}

/* 护眼模式样式 */
.eye-care.fancy-select__selected,
.eye-care-mode .fancy-select__selected {
  background: rgba(44, 60, 46, 0.85);
  border-color: #7fb069;
  color: #dbe8d9;
  box-shadow: 0 4px 24px rgba(127, 176, 105, 0.15), 0 1.5px 6px rgba(144, 198, 149, 0.1);
}

.eye-care.fancy-select.open .fancy-select__selected,
.eye-care.fancy-select__selected:hover,
.eye-care-mode .fancy-select.open .fancy-select__selected,
.eye-care-mode .fancy-select__selected:hover {
  border-color: #90c695;
  box-shadow: 0 0 0 4px rgba(144, 198, 149, 0.3), 0 4px 24px rgba(127, 176, 105, 0.25);
  background: rgba(44, 60, 46, 0.95);
}

.eye-care.fancy-select__selected .arrow,
.eye-care-mode .fancy-select__selected .arrow {
  stroke: #7fb069;
}

.eye-care.fancy-select__dropdown,
.eye-care-mode .fancy-select__dropdown {
  background: rgba(44, 60, 46, 0.95);
  box-shadow: 0 8px 32px 0 rgba(127, 176, 105, 0.18), 0 2px 12px rgba(144, 198, 149, 0.1);
  scrollbar-color: #7fb069 #2a3c2e;
}

.eye-care.fancy-select__dropdown::-webkit-scrollbar-thumb,
.eye-care-mode .fancy-select__dropdown::-webkit-scrollbar-thumb {
  background: rgba(127, 176, 105, 0.3);
}

.eye-care.fancy-select__dropdown li,
.eye-care-mode .fancy-select__dropdown li {
  color: #dbe8d9;
}

.eye-care.fancy-select__dropdown li.selected,
.eye-care.fancy-select__dropdown li:hover,
.eye-care-mode .fancy-select__dropdown li.selected,
.eye-care-mode .fancy-select__dropdown li:hover {
  background: linear-gradient(90deg, #7fb069 0%, #90c695 100%);
  color: #fff;
  box-shadow: 0 2px 8px 0 rgba(127, 176, 105, 0.2);
}
</style> 