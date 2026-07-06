import { BASE_URL } from '@/utils/api/api-config';

export const API = {
  AUTH: {
    LOGIN: `${BASE_URL}/api/auth/login`,
    FORGOT: {
      SEND_CODE: `${BASE_URL}/api/auth/forgot/send-code`,
      EMAIL_CODE: (email) => `${BASE_URL}/api/auth/forgot/email-code?email=${encodeURIComponent(email)}`,
      PHONE_CODE: (phone) => `${BASE_URL}/api/auth/forgot/phone-code?phone=${encodeURIComponent(phone)}`,
      RESET: `${BASE_URL}/api/auth/forgot/reset`,
    },
  },
};
