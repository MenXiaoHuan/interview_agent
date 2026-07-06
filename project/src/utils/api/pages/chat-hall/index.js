import { BASE_URL } from '@/utils/api/api-config';

export const API = {
  USER: {
    PROFILE: `${BASE_URL}/api/user/profile`,
  },
  CHAT: {
    MESSAGES: (days = 1) => `${BASE_URL}/api/chat/messages?days=${days}`,
    MESSAGE: `${BASE_URL}/api/chat/message`,
  },
};
