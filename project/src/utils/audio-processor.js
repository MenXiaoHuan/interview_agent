// AudioWorklet module code is loaded via URL. uni-app's build toolchain may not
// reliably support `?url` imports for JS modules, so we generate a Blob URL
// at runtime and pass it to `audioWorklet.addModule(...)`.

const AUDIO_PROCESSOR_WORKLET_CODE = `
class AudioProcessor extends AudioWorkletProcessor {
  constructor() {
    super();
    this.bufferSize = 16384;
    this.buffer = new Float32Array(this.bufferSize);
    this.bufferIndex = 0;
  }

  process(inputs) {
    const input = inputs[0];
    if (input && input.length > 0) {
      const channel = input[0];
      for (let i = 0; i < channel.length; i++) {
        this.buffer[this.bufferIndex++] = channel[i];
        if (this.bufferIndex >= this.bufferSize) {
          this.port.postMessage({ audioData: this.buffer.slice() });
          this.bufferIndex = 0;
        }
      }
    }
    return true;
  }
}
registerProcessor('audio-processor', AudioProcessor);
`.trim();

let cachedWorkletUrl = null;

export function getAudioProcessorWorkletUrl() {
  if (cachedWorkletUrl) return cachedWorkletUrl;
  const blob = new Blob([AUDIO_PROCESSOR_WORKLET_CODE], {
    type: 'application/javascript'
  });
  cachedWorkletUrl = URL.createObjectURL(blob);
  return cachedWorkletUrl;
}

export function revokeAudioProcessorWorkletUrl() {
  if (!cachedWorkletUrl) return;
  URL.revokeObjectURL(cachedWorkletUrl);
  cachedWorkletUrl = null;
}
