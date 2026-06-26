import { toFixed } from 'ant-design-vue/es/input-number/src/utils/MiniDecimal'
import { saveAs } from 'file-saver'
import type { st } from 'vue-router/dist/router-CWoNjPRp.mjs'

/**
 * 格式化文件大小
 * @param size
 * @returns
 */
export const formatSize = (size?: number) => {
  if (!size) return '未知'
  if (size < 1024) return size + 'B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(2) + 'KB'
  return (size / (1024 * 1024)).toFixed(2) + 'MB'
}

export function downloadImage(url?: string, fileName?: string) {
  if (!url) {
    return
  }
  saveAs(url, fileName)
}

export function toHexColor(input: string) {
  //  去掉 0x前缀
  const colorValue = input.startsWith('0x') ? input.slice(2) : input
  //  将剩余部分解析为十六进制数，再转成6位十六进制字符串
  const hexColor = parseInt(colorValue, 16).toString(16).padStart(6, '0')
  //  返回标准的 #RRGGBB格式

  return `#${hexColor}`
}
