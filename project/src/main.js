import {
	createSSRApp
} from "vue";
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import App from "./App.vue";
import './styles/theme.css';  // 引入全局主题样式

// === 预加载所有页面和组件 ===
// 组件预加载
import(/* webpackPrefetch: true */ '@/components/SmartIcon.vue');
import(/* webpackPrefetch: true */ '@/components/RadarChart.vue');
import(/* webpackPrefetch: true */ '@/components/FancySelect.vue');
import(/* webpackPrefetch: true */ '@/components/Modal.vue');
import(/* webpackPrefetch: true */ '@/components/JobSelector.vue');
// 页面预加载
import(/* webpackPrefetch: true */ '@/pages/interview-scenario/index.vue');
import(/* webpackPrefetch: true */ '@/pages/comprehensive-scenario/index.vue');
import(/* webpackPrefetch: true */ '@/pages/comprehensive-resume/index.vue');
import(/* webpackPrefetch: true */ '@/pages/history/index.vue');
import(/* webpackPrefetch: true */ '@/pages/personal-center/index.vue');
import(/* webpackPrefetch: true */ '@/pages/interview-ai/index.vue');
import(/* webpackPrefetch: true */ '@/pages/interview-resume/index.vue');
import(/* webpackPrefetch: true */ '@/pages/register/index.vue');
import(/* webpackPrefetch: true */ '@/pages/comprehensive-questions/index.vue');
import(/* webpackPrefetch: true */ '@/pages/interview-interface/index.vue');
import(/* webpackPrefetch: true */ '@/pages/job-selection/index.vue');
import(/* webpackPrefetch: true */ '@/pages/interview-questions/index.vue');
import(/* webpackPrefetch: true */ '@/pages/login/index.vue');
import(/* webpackPrefetch: true */ '@/pages/comprehensive-report/index.vue');

export function createApp() {
	const app = createSSRApp(App);
	const pinia = createPinia();
	
	// 注册 Element Plus
	app.use(ElementPlus);
	
	// 注册所有图标
	for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
		app.component(key, component);
	}
	
	app.use(pinia);

	// 全局异常处理
	app.config.errorHandler = (err, _instance, info) => {
		console.error('全局异常:', err, info);
		if (typeof uni !== 'undefined') {
			uni.showToast({ title: '发生错误，请重试', icon: 'none', duration: 2000 });
		}
	};

	return {
		app,
		pinia,
	};
}
