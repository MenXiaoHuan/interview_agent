export const DEFAULT_MALE_AVATAR = '/static/pp_male.png'
export const DEFAULT_FEMALE_AVATAR = '/static/pp_female.png'
export const DEFAULT_AVATAR = DEFAULT_MALE_AVATAR

function normalizeGender(input) {
  if (input && typeof input === 'object') {
    return Number(input.gender ?? 0)
  }
  return Number(input ?? 0)
}

export function getDefaultAvatarByGender(input) {
  return normalizeGender(input) === 2 ? DEFAULT_FEMALE_AVATAR : DEFAULT_MALE_AVATAR
}

export function resolveUserAvatar(user, fallbackGender = 0) {
  if (typeof user === 'string') {
    const avatar = user.trim()
    return avatar || getDefaultAvatarByGender(fallbackGender)
  }

  const avatar = String(user?.avatar || user?.avatarUrl || '').trim()
  if (avatar) {
    return avatar
  }

  return getDefaultAvatarByGender(user?.gender ?? fallbackGender)
}

export function applyAvatarFallback(event, user, fallbackGender = 0) {
  const target = event?.target || event?.currentTarget
  if (!target) {
    return
  }
  target.src = getDefaultAvatarByGender(user?.gender ?? user ?? fallbackGender)
}
