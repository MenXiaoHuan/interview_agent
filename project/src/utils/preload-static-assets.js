// Preload frequently used static images early to avoid "late pop-in" on first page enter.
// Works for H5 (browser Image) and falls back to uni.getImageInfo where available.

import featureChatAi from '@/static/funtion_show/chat_ai.jpg';
import featureHomepageFemale from '@/static/funtion_show/homepage_female.jpg';
import featureHomepageMale from '@/static/funtion_show/homepage_male.jpg';
import featureInterviewAi from '@/static/funtion_show/interview_ai.jpg';
import featureJobSelectionFemale from '@/static/funtion_show/job_selection_female.jpg';
import featureJobSelectionMale from '@/static/funtion_show/job_selection_male.jpg';

const DEFAULT_STATIC_IMAGE_URLS = [
  '/static/pp_male.png',
  '/static/pp_female.png',
  featureChatAi,
  featureHomepageFemale,
  featureHomepageMale,
  featureInterviewAi,
  featureJobSelectionFemale,
  featureJobSelectionMale
];

export function preloadStaticImages(urls = DEFAULT_STATIC_IMAGE_URLS) {
  const list = Array.from(new Set((urls || []).filter(Boolean)));
  if (list.length === 0) return;

  // H5: warm the browser cache.
  if (typeof Image !== 'undefined') {
    for (const src of list) {
      const img = new Image();
      // Hint the browser this is not a lazily loaded asset.
      img.decoding = 'async';
      img.loading = 'eager';
      img.src = src;
    }
    return;
  }

  // Other uni-app runtimes: best-effort cache warm-up.
  if (typeof uni !== 'undefined' && typeof uni.getImageInfo === 'function') {
    for (const src of list) {
      try {
        uni.getImageInfo({ src });
      } catch (_) {
        // ignore
      }
    }
  }
}
