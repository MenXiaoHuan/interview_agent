import { BASE_URL } from '@/utils/api/api-config';

export const API = {
  USER: {
    AVATAR: `${BASE_URL}/api/user/avatar`,
    NICKNAME: `${BASE_URL}/api/user/nickname`,
    PROFILE: `${BASE_URL}/api/user/profile`,
    CHANGE_PASSWORD: `${BASE_URL}/api/user/password`,
    BIND_EMAIL: `${BASE_URL}/api/user/email`,
    BIND_PHONE: `${BASE_URL}/api/user/phone`,
  },
  FEEDBACK: {
    SUBMIT: `${BASE_URL}/api/feedback/submit`,
  },
};
