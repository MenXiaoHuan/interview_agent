// 本地存储工具，兼容uni和浏览器
export function getStorage(key) {
  try {
    if (key === 'token' || key === 'userInfo' || key === 'activeUserId') {
      const sessVal = typeof window !== 'undefined' ? window.sessionStorage.getItem(key) : null;
      if (sessVal) return sessVal;
    }
  } catch {}
  if (typeof uni !== 'undefined') {
    return uni.getStorageSync(key);
  }
  return localStorage.getItem(key);
}

export function setStorage(key, value) {
  try {
    if (key === 'token' || key === 'userInfo' || key === 'activeUserId') {
      if (typeof window !== 'undefined') window.sessionStorage.setItem(key, value);
    }
  } catch {}
  if (typeof uni !== 'undefined') {
    return uni.setStorageSync(key, value);
  }
  return localStorage.setItem(key, value);
}

export function removeStorage(key) {
  try {
    if (key === 'token' || key === 'userInfo' || key === 'activeUserId') {
      if (typeof window !== 'undefined') window.sessionStorage.removeItem(key);
    }
  } catch {}
  if (typeof uni !== 'undefined') {
    return uni.removeStorageSync(key);
  }
  return localStorage.removeItem(key);
}
