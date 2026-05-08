// 图标映射表 - 提供Font Awesome图标的Unicode/Emoji备选方案
export const iconMap = {
  // 导航和操作图标
  'fas fa-arrow-left': '←', // 左箭头
  'fas fa-arrow-right': '→', // 右箭头
  'fas fa-times': '✕', // 关闭
  'fas fa-check': '✓', // 勾选
  'fas fa-check-circle': '✅', // 成功圆圈
  'fas fa-clock': '⏰', // 时钟
  'fas fa-play': '▶️', // 播放
  'fas fa-stop': '⏹️', // 停止
  'fas fa-pause': '⏸️', // 暂停
  
  // 录音和音频相关
  'fas fa-microphone': '🎤', // 麦克风
  'fas fa-circle recording-indicator': '🔴', // 录音指示器
  'fas fa-volume-up': '🔊', // 音量
  'fas fa-volume-mute': '🔇', // 静音
  
  // 文件和文档
  'fas fa-file': '📄', // 文件
  'fas fa-file-alt': '📝', // 文档
  'fas fa-file-pdf': '📕', // PDF
  'fas fa-file-word': '📘', // Word
  'fas fa-briefcase': '💼', // 公文包
  'fas fa-cloud-upload-alt': '☁️', // 云上传
  
  // 评估和统计
  'fas fa-star': '⭐', // 星星
  'fas fa-chart-bar': '📊', // 柱状图
  'fas fa-chart-line': '📈', // 折线图
  'fas fa-bullseye': '🎯', // 靶心
  'fas fa-tachometer-alt': '⚡', // 速度表
  'fas fa-random': '🎲', // 随机
  
  // 沟通和表达
  'fas fa-comment-alt': '💬', // 评论
  'fas fa-heart': '❤️', // 心形
  'fas fa-smile': '😊', // 微笑
  'fas fa-lightbulb': '💡', // 灯泡
  'fas fa-eye': '👁️', // 眼睛
  
  // 展开/收起
  'fas fa-chevron-up': '▲', // 向上箭头
  'fas fa-chevron-down': '▼', // 向下箭头
  'fas fa-chevron-left': '◀', // 向左箭头
  'fas fa-chevron-right': '▶', // 向右箭头
  
  // 其他常用图标
  'fas fa-home': '🏠', // 家
  'fas fa-user': '👤', // 用户
  'fas fa-cog': '⚙️', // 设置
  'fas fa-search': '🔍', // 搜索
  'fas fa-download': '⬇️', // 下载
  'fas fa-upload': '⬆️', // 上传
  'fas fa-save': '💾', // 保存
  'fas fa-edit': '✏️', // 编辑
  'fas fa-trash': '🗑️', // 删除
  'fas fa-plus': '➕', // 加号
  'fas fa-minus': '➖', // 减号
  'fas fa-question': '❓', // 问号
  'fas fa-exclamation': '❗', // 感叹号
  'fas fa-info-circle': 'ℹ️', // 信息
  'fas fa-warning': '⚠️', // 警告
  'fas fa-exclamation-triangle': '⚠️', // 警告三角形
  'fas fa-ban': '🚫', // 禁止
  'fas fa-lock': '🔒', // 锁定
  'fas fa-unlock': '🔓', // 解锁
  'fas fa-shield-alt': '🛡️', // 盾牌
  'fas fa-key': '🔑', // 钥匙
  'fas fa-envelope': '✉️', // 信封
  'fas fa-phone': '📞', // 电话
  'fas fa-mobile-alt': '📱', // 手机
  'fas fa-laptop': '💻', // 笔记本
  'fas fa-desktop': '🖥️', // 台式机
  'fas fa-tablet-alt': '📱', // 平板
  'fas fa-wifi': '📶', // WiFi
  'fas fa-bluetooth': '📡', // 蓝牙
  'fas fa-battery-full': '🔋', // 电池满
  'fas fa-battery-half': '🔋', // 电池半满
  'fas fa-battery-empty': '🔋', // 电池空
  'fas fa-calendar': '📅', // 日历
  'fas fa-calendar-alt': '📆', // 日历
  'fas fa-hourglass': '⏳', // 沙漏
  'fas fa-stopwatch': '⏱️', // 秒表
  'fas fa-timer': '⏲️', // 定时器
};

// 获取图标函数
export function getIcon(className) {
  return iconMap[className] || '❓';
}

// 检查Font Awesome是否加载
export function isFontAwesomeLoaded() {
  if (typeof window === 'undefined' || !window.document) {
    return false;
  }
  
  // 检查是否有Font Awesome的CSS链接
  const fontAwesomeLink = window.document.querySelector('link[href*="font-awesome"]');
  if (fontAwesomeLink) {
    return true;
  }
  
  // 检查是否有Font Awesome的字体文件
  const fontAwesomeFont = window.document.querySelector('link[href*="all.min.css"]');
  if (fontAwesomeFont) {
    return true;
  }
  
  // 检查CSS中是否定义了Font Awesome的字体
  const styleSheets = window.document.styleSheets;
  for (let i = 0; i < styleSheets.length; i++) {
    try {
      const rules = styleSheets[i].cssRules || styleSheets[i].rules;
      for (let j = 0; j < rules.length; j++) {
        if (rules[j].cssText && rules[j].cssText.includes('font-awesome')) {
          return true;
        }
      }
    } catch (e) {
      // 跨域样式表会抛出错误，忽略
    }
  }
  
  return false;
}

// 智能图标组件
export function getSmartIcon(className) {
  // 返回Unicode/Emoji
  return { type: 'unicode', value: getIcon(className) };
} 