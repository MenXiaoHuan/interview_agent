import { defineConfig, loadEnv } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import fs from 'fs'
import path from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

const repoRoot = path.resolve(__dirname, '..')

function getPlatformEnv(mode) {
  return {
    ...loadEnv(mode, repoRoot, ''),
    ...process.env
  }
}

function toPort(value, fallback) {
  const port = Number.parseInt(value, 10)
  return Number.isInteger(port) && port > 0 ? port : fallback
}

export default defineConfig(({ mode }) => {
  const env = getPlatformEnv(mode)
  const webPort = toPort(env.PLATFORM_WEB_HOST_PORT, 5173)
  const apiProxyTarget = env.PLATFORM_WEB_API_PROXY_TARGET || 'http://localhost:8080'

  return {
    css: {
      preprocessorOptions: {
        sass: {
          api: 'modern-compiler',
          quietDeps: true,
          silenceDeprecations: ['legacy-js-api']
        },
        scss: {
          api: 'modern-compiler',
          quietDeps: true,
          silenceDeprecations: ['legacy-js-api']
        }
      }
    },
    plugins: [
      uni(),
      AutoImport({
        resolvers: [ElementPlusResolver()],
      }),
      Components({
        resolvers: [ElementPlusResolver()],
      }),
    ],
    server: {
      https: {
        key: fs.readFileSync(path.resolve(__dirname, 'localhost+2-key.pem')),
        cert: fs.readFileSync(path.resolve(__dirname, 'localhost+2.pem')),
      },
      port: webPort,
      proxy: {
        '/api': {
          target: apiProxyTarget,
          changeOrigin: false,
          secure: false
        },
        '/avatar': {
          target: apiProxyTarget,
          changeOrigin: false,
          secure: false
        },
        '/scenario': {
          target: apiProxyTarget,
          changeOrigin: false,
          secure: false
        }
      }
    },
    resolve: {
      alias: {
        '@': path.resolve(__dirname, './src')
      }
    }
  }
})
