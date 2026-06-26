<template>
  <div>
    <a-modal v-model:visible="visible" :title="title" :footer="false" @cancle="closeModal">
      <h4>复制分享链接</h4>
      <a-typography-link copyable>
        {{ link }}
      </a-typography-link>
      <div style="margin-bottom: 16px" />
      <h4>手机扫码查看</h4>
      <a-qrcode :value="link" />
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { addAbortSignal } from 'stream'
import { ref } from 'vue'
import { st } from 'vue-router/dist/router-CWoNjPRp.mjs'
const open = ref<boolean>(false)
interface Props {
  title: string
  link: string
}
const props = withDefaults(defineProps<Props>(), {
  title: '分享图片',
  link: 'https://zshen.chousan.top',
})

//  是否可见
const visible = ref(false)

//  打开弹窗
const openModal = () => {
  visible.value = true
}
//  关闭弹窗
const closeModal = () => {
  visible.value = false
}
//  暴漏函数给父组件
defineExpose({
  openModal,
})
</script>
