<template>
  <a-modal
    class="image-cropper"
    v-model:visible="visible"
    title="图片编辑"
    :footer="false"
    @cancle="closeModal"
  >
    <!--图片裁切组件-->
    <vue-cropper
      ref="cropperRef"
      :img="imgUrl"
      output-type="png"
      :info="true"
      :can-move-box="true"
      :fixed-box="false"
      :auto-crop="true"
      :center-box="true"
    />
    <div style="margin-bottom: 16px" />
    <div class="image-cropper-actions">
      <a-space>
        <a-button @click="changeScale(1)">放大</a-button>
        <a-button @click="changeScale(-1)">缩小</a-button>
        <a-button @click="rotateLeft">左旋</a-button>
        <a-button @click="rotateRight">右旋</a-button>
        <a-button type="primary" :loading="loading" @click="handleConfirm">确认</a-button>
      </a-space>
    </div>
    <div style="margin-bottom: 16px" />
  </a-modal>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { uploadPictureUsingPost } from '@/api/pictureController'
import { message } from 'ant-design-vue'
import { b } from 'vue-router/dist/router-CWoNjPRp.mjs'

interface Props {
  imgUrl?: string
  spaceId?: Number
  picture?: API.PictureVo
  onSuccess?: (newPicture: API.PictureVo) => void
}
const props = defineProps<Props>()

//  获取图片裁切器的引用
const cropperRef = ref()

//  缩放比例
const changeScale = (num) => {
  cropperRef.value?.changeScale(num)
}
const rotateLeft = () => {
  cropperRef.value?.rotateLeft()
}
const rotateRight = () => {
  cropperRef.value?.rotateRight()
}

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
const handleConfirm = () => {
  cropperRef.value.getCropBlob((blob: Blob) => {
    // blob为已裁剪好的文件
    const fileName = (props.picture?.name || 'image') + '.png'
    const file = new File([blob], fileName, { type: blob.type })
    //  上传文件
    handleUpload({ file })
  })
}

const loading = ref(false)
const handleUpload = async ({ file }: any) => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = props.picture ? { id: props.picture.id } : {}
    params.spaceId = props.spaceId
    const res = await uploadPictureUsingPost(params, {}, file)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功！')
      // 将上传成功的图片信息递交给父组件
      props.onSuccess?.(res.data.data)
      closeModal()
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
    console.error('图片上传失败', error)
    message.error('图片上传失败，' + error.message)
  }
  loading.value = false
}
</script>

<style>
.image-cropper {
  text-align: center;
}
.image-cropper .vue-cropper {
  height: 400px !important;
}
</style>
