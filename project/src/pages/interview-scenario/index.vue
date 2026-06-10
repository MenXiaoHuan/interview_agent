<template>
  <div class="audio-evaluation-bg" :class="{ 'eye-care': isEyeCareMode }">
    <!-- 星空背景效果 -->
    <div id="stars"></div>
    <div id="stars2"></div>
    <div id="stars3"></div>
    
    <!-- 返回按钮 -->
    <div class="back-button" @click="goBack">
      <svg width="24" height="24" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M15 18L9 12L15 6" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
      </svg>
    </div>
    
    <div class="audio-evaluation-card fade-in">
      <template v-if="!hasStarted">
        <h1 class="main-title">场景评测</h1>
        <div class="subtitle">评测说明</div>
        <div class="desc-block">
          <p class="desc">系统将对您的口语作答进行多维分析，重点包括：</p>
          <ul class="desc-list">
            <li>语音转写与表达分析（流畅度、停顿、用词）</li>
            <li>问题相关性与逻辑组织（是否围绕题意、结构清晰）</li>
            <li>沟通表达与即兴应对（表达清楚、应变能力）</li>
          </ul>
        </div>
        <template v-if="interviewQuestions.length > 0">
          <button @click="startAssessment" class="control-button primary big">开始评测</button>
        </template>
        <template v-else>
          <div class="empty-tip">
            <p class="empty-text">暂无题目，请点击下方按钮刷新后重试</p>
            <button @click="refreshQuestions" class="control-button primary big">刷新题目</button>
          </div>
        </template>
      </template>
      <template v-else>
        <div v-if="!isCompleted && !analysisResults" class="assessment-screen">
          <div class="question-progress">
            问题 {{ currentQuestionIndex + 1 }}/{{ interviewQuestions.length }}
          </div>
          <div
            v-if="showWelcomeText"
            class="welcome-text-container fade-in"
            :class="{'is-speaking': showWelcomeText}"
          >
            <div class="welcome-text">{{ interviewQuestions[currentQuestionIndex] }}</div>
          </div>

          <div v-if="!showWelcomeText" class="desc-block fade-in">
            <p class="tips-main">请确保麦克风正常，点击下方按钮开始录制音频作答。</p>
            <div class="question-text-container">
              <h4 class="current-question-title">当前问题：</h4>
              <p class="current-question-content">{{ interviewQuestions[currentQuestionIndex] }}</p>
            </div>
          </div>

          <div class="audio-wave-container" v-if="isRecording">
            <svg class="audio-wave" width="180" height="40" viewBox="0 0 180 40">
              <defs>
                <linearGradient id="waveGradient" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="#7c4dff"/>
                  <stop offset="100%" stop-color="#00e5ff"/>
                </linearGradient>
              </defs>
              <rect v-for="n in 7"
                :key="n"
                :x="20 + (n-1)*20"
                y="10"
                width="8"
                :height="20"
                rx="4"
                :style="`animation-delay: ${n*0.18}s;`"
                class="audio-bar"
              />
            </svg>
          </div>

          

          <div class="button-block" style="display: flex; gap: 24px;">
            <button
              v-if="!isRecording && !showWelcomeText"
              @click="startRecording"
              :disabled="!isCameraReady || isPlayingWelcome"
              class="control-button record-btn big"
            >
              <svg class="btn-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M12 16a4 4 0 0 0 4-4V7a4 4 0 1 0-8 0v5a4 4 0 0 0 4 4Z" stroke="currentColor" stroke-width="2"/>
                <path d="M7 11v1a5 5 0 0 0 10 0v-1" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                <path d="M12 21v-3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              开始录制
            </button>
            <button
              v-else-if="isRecording"
              @click="stopRecording"
              :disabled="isUploading"
              class="control-button stop big"
            >
              <svg class="btn-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <rect x="6" y="6" width="12" height="12" rx="2" stroke="currentColor" stroke-width="2"/>
              </svg>
              停止录制
            </button>
          </div>

          <div v-if="isUploading" class="loading-message">
            <div class="spinner"></div>
            <p v-if="uploadStep === 'transcribing'">正在进行语音转录...</p>
            <p v-else-if="uploadStep === 'assessing'">正在分析评估音频...</p>
            <p v-else>正在上传音频并进行分析...</p>
          </div>
          <div v-if="uploadError" class="error-message">
            <div class="error-content">
              <span>上传或分析失败: {{ uploadError }}</span>
            </div>
          </div>

          
        </div>

        <div v-if="analysisResults" class="analysis-section">
          <h3>评测结果</h3>
          <div class="overall-score">
            总分：<span>{{ analysisResults.logicScore }}</span>
          </div>

          <div class="feedback-section">
            

            <!-- 能力分析（进度条） -->
            <div class="result-item">
              <h4>能力分析</h4>
              <div v-for="(ability, idx) in analysisResults.abilities" :key="ability.name" class="ability-progress-block">
                <div class="ability-label-row">
                  <span class="ability-name">{{ ability.name }}</span>
                  <span class="ability-score">{{ ability.score }}分</span>
                </div>
                <div class="progress-bar-outer">
                  <div class="progress-bar-inner" :style="{ width: (Math.min(20, Math.max(0, ability.score)) * 5) + '%'}"></div>
                </div>
                <div class="ability-desc">{{ ability.desc }}</div>
              </div>
            </div>

            <!-- 改进建议（每题建议） -->
            <div class="suggestion-box">
              <div class="suggestion-header">
                <svg class="suggestion-icon" width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M12 2a7 7 0 0 1 7 7c0 2.5-1.3 4.7-3.2 6l-.8.6V18a2 2 0 0 1-2 2h-1v1a1 1 0 0 1-1 1h-1a1 1 0 0 1-1-1v-1H8a2 2 0 0 1-2-2v-2.4l-.8-.6A7 7 0 0 1 5 9a7 7 0 0 1 7-7Z" stroke="currentColor" stroke-width="1.6"/>
                </svg>
                <span class="suggestion-title">改进建议</span>
              </div>
              <ul class="suggestion-list enhanced">
                <li class="suggestion-item" v-for="(suggestion, idx) in analysisResults.questionSuggestions" :key="idx">{{ suggestion }}</li>
              </ul>
            </div>

            <!-- 发音/表情建议模块已移除 -->
          </div>

          <div class="action-buttons">
            <button @click="handleContinueInterview" class="control-button primary big">继续面试</button>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, watch, nextTick } from 'vue';
import { useUserStore } from '@/stores/user';
import { storeToRefs } from 'pinia';
import { useRouter, useRoute } from 'vue-router';
import { analyzeScenarioInterview, getJobInfo, saveScenarioHistory, getScenarioQuestions } from '@/utils/api';
import { getUserSession } from '@/utils/user-session';
import { BASE_URL } from '@/utils/api';
import { getAudioProcessorWorkletUrl } from '@/utils/audio-processor';


const userStore = useUserStore();
const { isEyeCareMode } = storeToRefs(userStore);

const hasStarted = ref(false);
const isCompleted = ref(false);
const mediaRecorder = ref(null);
const recordedChunks = ref([]);
const isRecording = ref(false);
const isCameraReady = ref(false);
const cameraError = ref(null);
const isUploading = ref(false);
const uploadError = ref(null);
const analysisResults = ref(null);
const eventChannel = ref(null);
const uploadStep = ref(null);
const isPlayingWelcome = ref(false);
const showWelcomeText = ref(false);
const currentQuestionIndex = ref(0);
const answers = ref({});

const router = useRouter();
const route = useRoute();
const isFromAiInterview = computed(() => route.query.from === 'ai-interview');
const getAiSessionId = () => {
  const routeSessionId = String(route.query.chatId || '').trim();
  if (routeSessionId) {
    return routeSessionId;
  }
  try {
    return String(sessionStorage.getItem('activeAiConversationChatId') || '').trim();
  } catch (_) {
    return '';
  }
};
const buildInterviewAiReturnUrl = ({ completedType = '', mode = '', score = 0, timestamp = '' } = {}) => {
  const query = [];
  query.push('autoOpen=1');
  const safeSessionId = getAiSessionId();
  const safeCompletedType = String(completedType || '').trim();
  const safeMode = String(mode || '').trim();
  const hasExplicitScore = score !== undefined && score !== null && String(score).trim() !== '';
  const safeScore = Math.max(0, Number(score) || 0);
  const safeTimestamp = String(timestamp || '').trim();
  if (safeSessionId) {
    query.push(`chatId=${encodeURIComponent(safeSessionId)}`);
  }
  if (safeCompletedType) {
    query.push(`completedType=${encodeURIComponent(safeCompletedType)}`);
  }
  if (safeMode) {
    query.push(`mode=${encodeURIComponent(safeMode)}`);
  }
  if (hasExplicitScore) {
    query.push(`score=${encodeURIComponent(safeScore)}`);
  }
  if (safeTimestamp) {
    query.push(`timestamp=${encodeURIComponent(safeTimestamp)}`);
  }
  return `/pages/interview-ai/index${query.length ? `?${query.join('&')}` : ''}`;
};

const interviewQuestions = ref([]);
const welcomeText = ref('');

// 从API获取场景问题并随机选择5题
const loadScenarioQuestions = async () => {
  try {
    const jobId = uni.getStorageSync('currentJobId') || 1;
    const response = await getScenarioQuestions(jobId);
    
    if (response.code === 200 && response.data && response.data.length > 0) {
      // 随机选择5题
      const allQuestions = response.data;
      const shuffled = allQuestions.sort(() => 0.5 - Math.random());
      const selectedQuestions = shuffled.slice(0, 5);
      
      interviewQuestions.value = selectedQuestions.map(q => q.question);
      welcomeText.value = interviewQuestions.value[0];
    } else {
      interviewQuestions.value = [];
      welcomeText.value = '';
    }
  } catch (error) {
    console.error('获取场景问题失败:', error);
    interviewQuestions.value = [];
    welcomeText.value = '';
  }
};

 

onMounted(async () => {
  try {
    // 加载场景问题
    await loadScenarioQuestions();
    
    const pages = getCurrentPages();
    const currentPage = pages[pages.length - 1];
    if (currentPage && currentPage.$page && currentPage.$page.openType === 'navigateTo') {
      try {
        eventChannel.value = currentPage.getOpenerEventChannel();
        
        eventChannel.value.on('acceptDataFromOpener', function(data) {
          if (data.completed && data.score) {
            isCompleted.value = true;
            hasStarted.value = true;
            analysisResults.value = {
              logicScore: data.score,
              transcribedText: "已完成的音频评测",
              speedAnnotations: "正常",
              logicSuggestions: ["评测已完成"],
              benchmark: {
                averageSpeed: "120",
                averageLogicScore: "85"
              }
            };
          }
        });
      } catch (err) {
        console.error('Error setting up event channel:', err);
        cameraError.value = err.message || '无法设置事件通道';
      }
    }
  } catch (err) {
    console.error('Error during page load:', err);
    cameraError.value = err.message || '初始化失败';
  }
});

const startAssessment = async () => {
  try {
    if (!interviewQuestions.value.length) {
      uni.showToast({ title: '暂无题目，请刷新后重试', icon: 'none', duration: 2000 });
      return;
    }
    hasStarted.value = true;
    showWelcomeText.value = true;
    console.log('开始播放欢迎语音...');
    await playQuestion();
    
    console.log('欢迎语音播放完成，等待1秒...');
    await new Promise(resolve => setTimeout(resolve, 1000));
    
    console.log('开始设置麦克风...');
    await setupMicrophone();
  } catch (err) {
    console.error('Error setting up microphone:', err);
    cameraError.value = err.message || '麦克风初始化失败';
  }
};

const refreshQuestions = async () => {
  await loadScenarioQuestions();
  const msg = interviewQuestions.value.length ? '题目已刷新' : '暂无题目，请稍后重试';
  uni.showToast({ title: msg, icon: interviewQuestions.value.length ? 'success' : 'none', duration: 1800 });
};

const playQuestion = async () => {
  try {
    isPlayingWelcome.value = true;
    const token = uni.getStorageSync('token');
    if (!token) {
      throw new Error('请先登录');
    }

    console.log('开始请求语音合成...');
    const response = await fetch(`${BASE_URL}/api/speech/synthesize`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify({
        text: interviewQuestions.value[currentQuestionIndex.value],
        vcn: 'x4_yezi',
        pitch: 50,
        speed: 50,
        tte: 'UTF8'
      })
    });

    if (!response.ok) {
      const errorText = await response.text();
      console.error('语音合成请求失败:', response.status, errorText);
      throw new Error(`语音合成失败: ${response.status} ${errorText}`);
    }

    // 处理JSON响应
    const contentType = response.headers.get('content-type');
    console.log('响应Content-Type:', contentType);

    // 检查是否为JSON响应
    if (!contentType || !contentType.includes('application/json')) {
      const errorText = await response.text();
      console.error('服务器返回了非JSON数据:', errorText);
      throw new Error('服务器返回了非预期的数据格式');
    }

    // 解析JSON响应
    const jsonResponse = await response.json();
    console.log('API响应:', jsonResponse);

    // 检查API响应状态
    if (jsonResponse.code !== 200) {
      console.error('API返回错误:', jsonResponse.message);
      throw new Error(jsonResponse.message || '语音合成失败');
    }

    // 从响应中获取Base64音频数据
    const base64Audio = jsonResponse.data;
    if (!base64Audio) {
      throw new Error('API响应中缺少音频数据');
    }

    // 解码Base64为二进制数据
    const binaryString = atob(base64Audio);
    const arrayBuffer = new ArrayBuffer(binaryString.length);
    const uint8Array = new Uint8Array(arrayBuffer);

    for (let i = 0; i < binaryString.length; i++) {
      uint8Array[i] = binaryString.charCodeAt(i);
    }

    console.log('PCM数据大小:', arrayBuffer.byteLength);

    if (arrayBuffer.byteLength === 0) {
      throw new Error('服务器返回了空的音频数据');
    }

    // 转换PCM为WAV格式
    const wavBlob = pcmToWav(new Int16Array(arrayBuffer), {
      sampleRate: 16000,
      numChannels: 1,
      bitDepth: 16
    });
    console.log('WAV数据大小:', wavBlob.size);

    if (wavBlob.size <= 44) {
      throw new Error('生成的WAV文件没有音频数据');
    }

    return new Promise((resolve, reject) => {
      const audioUrl = URL.createObjectURL(wavBlob);
      console.log('创建音频URL:', audioUrl);

      const audio = new Audio();
      audio.volume = 1.0;

      audio.oncanplaythrough = () => {
        console.log('音频可以播放');
        const playPromise = audio.play();
        if (playPromise !== undefined) {
          playPromise.then(() => {
            console.log('开始播放音频');
          }).catch(error => {
            if (error.name !== 'AbortError') {
              console.error('播放失败:', error);
              reject(error);
            }
          });
        }
      };

      audio.onended = () => {
        console.log('音频播放结束');
        URL.revokeObjectURL(audioUrl);
        setTimeout(() => {
          showWelcomeText.value = false;
        }, 500);
        resolve();
      };

      audio.onerror = (error) => {
        console.error('音频错误:', error);
        URL.revokeObjectURL(audioUrl);
        reject(new Error('音频播放失败'));
      };

      audio.src = audioUrl;
      audio.load();
    });

  } catch (error) {
    console.error('播放问题失败:', error);
    uni.showToast({
      title: error.message || '播放问题失败',
      icon: 'none',
      duration: 2000
    });
    showWelcomeText.value = false;
    throw error;
  } finally {
    isPlayingWelcome.value = false;
  }
};

const setupMicrophone = async () => {
  try {
    const stream = await navigator.mediaDevices.getUserMedia({
      audio: {
        sampleRate: 16000,
        channelCount: 1,
        echoCancellation: true,
        noiseSuppression: true
      }
    });
    isCameraReady.value = true;
    
    const audioContext = new AudioContext({
      sampleRate: 16000
    });

    await audioContext.audioWorklet.addModule(getAudioProcessorWorkletUrl());
    const source = audioContext.createMediaStreamSource(stream);
    
    const processor = new AudioWorkletNode(audioContext, 'audio-processor');
    
    const audioChunks = [];
    let isRecording = false;
    
    processor.port.onmessage = (event) => {
      if (isRecording && event.data.audioData) {
        const inputData = event.data.audioData;
        const pcmData = new Int16Array(inputData.length);
        for (let i = 0; i < inputData.length; i++) {
          pcmData[i] = Math.max(-32768, Math.min(32767, Math.round(inputData[i] * 32767)));
        }
        audioChunks.push(pcmData);
      }
    };
    
    source.connect(processor);
    processor.connect(audioContext.destination);
    
    mediaRecorder.value = {
      stream,
      audioContext,
      processor,
      source,
      audioChunks,
      start: () => {
        audioChunks.length = 0;
        isRecording = true;
      },
      stop: () => {
        isRecording = false;
        
        const totalLength = audioChunks.reduce((acc, chunk) => acc + chunk.length, 0);
        const mergedPCM = new Int16Array(totalLength);
        let offset = 0;
        for (const chunk of audioChunks) {
          mergedPCM.set(chunk, offset);
          offset += chunk.length;
        }
        
        const wavBlob = pcmToWav(mergedPCM, {
          sampleRate: 16000,
          numChannels: 1,
          bitDepth: 16
        });
        
        const reader = new FileReader();
        reader.readAsDataURL(wavBlob);
        reader.onloadend = () => {
          const base64data = reader.result.split(',')[1];
          uploadAudio(base64data);
        };
      }
    };

    return true;
  } catch (err) {
    console.error('Error accessing microphone:', err);
    cameraError.value = '无法访问麦克风，请确保已授予权限';
    return false;
  }
};

const pcmToWav = (pcmData, options) => {
  const numChannels = options.numChannels || 1;
  const sampleRate = options.sampleRate || 16000;
  const bitDepth = options.bitDepth || 16;
  const bytesPerSample = bitDepth / 8;
  const blockAlign = numChannels * bytesPerSample;

  const buffer = new ArrayBuffer(44 + pcmData.length * bytesPerSample);
  const view = new DataView(buffer);

  writeString(view, 0, 'RIFF');
  view.setUint32(4, 36 + pcmData.length * bytesPerSample, true);
  writeString(view, 8, 'WAVE');
  writeString(view, 12, 'fmt ');
  view.setUint32(16, 16, true);
  view.setUint16(20, 1, true);
  view.setUint16(22, numChannels, true);
  view.setUint32(24, sampleRate, true);
  view.setUint32(28, sampleRate * blockAlign, true);
  view.setUint16(32, blockAlign, true);
  view.setUint16(34, bitDepth, true);
  writeString(view, 36, 'data');
  view.setUint32(40, pcmData.length * bytesPerSample, true);

  const offset = 44;
  if (bitDepth === 16) {
    for (let i = 0; i < pcmData.length; i++) {
      view.setInt16(offset + i * 2, pcmData[i], true);
    }
  }

  return new Blob([buffer], { type: 'audio/wav' });
};

const writeString = (view, offset, string) => {
  for (let i = 0; i < string.length; i++) {
    view.setUint8(offset + i, string.charCodeAt(i));
  }
};

const startRecording = () => {
  if (!mediaRecorder.value || !isCameraReady.value) {
    cameraError.value = '麦克风未就绪';
    return;
  }

  recordedChunks.value = [];
  isUploading.value = false;
  uploadError.value = null;
  analysisResults.value = null;
  
  try {
    mediaRecorder.value.start();
    isRecording.value = true;
  } catch (err) {
    console.error('Error starting recording:', err);
    cameraError.value = err.message || '开始录制失败';
  }
};

const stopRecording = () => {
  if (mediaRecorder.value && isRecording.value) {
    mediaRecorder.value.stop();
    isRecording.value = false;
  }
};

const uploadAudio = async (audioBase64) => {
  if (!audioBase64) {
    uploadError.value = '没有录制的音频数据';
    return;
  }

  isUploading.value = true;
  uploadError.value = null;
  uploadStep.value = null;

  try {
    const token = uni.getStorageSync('token');
    if (!token) {
      throw new Error('请先登录');
    }

    // 1. 调用转写接口，获取文本
    const transcription = await getTranscription(audioBase64, token);
    if (!transcription || transcription.trim().length === 0) {
      uploadError.value = '未检测到说话，请你重新录音';
      return;
    }
    // 2. 保存到 answers
    answers.value[`question${currentQuestionIndex.value + 1}`] = transcription;

    // 3. 判断是否为最后一题
    if (currentQuestionIndex.value < interviewQuestions.value.length - 1) {
      // 进入下一题
      currentQuestionIndex.value++;
      showWelcomeText.value = true;
      await playQuestion();
    } else {
      // 所有题目答完，统一分析
      await submitAllAnswers();
    }
  } catch (error) {
    uploadError.value = error?.message || '上传或分析失败，请重试';
    console.error('音频转写或保存答案失败:', error);
  } finally {
    isUploading.value = false;
  }
};

const base64ToBlob = (base64, mimeType) => {
  const byteCharacters = atob(base64);
  const byteArrays = [];

  for (let offset = 0; offset < byteCharacters.length; offset += 512) {
    const slice = byteCharacters.slice(offset, offset + 512);
    const byteNumbers = new Array(slice.length);
    
    for (let i = 0; i < slice.length; i++) {
      byteNumbers[i] = slice.charCodeAt(i);
    }
    
    const byteArray = new Uint8Array(byteNumbers);
    byteArrays.push(byteArray);
  }

  return new Blob(byteArrays, { type: mimeType });
};

const retryUpload = () => {
  uploadError.value = null;
  uploadAudio();
};

const handleContinueInterview = async () => {
  try {
    const jobId = String(
      route.query.jobId
      || uni.getStorageSync('currentJobId')
      || ''
    ).trim();
    
    if (!jobId) {
      throw new Error('请先选择职位');
    }
    uni.setStorageSync('currentJobId', jobId);

    const latestAssessmentId = uni.getStorageSync('latestAudioAssessmentId');
    if (!latestAssessmentId) {
      console.warn('latestAudioAssessmentId not found in storage.');
    }

    // 新增：保存场景评测历史
    if (analysisResults.value) {
      const comp = analysisResults.value.competenciesScore || {};
      const questionSuggestions = analysisResults.value.questionSuggestions || [];
      
      // 添加调试日志
      console.log('保存场景历史记录 - 分析结果:', analysisResults.value);
      console.log('保存场景历史记录 - 能力评分:', comp);
      console.log('保存场景历史记录 - 题目建议:', questionSuggestions);
      
      try {
        const historyData = {
          userId: Number((getUserSession() || {}).userId || 0),
          jobId,
          totalScore: analysisResults.value.logicScore,
          score1: comp.professionalSkills ?? 0,
          suggestion1: questionSuggestions[0] || '',
          score2: comp.projectExecution ?? 0,
          suggestion2: questionSuggestions[1] || '',
          score3: comp.innovation ?? 0,
          suggestion3: questionSuggestions[2] || '',
          score4: comp.communication ?? 0,
          suggestion4: questionSuggestions[3] || '',
          score5: comp.adaptability ?? 0,
          suggestion5: questionSuggestions[4] || ''
        };
        console.log('保存场景历史记录 - 发送数据:', historyData);
        await saveScenarioHistory(historyData);
        console.log('场景历史记录保存成功');
      } catch (e) {
        console.error('保存场景历史记录失败:', e);
        uni.showToast({ title: '历史保存失败: ' + (e.message || '未知错误'), icon: 'none', duration: 2000 });
      }
    }

    uni.setStorageSync('audioAssessment', {
      completed: true,
      score: analysisResults.value.logicScore,
      jobId: jobId,
      assessmentId: latestAssessmentId
    });

    uni.showToast({
      title: '音频评测完成',
      icon: 'success',
      duration: 1500
    });

    if (eventChannel.value) {
      eventChannel.value.emit('moduleComplete', {
        module: 'audio',
        score: analysisResults.value.logicScore,
        status: '已完成'
      });
    }

    setTimeout(() => {
      if (isFromAiInterview.value) {
        try {
          localStorage.setItem('audioJustCompleted', 'true');
          localStorage.setItem('audioCompletedTimestamp', Date.now().toString());
          localStorage.setItem('audioCompletedJobId', String(jobId || ''));
        } catch (e) {}
        const finalScore = Number(
          analysisResults.value?.logicScore
          ?? analysisResults.value?.overallScore
          ?? 0
        ) || 0;
        uni.redirectTo({
          url: buildInterviewAiReturnUrl({
            completedType: 'audio',
            mode: 'SPECIAL',
            score: finalScore,
            timestamp: Date.now()
          })
        });
        return;
      }
      uni.redirectTo({
        url: `/pages/interview-interface/index?jobId=${jobId}`
      });
    }, 1500);
  } catch (error) {
    console.error('处理继续面试失败:', error);
    uni.showToast({
      title: error.message || '处理继续面试失败',
      icon: 'none',
      duration: 2000
    });

    if (error.message.includes('登录')) {
      setTimeout(() => {
        uni.redirectTo({
          url: '/pages/login/index'
        });
      }, 2000);
    }
  }
};

const getTranscription = async (audioBase64, token) => {
  try {
    const formData = new FormData();
    const audioBlob = base64ToBlob(audioBase64, 'audio/wav');
    formData.append('file', audioBlob, 'recording.wav');

    const uploadResponse = await fetch(`${BASE_URL}/api/transcription/upload`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: formData
    });

    if (!uploadResponse.ok) {
      throw new Error('音频上传失败: ' + uploadResponse.statusText);
    }

    const uploadResult = await uploadResponse.json();
    if (uploadResult.code !== 0) {
      throw new Error(uploadResult.message || '音频上传失败');
    }

    const taskResponse = await fetch(`${BASE_URL}/api/transcription/create-task`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/x-www-form-urlencoded'
      },
      body: `audioUrl=${encodeURIComponent(uploadResult.data.url)}`
    });

    if (!taskResponse.ok) {
      throw new Error('创建转写任务失败: ' + taskResponse.statusText);
    }

    const taskResult = await taskResponse.json();
    if (taskResult.code !== 0) {
      throw new Error(taskResult.message || '创建转写任务失败');
    }

    const taskId = taskResult.data.task_id;

    let transcriptionResult = null;
    let attempts = 0;
    const maxAttempts = 30;

    while (attempts < maxAttempts) {
      const statusResponse = await fetch(`${BASE_URL}/api/transcription/query-task/${taskId}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      if (!statusResponse.ok) {
        throw new Error('查询任务状态失败: ' + statusResponse.statusText);
      }

      const statusResult = await statusResponse.json();
      if (statusResult.code !== 0) {
        throw new Error(statusResult.message || '查询任务状态失败');
      }

      if (statusResult.data.task_status === '3' || statusResult.data.task_status === '4') {
        transcriptionResult = statusResult;
        break;
      }

      await new Promise(resolve => setTimeout(resolve, 2000));
      attempts++;
    }

    if (!transcriptionResult) {
      throw new Error('转写任务超时');
    }

    return processTranscriptionResult(transcriptionResult);
  } catch (error) {
    console.error('获取转写文本失败:', error);
    throw error;
  }
};

const processTranscriptionResult = (result) => {
  if (!result.data.result || !result.data.result.lattice) {
    return '';
  }

  const textSegments = result.data.result.lattice
    .map(item => {
      const json = item.json_1best;
      if (!json || !json.st || !json.st.rt) return '';
      
      return json.st.rt
        .map(rt => rt.ws
          .map(ws => ws.cw
            .map(cw => cw.w)
            .join(''))
          .join(''))
        .join('');
    })
    .filter(text => text.length > 0);

  const text = textSegments.join(' ');
  return text.trim();
};

// 返回面试界面页面
const goBack = () => {
  // 优先从 route.query 获取 jobId，否则从 localStorage 获取
  let jobId = route.query.jobId || uni.getStorageSync('currentJobId') || 1;
  if (isFromAiInterview.value && jobId) {
    uni.redirectTo({
      url: buildInterviewAiReturnUrl()
    });
    return;
  }
  if (jobId) {
    uni.redirectTo({
      url: `/pages/interview-interface/index?jobId=${jobId}`,
      success: () => {
        console.log('Redirected to interview interface successfully');
      },
      fail: (err) => {
        console.error('Failed to redirect to interview interface:', err);
        uni.showToast({
          title: '跳转失败，请重试',
          icon: 'none'
        });
      }
    });
  } else {
    // 如果没有jobId，则返回上一页
    uni.navigateBack();
  }
};

const skipQuestion = async () => {
  console.log('skipQuestion called');
  if (currentQuestionIndex.value === interviewQuestions.value.length - 1) {
    // 最后一题，直接进入分析
    if (typeof uni !== 'undefined' && uni.showLoading) {
      uni.showLoading({ title: '正在分析中...', mask: true });
    } else if (window && window.alert) {
      // H5 fallback
      window._analyzingLoading = true;
      const loadingDiv = document.createElement('div');
      loadingDiv.id = 'analyzing-loading';
      loadingDiv.style = 'position:fixed;top:0;left:0;width:100vw;height:100vh;background:rgba(0,0,0,0.35);z-index:9999;display:flex;align-items:center;justify-content:center;font-size:2rem;color:#fff;';
      loadingDiv.innerText = '正在分析中...';
      document.body.appendChild(loadingDiv);
    }
    console.log('即将调用submitAllAnswers');
    await submitAllAnswers();
    isCompleted.value = true;
    if (typeof uni !== 'undefined' && uni.hideLoading) {
      uni.hideLoading();
    } else if (window && window._analyzingLoading) {
      const loadingDiv = document.getElementById('analyzing-loading');
      if (loadingDiv) document.body.removeChild(loadingDiv);
      window._analyzingLoading = false;
    }
  } else {
    currentQuestionIndex.value++;
    showWelcomeText.value = true;
    await playQuestion();
  }
};

const submitAllAnswers = async () => {
  isUploading.value = true;
  uploadError.value = null;
  try {
    // 获取岗位信息
    let jobInfo = null;
    const jobInfoStr = uni.getStorageSync('currentJobInfo');
    if (jobInfoStr) {
      jobInfo = JSON.parse(jobInfoStr);
    } else {
      const jobId = uni.getStorageSync('currentJobId');
      if (jobId) {
        try {
          const jobRes = await getJobInfo(jobId);
          jobInfo = jobRes.data;
        } catch (e) {
          jobInfo = { name: '', description: '' };
        }
      } else {
        jobInfo = { name: '', description: '' };
      }
    }

    // 构造题目和回答结构
    const questionsAndAnswers = interviewQuestions.value.map((q, idx) => ({
      question: q,
      answer: answers.value[`question${idx + 1}`] || ''
    }));

    // 检查是否有有效回答
    const hasValidAnswers = questionsAndAnswers.some(qa => qa.answer && qa.answer.trim().length > 0);
    
    // 调用后端内置 agent（系统提示词统一由后端维护）
    const response = await analyzeScenarioInterview(jobInfo, questionsAndAnswers);

    if (response.code === 200) {
      const result = response.data;
      if (!result || typeof result !== 'object') {
        console.error('AI响应格式错误:', response.data);
        uploadError.value = 'AI响应格式错误，请重试';
        return;
      }
      
      // 计算能力雷达图数据
      const clamp20 = (v) => {
        const n = Number(v ?? 0);
        if (Number.isNaN(n)) return 0;
        return Math.max(0, Math.min(20, n));
      };
      const competenciesScore = {
        professionalSkills: clamp20(result.abilities?.[0]?.score),
        projectExecution: clamp20(result.abilities?.[1]?.score),
        innovation: clamp20(result.abilities?.[2]?.score),
        communication: clamp20(result.abilities?.[3]?.score),
        adaptability: clamp20(result.abilities?.[4]?.score)
      };
      
      // 处理建议格式 - 将题目内容与建议组合
      const processedSuggestions = (result.questionSuggestions || []).map((suggestion, index) => {
        const question = interviewQuestions.value[index];
        // 如果建议已经包含题目内容，直接返回；否则添加题目内容
        if (suggestion.includes(question)) {
          return suggestion;
        } else {
          return `第${index + 1}题建议：${question}+${suggestion.replace(/^第\d+题建议：?/, '')}`;
        }
      });
      
      // 添加调试日志
      console.log('AI分析结果:', result);
      console.log('能力评分:', competenciesScore);
      console.log('原始题目建议:', result.questionSuggestions);
      console.log('处理后的题目建议:', processedSuggestions);
      
      // 生成唯一的评估ID
        const u = getUserSession() || {};
        const userId = Number(u.userId || 0);
      const assessmentId = `assessment_${Date.now()}_${userId}`;
      analysisResults.value = {
        assessmentId: assessmentId,
        transcribedText: questionsAndAnswers.map(qa => qa.answer).join('\n\n'),
        speedAnnotations: '面试完成',
        logicScore: Number(result.totalScore ?? 0),
        abilities: Array.isArray(result.abilities) ? result.abilities : [],
        questionSuggestions: processedSuggestions,
        pronunciationAdvice: result.pronunciationAdvice ?? '',
        expressionAdvice: result.expressionAdvice ?? '',
        benchmark: {
          averageSpeed: '120',
          averageLogicScore: '85'
        },
        competenciesScore: competenciesScore,
      };
      uni.setStorageSync('latestAudioAssessmentId', assessmentId);
      isCompleted.value = true;
    } else {
      throw new Error(response.message || 'AI分析失败');
    }
  } catch (error) {
    uploadError.value = '未检测到说话，请你重新录音';
  } finally {
    isUploading.value = false;
  }
};

onUnmounted(() => {
  if (mediaRecorder.value) {
    if (isRecording.value) {
      mediaRecorder.value.stop();
    }
    mediaRecorder.value.source.disconnect();
    mediaRecorder.value.processor.disconnect();
    mediaRecorder.value.audioContext.close();
    mediaRecorder.value.stream.getTracks().forEach(track => track.stop());
  }
  
  isRecording.value = false;
  isUploading.value = false;
  uploadError.value = null;
  
});
</script>

<style lang="scss">
@use "sass:math";
@use "sass:string";
.audio-evaluation-bg {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a1929 0%, #1a1f35 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding-top: 0;
  position: relative;
  overflow: hidden;

  &.eye-care {
    background: linear-gradient(135deg, #1c2a1f 0%, #2a3c2e 100%);

    &::before {
      background-image: 
        linear-gradient(rgba(144, 198, 149, 0.03) 1px, transparent 1px),
        linear-gradient(90deg, rgba(144, 198, 149, 0.03) 1px, transparent 1px);
      opacity: 0.2;
    }
  }
}

.audio-evaluation-card {
  background: rgba(30, 40, 60, 0.85);
  border: 1.5px solid rgba(124, 77, 255, 0.18);
  box-shadow: 0 8px 32px 0 rgba(0,0,0,0.25), 0 1.5px 6px rgba(0,229,255,0.08);
  backdrop-filter: blur(18px);
  border-radius: 24px;
  max-width: 540px;
  width: 100%;
  margin: 40px auto;
  padding: 48px 32px 40px 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  animation: fade-in 0.7s;
  position: relative;
  z-index: 1;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: linear-gradient(135deg, 
      rgba(124, 77, 255, 0.05) 0%, 
      rgba(0, 229, 255, 0.05) 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
    border-radius: 24px;
    pointer-events: none;
  }

  &:hover::before {
    opacity: 1;
  }
}

/* 护眼模式下的卡片颜色调整 */
.eye-care .audio-evaluation-card {
  background: rgba(127, 176, 105, 0.15);
  border: 1.5px solid rgba(127, 176, 105, 0.25);
  box-shadow: 0 8px 32px rgba(127, 176, 105, 0.2), 0 1.5px 6px rgba(0,0,0,0.15);

  &::before {
    background: linear-gradient(135deg, 
      rgba(127, 176, 105, 0.05) 0%, 
      rgba(144, 198, 149, 0.05) 100%);
  }
}

.eye-care .welcome-text-container,
.eye-care .button-block,
.eye-care .camera-status-area,
.eye-care .audio-wave-container,
.eye-care .instructions,
.eye-care .result-item {
  background: rgba(144, 198, 149, 0.12) !important;
  border: 1px solid rgba(127, 176, 105, 0.25);
  box-shadow: 0 2px 8px rgba(127, 176, 105, 0.15);
}

.eye-care .welcome-text-container:hover,
.eye-care .button-block:hover,
.eye-care .result-item:hover {
  background: rgba(127, 176, 105, 0.15) !important;
  border-color: rgba(127, 176, 105, 0.3);
  box-shadow: 0 4px 12px rgba(127, 176, 105, 0.2);
}

.eye-care .error-message {
  background: rgba(255, 77, 79, 0.08);
  border: 1px solid rgba(255, 77, 79, 0.25);
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.15);
}

.eye-care .control-button.primary {
  background: linear-gradient(90deg, #7fb069 0%, #90c695 100%);
}

.eye-care .control-button.primary:hover:not(:disabled) {
  background: linear-gradient(90deg, #6a9a5a 0%, #7fb069 100%);
  box-shadow: 0 8px 24px rgba(127, 176, 105, 0.3);
}

.eye-care .control-button.stop {
  background: linear-gradient(135deg, #ff4757 0%, #ff6b7a 100%);
}

.eye-care .control-button.stop:hover:not(:disabled) {
  background: linear-gradient(135deg, #e63946 0%, #ff4757 100%);
  box-shadow: 0 8px 24px rgba(255, 71, 87, 0.3);
}

.eye-care .control-button.retry {
  background: rgba(255, 77, 79, 0.1);
  border-color: rgba(255, 77, 79, 0.3);
}

.eye-care .control-button.retry:hover {
  background: rgba(255, 77, 79, 0.2);
  border-color: rgba(255, 77, 79, 0.4);
  box-shadow: 0 8px 25px rgba(255, 77, 79, 0.2);
}

.eye-care .spinner {
  border-color: rgba(127, 176, 105, 0.1);
  border-top-color: #7fb069;
}

.eye-care .loading-message {
  color: #7fb069;
}

.eye-care .camera-status {
  color: #7fb069;
}

.eye-care .error-content {
  color: #ff6b6b;
}

.eye-care .main-title,
.eye-care .welcome-text,
.eye-care .tips-main,
.eye-care .current-question-content,
.eye-care .result-item p,
.eye-care .instructions li {
  color: #fff !important;
}

.eye-care .subtitle,
.eye-care .question-progress,
.eye-care .current-question-title,
.eye-care .result-item h4,
.eye-care .ability-name,
.eye-care .ability-score {
  color: #7fb069 !important;
}

.eye-care .overall-score {
  color: #7fb069;
}

.eye-care .overall-score span {
  background: linear-gradient(135deg, #7fb069 0%, #90c695 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.eye-care .progress-bar-outer {
  background: rgba(127, 176, 105, 0.1);
}

.eye-care .progress-bar-inner {
  background: linear-gradient(90deg, #7fb069, #90c695);
}

.eye-care .ability-desc {
  color: rgba(255, 255, 255, 0.9) !important;
}

.eye-care .suggestion-list li {
  color: rgba(255, 255, 255, 0.9) !important;
}
@keyframes fade-in {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}
.main-title, .question-text, .overall-score, .control-button.big {
  color: #fff;
  text-shadow: 0 2px 8px rgba(124,77,255,0.18);
}
.subtitle {
  color: #00e5ff;
  font-size: 1.25rem;
  font-weight: 700;
  margin-bottom: 18px;
  text-align: center;
  text-shadow: 0 0 10px rgba(0, 229, 255, 0.3);
}
.desc-block {
  margin-bottom: 36px;
}
.desc, .desc-list li, .instructions, .question-text-container, .result-item p {
  color: rgba(255,255,255,0.92);
}
.desc-list li {
  position: relative;
  padding-left: 22px;
  margin-bottom: 14px;
  font-size: 1.05rem;
  line-height: 1.8;
}
.desc-list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0.7em;
  width: 10px;
  height: 10px;
  background: linear-gradient(135deg, #7c4dff, #00e5ff);
  border-radius: 50%;
  display: inline-block;
  margin-right: 8px;
  box-shadow: 0 0 10px rgba(124, 77, 255, 0.5);
}
.desc-list {
  padding-left: 0;
  margin: 0 auto;
  max-width: 340px;
  list-style: none;
}

.empty-tip {
  width: 100%;
  max-width: 480px;
  margin: 10px auto 0;
  padding: 16px;
  border-radius: 12px;
  background: rgba(255,255,255,0.06);
  border: 1px solid rgba(124,77,255,0.12);
  text-align: center;
  color: rgba(255,255,255,0.9);
}
.empty-text { margin-bottom: 10px; }
.control-button.big {
  width: 100%;
  max-width: 420px;
  font-size: 1.2rem;
  padding: 18px 0;
  border-radius: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #7c4dff 0%, #00e5ff 100%);
  color: #fff;
  border: 1px solid rgba(124, 77, 255, 0.2);
  margin-top: 20px;
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  backdrop-filter: blur(10px);
  box-shadow: 0 0 24px #00e5ff66, 0 4px 24px 0 rgba(124,77,255,0.25);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(
      90deg,
      transparent,
      rgba(255, 255, 255, 0.2),
      transparent
    );
    transition: 0.5s;
  }

  &:hover:not(:disabled) {
    transform: translateY(-5px);
    border-color: rgba(124, 77, 255, 0.3);
    box-shadow: 
      0 10px 30px rgba(0, 0, 0, 0.3),
      0 0 20px rgba(124, 77, 255, 0.2),
      0 0 40px rgba(0, 229, 255, 0.2);

    &::before {
      left: 100%;
    }
  }

  &:active {
    transform: translateY(-2px) scale(0.98);
  }
}

.btn-icon {
  font-size: 1.2em;
  filter: drop-shadow(0 0 5px rgba(255, 255, 255, 0.5));
}

.button-block {
  display: flex;
  justify-content: center;
  margin: 20px 0;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;
  backdrop-filter: blur(10px);
  box-shadow: 0 0 20px rgba(124, 77, 255, 0.1);
}

// 通用按钮样式
.control-button {
  background: linear-gradient(135deg, #7c4dff 0%, #00e5ff 100%);
  color: #fff;
  border: 1px solid rgba(124, 77, 255, 0.2);
  padding: 12px 24px;
  border-radius: 12px;
  font-size: 1rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.3s ease;
  cursor: pointer;
  backdrop-filter: blur(10px);
  box-shadow: 
    0 4px 15px rgba(0, 0, 0, 0.2),
    0 0 20px rgba(124, 77, 255, 0.1);
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(
      90deg,
      transparent,
      rgba(255, 255, 255, 0.2),
      transparent
    );
    transition: 0.5s;
  }

  &:hover:not(:disabled) {
    transform: translateY(-3px);
    border-color: rgba(124, 77, 255, 0.3);
    box-shadow: 
      0 8px 25px rgba(0, 0, 0, 0.3),
      0 0 20px rgba(124, 77, 255, 0.2);

    &::before {
      left: 100%;
    }
  }

  &:active {
    transform: translateY(-1px) scale(0.98);
  }

  &.stop {
    background: linear-gradient(135deg, #ff4757 0%, #ff6b7a 100%);
    border-color: rgba(255, 71, 87, 0.2);
    box-shadow: 
      0 4px 15px rgba(0, 0, 0, 0.2),
      0 0 20px rgba(255, 71, 87, 0.1);

    &:hover:not(:disabled) {
      border-color: rgba(255, 71, 87, 0.3);
      box-shadow: 
        0 8px 25px rgba(0, 0, 0, 0.3),
        0 0 20px rgba(255, 71, 87, 0.2);
    }
  }
}

.control-button.record-btn {
  background: linear-gradient(135deg, #6B8CFF 0%, #00D1FF 100%);
  border-color: rgba(107, 140, 255, 0.25);
  box-shadow: 0 6px 20px rgba(107,140,255,0.28), 0 0 24px rgba(0,209,255,0.18);
}
.control-button.record-btn:hover:not(:disabled) {
  background: linear-gradient(135deg, #5A7AF5 0%, #00C4F2 100%);
  box-shadow: 0 10px 28px rgba(107,140,255,0.35), 0 0 30px rgba(0,209,255,0.22);
}

.audio-wave-container {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  margin-bottom: 24px;
  min-height: 48px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  padding: 20px;
  backdrop-filter: blur(10px);
  box-shadow: 0 0 20px rgba(124, 77, 255, 0.1);
}
.audio-wave {
  display: block;
}
.audio-bar {
  fill: url(#waveGradient);
  opacity: 0.85;
  animation: waveMove 1.6s infinite cubic-bezier(.4,0,.2,1) alternate;
  transform-origin: 50% 100%;
  filter: drop-shadow(0 0 5px rgba(124, 77, 255, 0.3));
}
@keyframes waveMove {
  0%   { height: 12px; y: 16px; opacity: 0.7;}
  20%  { height: 28px; y: 4px; opacity: 1;}
  40%  { height: 18px; y: 12px; opacity: 0.8;}
  60%  { height: 32px; y: 0px; opacity: 1;}
  80%  { height: 20px; y: 10px; opacity: 0.85;}
  100% { height: 12px; y: 16px; opacity: 0.7;}
}

.error-message {
  background: rgba(255, 71, 87, 0.1);
  border: 1px solid rgba(255, 71, 87, 0.2);
  border-radius: 12px;
  padding: 16px;
  margin: 20px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  animation: slideIn 0.3s ease-out;
  backdrop-filter: blur(10px);
  box-shadow: 0 0 20px rgba(255, 71, 87, 0.1);
}

.error-content {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ff6b7a;
  font-size: 0.95rem;
  text-shadow: 0 0 10px rgba(255, 107, 122, 0.3);
}

.control-button.retry {
  background: rgba(255, 71, 87, 0.1);
  color: #ff6b7a;
  border: 1px solid rgba(255, 71, 87, 0.3);
  padding: 8px 20px;
  border-radius: 12px;
  font-size: 0.9rem;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s ease;
  cursor: pointer;
  backdrop-filter: blur(10px);
  box-shadow: 0 0 20px rgba(255, 71, 87, 0.1);
}

.control-button.retry:hover {
  background: rgba(255, 71, 87, 0.2);
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(255, 71, 87, 0.2);
  border-color: rgba(255, 71, 87, 0.4);
}

.control-button.retry:active {
  transform: translateY(-1px) scale(0.98);
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 600px) {
  .audio-evaluation-card {
    padding: 24px 8px 24px 8px;
    max-width: 98vw;
  }
  .main-title {
    font-size: 1.5rem;
    margin-bottom: 18px;
  }
  .desc-block {
    margin-bottom: 20px;
  }
  .control-button.big {
    font-size: 1rem;
    padding: 14px 0;
    max-width: 100%;
  }
  .error-message {
    padding: 12px;
    margin: 16px 0;
  }
  
  .error-content {
    font-size: 0.9rem;
  }
  
  .control-button.retry {
    padding: 6px 16px;
    font-size: 0.85rem;
  }
}
.loading-message {
  text-align: center;
  color: #00e5ff;
  margin: 20px 0;
  text-shadow: 0 0 10px rgba(0, 229, 255, 0.3);
}
.spinner {
  width: 40px;
  height: 40px;
  margin: 0 auto 10px;
  border: 4px solid rgba(255, 255, 255, 0.1);
  border-top: 4px solid #00e5ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  box-shadow: 0 0 20px rgba(0, 229, 255, 0.3);
}
@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
.instructions {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(124, 77, 255, 0.1);
  padding: 20px;
  border-radius: 12px;
  margin: 20px 0;
  backdrop-filter: blur(10px);
  box-shadow: 0 0 20px rgba(124, 77, 255, 0.1);
}
.instructions ul {
  margin: 10px 0;
  padding-left: 20px;
}
.instructions li {
  margin-bottom: 8px;
  color: rgba(255, 255, 255, 0.7);
}
.analysis-section {
  margin-top: 30px;
  width: 100%;

  h3 {
    color: #00e5ff;
    text-align: center;
    font-size: 1.5em;
    margin-bottom: 20px;
    text-shadow: 0 0 10px rgba(0, 229, 255, 0.3);
  }
}
.overall-score {
  font-size: 1.2em;
  color: #00e5ff;
  text-align: center;
  margin-bottom: 30px;
  text-shadow: 0 0 10px rgba(0, 229, 255, 0.3);
}
.overall-score span {
  font-weight: bold;
  font-size: 1.5em;
  background: linear-gradient(135deg, #7c4dff 0%, #00e5ff 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  text-shadow: 0 0 20px rgba(124, 77, 255, 0.3);
}
.feedback-section {
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}
.result-item {
  width: 100%;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(124, 77, 255, 0.1);
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 20px;
  backdrop-filter: blur(10px);
  box-shadow: 
    0 4px 15px rgba(0, 0, 0, 0.2),
    0 0 20px rgba(124, 77, 255, 0.1);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    border-color: rgba(124, 77, 255, 0.2);
    box-shadow: 
      0 8px 25px rgba(0, 0, 0, 0.3),
      0 0 20px rgba(124, 77, 255, 0.2);
  }
}
.result-item h4 {
  color: #00e5ff;
  margin: 0 0 15px 0;
  font-size: 1.1em;
  text-shadow: 0 0 10px rgba(0, 229, 255, 0.3);
}
.result-item p {
  color: rgba(255, 255, 255, 0.8);
  line-height: 1.6;
  margin: 0;
  white-space: pre-line;
}
.metrics {
  display: flex;
  gap: 20px;
  margin-top: 15px;
}
.metric {
  flex: 1;
}
.metric .label {
  color: #666;
  display: block;
  margin-bottom: 5px;
}
.metric .value {
  font-weight: 500;
  color: #333;
}
.score-display {
  font-size: 3em;
  font-weight: bold;
  color: #4A6FFF;
  text-align: center;
  margin-bottom: 20px;
}
.score-label {
  font-size: 0.4em;
  color: #666;
}
.suggestions h5 {
  color: #666;
  margin-bottom: 10px;
}
.suggestions ul {
  list-style: none;
  padding: 0;
}
.suggestions li {
  margin-bottom: 8px;
  padding-left: 20px;
  position: relative;
}
.suggestions li:before {
  content: "•";
  position: absolute;
  left: 0;
  color: #4A6FFF;
}
.benchmark-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}
.benchmark-item .label {
  color: #666;
}
.benchmark-item .value {
  font-weight: 500;
}
.action-buttons {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-top: 40px;
}
.action-buttons button {
  margin: 0 10px;
}
.welcome-text-container {
  text-align: center;
  padding: 30px 25px;
  margin-bottom: 30px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(124, 77, 255, 0.1);
  border-radius: 16px;
  backdrop-filter: blur(10px);
  box-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.2),
    0 0 20px rgba(124, 77, 255, 0.1);
  position: relative;
  overflow: hidden;
  transition: all 0.6s ease-in-out;
}

.welcome-text-container.is-speaking {
  animation: speakingPulse 1.8s infinite ease-in-out alternate;
}

@keyframes speakingPulse {
  0% {
    box-shadow: 
      0 4px 20px rgba(0, 0, 0, 0.2), 
      0 0 0 0 rgba(124, 77, 255, 0);
  }
  100% {
    box-shadow: 
      0 4px 25px rgba(0, 0, 0, 0.3), 
      0 0 0 15px rgba(124, 77, 255, 0.1);
  }
}

.welcome-text {
  font-size: 1.1rem;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 0;
  white-space: pre-wrap;
  z-index: 1;
  position: relative;
  text-shadow: 0 0 10px rgba(255, 255, 255, 0.1);
}

.fade-in {
  animation: fadeIn 0.5s ease-out;
}

.fade-out {
  animation: fadeOut 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes fadeOut {
  from {
    opacity: 1;
    transform: translateY(0);
  }
  to {
    opacity: 0;
    transform: translateY(-10px);
  }
}

/* Starfield background effect */
@function multiple-box-shadow($n) {
  $value: '#{math.random(2560)}px #{math.random(2560)}px #FFF';
  @for $i from 2 through $n {
    $value: '#{$value}, #{math.random(2560)}px #{math.random(2560)}px #FFF';
  }
  @return string.unquote($value);
}

$shadows-small: multiple-box-shadow(1000);
$shadows-medium: multiple-box-shadow(300);
$shadows-large: multiple-box-shadow(150);

#stars, #stars2, #stars3 {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: auto;
  height: auto;
  display: block;
  z-index: 0;
  transform: translateZ(0);
  background: transparent;
}

#stars {
  width: 1px;
  height: 1px;
  box-shadow: $shadows-small;
  animation: animStar 150s linear infinite;
}

#stars2 {
  width: 2px;
  height: 2px;
  box-shadow: $shadows-medium;
  animation: animStar 100s linear infinite;
}

#stars3 {
  width: 3px;
  height: 3px;
  box-shadow: $shadows-large;
  animation: animStar 50s linear infinite;
}

@keyframes animStar {
  from {
    transform: translateY(0px);
  }
  to {
    transform: translateY(-2560px);
  }
}

.control-button:disabled {
  background: rgba(255, 255, 255, 0.1);
  color: rgba(255, 255, 255, 0.3);
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
  border-color: rgba(255, 255, 255, 0.1);

  &::before {
    display: none;
  }
}

.control-button:disabled:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: none;
  box-shadow: none;
  border-color: rgba(255, 255, 255, 0.1);
}

.action-buttons {
  width: 100%;
  display: flex;
  justify-content: center;
  margin-top: 40px;
}

@media (max-width: 768px) {
  .radar-chart-container {
    height: 400px;
    margin: 20px auto;
  }

  .result-item {
    padding: 15px;
  }

  .result-item h4 {
    font-size: 1em;
  }

  .result-item p {
    font-size: 0.9em;
  }
}

// 分隔线样式
.divider {
  width: 100%;
  height: 1.5px;
  background: linear-gradient(90deg, rgba(124,77,255,0.18), rgba(0,229,255,0.12));
  margin: 24px 0;
  border-radius: 1px;
}

.question-progress {
  color: #00e5ff;
  font-size: 1.15rem;
  font-weight: 700;
  letter-spacing: 1px;
  margin-bottom: 18px;
  margin-left: 56px;
}
.tips-main {
  color: #fff;
  font-size: 1.18rem;
  font-weight: 700;
  margin-bottom: 16px;
  text-shadow: 0 2px 8px rgba(0,229,255,0.12);
}
.current-question-title {
  color: #7c4dff;
  font-size: 1.08rem;
  font-weight: 700;
  margin: 12px 0 8px 0;
}
.current-question-content {
  color: #fff;
  font-size: 1.08rem;
  font-weight: 600;
  line-height: 1.8;
  margin-bottom: 10px;
  text-shadow: 0 2px 8px rgba(124,77,255,0.10);
}

/* 返回按钮样式 */
.back-button {
  position: fixed;
  top: 20px;
  left: 20px;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: rgba(124, 77, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(124, 77, 255, 0.2);
  transition: all 0.3s ease;
  cursor: pointer;
  z-index: 1000;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);

  &:hover {
    transform: scale(1.05);
    background: rgba(124, 77, 255, 0.2);
    border-color: rgba(124, 77, 255, 0.3);
    box-shadow: 
      0 0 20px rgba(124, 77, 255, 0.2),
      0 0 40px rgba(0, 229, 255, 0.1);
  }

  svg {
    color: #7c4dff;
    filter: drop-shadow(0 0 5px rgba(124, 77, 255, 0.5));
  }
}

/* 护眼模式下的返回按钮样式 */
.eye-care .back-button {
  background: rgba(127, 176, 105, 0.1);
  border-color: rgba(127, 176, 105, 0.2);

  &:hover {
    background: rgba(127, 176, 105, 0.2);
    border-color: rgba(127, 176, 105, 0.3);
    box-shadow: 
      0 0 20px rgba(127, 176, 105, 0.2),
      0 0 40px rgba(144, 198, 149, 0.1);
  }

  svg {
    color: #7fb069;
    filter: drop-shadow(0 0 5px rgba(127, 176, 105, 0.5));
  }
}

.control-button.primary.big.skip {
  background: linear-gradient(90deg, #00e5ff 0%, #7c4dff 100%);
  color: #fff;
  border-radius: 24px;
  font-size: 20px;
  font-weight: 600;
  height: 56px;
  min-width: 200px;
  box-shadow: 0 2px 8px rgba(124,77,255,0.12);
  transition: background 0.2s, box-shadow 0.2s;
}
.control-button.primary.big.skip:hover:not(:disabled) {
  background: linear-gradient(90deg, #7c4dff 0%, #00e5ff 100%);
  box-shadow: 0 4px 16px rgba(124,77,255,0.18);
  color: #fff;
  cursor: pointer;
}
.control-button.primary.big.skip:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.radar-chart-container {
  width: 100%;
  max-width: 480px;
  margin: 0 auto;
  height: 400px;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
}
.echarts-radar {
  width: 100%;
  height: 100%;
  min-width: 300px;
  min-height: 300px;
}
.ability-progress-block {
  margin-bottom: 18px;
}
.ability-label-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}
.ability-name {
  font-weight: 600;
  color: #00e5ff;
  font-size: 1.08em;
}
.ability-score {
  font-weight: 600;
  color: #7c4dff;
  font-size: 1.08em;
}
.progress-bar-outer {
  width: 100%;
  height: 14px;
  background: rgba(124,77,255,0.10);
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 4px;
}
.progress-bar-inner {
  height: 100%;
  background: linear-gradient(90deg, #7c4dff 0%, #00e5ff 100%);
  border-radius: 8px;
  transition: width 0.6s cubic-bezier(.4,0,.2,1);
}
.ability-desc {
  color: #fff;
  font-size: 0.98em;
  margin-bottom: 2px;
  margin-left: 2px;
}
.suggestion-list {
  padding-left: 18px;
  margin: 0;
}
.suggestion-list li {
  color: #fff;
  margin-bottom: 8px;
  list-style: disc;
  font-size: 1em;
}
.suggestion-box { width: 100%; background: linear-gradient(180deg, rgba(30,34,54,0.85), rgba(30,34,54,0.72)); border: 1px solid rgba(124,77,255,0.25); border-left: 4px solid #4A6FFF; border-radius: 12px; padding: 18px 18px 16px 18px; box-shadow: 0 6px 18px rgba(0,0,0,0.22); }
.suggestion-header { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.suggestion-icon { color: #4A6FFF; filter: drop-shadow(0 0 6px rgba(74,111,255,0.35)); }
.suggestion-title { color: #eaf2ff; font-weight: 700; letter-spacing: 1px; }
.suggestion-list.enhanced { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 8px; }
.suggestion-item { color: #eaf2ff; background: rgba(124,77,255,0.12); border: 1px solid rgba(124,77,255,0.22); border-radius: 10px; padding: 10px 12px; line-height: 1.6; }
.eye-care .suggestion-box { background: rgba(127,176,105,0.12); border-color: rgba(127,176,105,0.28); border-left-color: #7fb069; box-shadow: 0 6px 18px rgba(0,0,0,0.18); }
.eye-care .suggestion-icon { color: #7fb069; filter: drop-shadow(0 0 6px rgba(127,176,105,0.35)); }
.eye-care .suggestion-title { color: #fffbea; }
.eye-care .suggestion-item { color: #153023; background: rgba(127,176,105,0.12); border-color: rgba(127,176,105,0.28); }
</style> 
