/**
 * WeChat mini-program subscribe messages (支付成功 / 发货通知).
 * Must be invoked from a tap. Empty tmplIds → no-op so H5 / mock stays crash-free.
 */
import http from '@/utils/http.js'
const envPay = () => (import.meta.env.VITE_APP_WX_TMPL_PAY || '').trim()
const envShip = () => (import.meta.env.VITE_APP_WX_TMPL_SHIP || '').trim()

let cachedPay = ''
let cachedShip = ''
let prefetching = null

function looksLikeTemplateId (value) {
  if (!value) return false
  const v = String(value).trim()
  if (!v) return false
  const lower = v.toLowerCase()
  if (v === '-' || lower === 'none' || lower === 'unset' || lower === 'todo') return false
  if (v.toUpperCase().startsWith('YOUR_') || lower.includes('placeholder')) return false
  return true
}

function currentTmplIds () {
  const pay = looksLikeTemplateId(cachedPay) ? cachedPay.trim() : envPay()
  const ship = looksLikeTemplateId(cachedShip) ? cachedShip.trim() : envShip()
  return [pay, ship].filter(looksLikeTemplateId).slice(0, 3)
}

export function prefetchSubscribeTmplIds () {
  if (prefetching) return prefetching
  prefetching = http.request({
    url: '/wx/subscribe/config',
    method: 'GET',
    dontTrunLogin: true
  }).then(({ data }) => {
    cachedPay = (data && data.paySuccessTemplateId) || ''
    cachedShip = (data && data.shipTemplateId) || ''
  }).catch(() => {
    // keep env fallback
  }).finally(() => {
    prefetching = null
  })
  return prefetching
}

/**
 * Show WeChat's subscribe popup. Always resolves; never blocks pay.
 */
export function requestOrderSubscribe () {
  // #ifndef MP-WEIXIN
  return Promise.resolve()
  // #endif
  // #ifdef MP-WEIXIN
  const ids = currentTmplIds()
  if (!ids.length) {
    return Promise.resolve()
  }
  return new Promise((resolve) => {
    try {
      uni.requestSubscribeMessage({
        tmplIds: ids,
        complete: () => resolve()
      })
    } catch (e) {
      resolve()
    }
  })
  // #endif
}
