<template>
  <transition name="modal-fade">
    <div v-if="isVisible" class="modal-overlay" @click.self="closeModal">
      <div class="modal-container">
        <button class="close-button" @click="closeModal">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M18 6L6 18" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M6 6L18 18" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </button>
        <div class="modal-header">
          <slot name="header"><h3>提示</h3></slot>
        </div>
        <div class="modal-body">
          <slot></slot>
        </div>
        <div class="modal-footer">
          <slot name="footer">
            <button class="primary-button" @click="closeModal">确定</button>
          </slot>
        </div>
      </div>
    </div>
  </transition>
</template>

<script setup>
const props = defineProps({
  isVisible: {
    type: Boolean,
    required: true
  }
})

const emit = defineEmits(['close'])

const closeModal = () => {
  emit('close')
}
</script>

<style scoped>
.modal-fade-enter-active, .modal-fade-leave-active {
  transition: opacity 0.3s cubic-bezier(.4,2,.6,1), transform 0.3s cubic-bezier(.4,2,.6,1);
}
.modal-fade-enter-from, .modal-fade-leave-to {
  opacity: 0;
  transform: scale(0.92);
}

.modal-overlay {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(10, 25, 41, 0.88);
  display: flex; justify-content: center; align-items: center;
  z-index: 1000;
  backdrop-filter: blur(3px);
}

.modal-container {
  background: linear-gradient(135deg, #181e36 0%, #232a4a 100%);
  border-radius: 32px;
  box-shadow:
    0 0 0 3px #7c4dff44,
    0 0 32px 8px #00e5ff33,
    0 8px 40px 0 #7c4dff44;
  border: 2.5px solid rgba(124,77,255,0.22);
  backdrop-filter: blur(24px);
  width: 95%; max-width: 480px; max-height: 90vh;
  display: flex; flex-direction: column; gap: 18px;
  overflow: hidden; position: relative;
  animation: modal-pop 0.4s cubic-bezier(.4,2,.6,1);
  padding: 36px 0 32px 0;
}

@keyframes modal-pop {
  0% { opacity: 0; transform: scale(0.92);}
  100% { opacity: 1; transform: scale(1);}
}

.modal-header {
  display: flex; align-items: center; justify-content: center;
  padding-bottom: 10px;
  border-bottom: 1.5px solid #2a3350;
}
.modal-header h3 {
  margin: 0; font-size: 24px; color: #eaf2ff;
  font-weight: 800; letter-spacing: 2px;
  text-shadow: 0 0 12px #7c4dff99, 0 0 2px #00e5ff66;
  text-align: center;
  flex: 1;
}

.close-button {
  position: absolute; top: 18px; right: 18px;
  background: none; border: none; cursor: pointer;
  color: #a8c6ff; transition: all 0.2s;
  padding: 6px; border-radius: 50%; line-height: 0; z-index: 1002;
  font-size: 26px;
  box-shadow: 0 2px 8px #232a4a44;
}
.close-button:hover {
  color: #00e5ff; background: rgba(124,77,255,0.12);
  box-shadow: 0 0 16px #7c4dffcc, 0 2px 8px #232a4a44;
}

.modal-body {
  flex-grow: 1; overflow-y: auto; color: #eaf2ff;
  font-size: 18px; line-height: 2; text-align: center;
  margin: 0; padding: 24px 18px 24px 18px;
  scrollbar-width: none; -ms-overflow-style: none;
}
.modal-body::-webkit-scrollbar { display: none; }

.modal-footer {
  display: flex; justify-content: center; padding-top: 10px;
  border-top: 1.5px solid #2a3350;
}

.primary-button {
  padding: 0 48px; height: 48px;
  background: #4A6FFF;
  color: #fff; border: none; border-radius: 24px; cursor: pointer;
  font-size: 20px; font-weight: bold; letter-spacing: 3px;
  box-shadow: 0 2px 8px #7c4dff22;
  position: relative; overflow: hidden;
  transition: box-shadow 0.3s, background 0.2s;
  display: flex; align-items: center; justify-content: center;
}
.primary-button:hover {
  background: #3A5FEF;
  box-shadow: 0 4px 16px #4A6FFF44;
}
</style>

export interface ModalProps {
  isVisible: boolean;
} 