<template>
  <div id="addPicturePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '修改图片' : '创建图片' }}
    </h2>
    <a-typography-paragraphy v-if="spaceId" type="secondary">
      保存至空间: <a :href="`/space/${spaceId}`" target="_blank">{{ spaceId }}</a>
    </a-typography-paragraphy>
    <!--选择上传方式-->
    <a-tabs v-model:activeKey="uploadType">
      <a-tab-pane key="file" tab="文件上传">
        <!--图片上传组件-->
        <PictureUpload :picture="picture" :onSuccess="onSuccess" :spaceId="spaceId"
      /></a-tab-pane>
      <a-tab-pane key="url" tab="URL上传" force-render>
        <!--图片URL上传组件-->
        <UrlPictureUpload :picture="picture" :onSuccess="onSuccess" :spaceId="spaceId"
      /></a-tab-pane>
    </a-tabs>
    <div v-if="picture" class="edit-bar">
      <a-space size="middle">
        <a-button :icon="h(EditOutlined)" @click="doEditPicture">编辑图片</a-button>
        <a-button
          v-if="picture.picWidth >= 512 && picture.picHeight >= 512"
          type="primary"
          :icon="h(FullscreenOutlined)"
          @click="doImageOutPainting"
          >AI扩图</a-button
        >
      </a-space>
    </div>
    <!--图片裁剪组件-->
    <ImageCropper
      ref="imageCropperRef"
      :imgUrl="picture?.url"
      :picture="picture"
      :spaceId="spaceId"
      :onSuccess="onCropSuccess"
    />
    <!--AI扩图组件-->
    <ImageOutPainting
      :picture="picture"
      ref="ImageOutPaintingRef"
      :onSuccess="onImageOutPaintingSuccess"
      :spaceId="spaceId"
    />
    <!--图片信息表单-->
    <a-form v-if="picture" layout="vertical" :model="pictureForm" @finish="handleSubmit">
      <a-form-item name="name" label="名称">
        <a-input v-model:value="pictureForm.name" placeholder="请输入名称" allow-clear />
      </a-form-item>
      <a-form-item name="introduction" label="简介">
        <a-textarea
          v-model:value="pictureForm.introduction"
          placeholder="请输入简介"
          :auto-size="{ minRows: 2, maxRows: 5 }"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="分类" name="category">
        <a-auto-complete
          v-model:value="pictureForm.category"
          placeholder="请输入分类"
          allow-clear
          :options="categoryOptions"
        />
      </a-form-item>
      <a-form-item label="标签" name="tags">
        <a-select
          v-model:value="pictureForm.tags"
          mode="tags"
          placeholder="请输入标签"
          allow-clear
          :options="tagOptions"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">
          {{ route.query?.id ? '修改' : '创建' }}
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import PictureUpload from '@/components/PictureUpload.vue'
import UrlPictureUpload from '@/components/UrlPictureUpload.vue'
import { message } from 'ant-design-vue'
import { computed, onMounted, reactive, ref, h } from 'vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { useRoute, useRouter } from 'vue-router'
import ImageCropper from '@/components/ImageCropper.vue'
import ImageOutPainting from '@/components/ImageOutPainting.vue'
import { EditOutlined, FullscreenOutlined } from '@ant-design/icons-vue'
import {
  editPictureUsingPost,
  listPictureTagCategoryUsingGet,
  getPictureVoByIdUsingGet,
} from '@/api/pictureController'
const uploadType = ref<'file' | 'url'>('file')
const router = useRouter()
const picture = ref<API.PictureVo>()
const onSuccess = (newPicture: API.PictureVo) => {
  picture.value = newPicture
  pictureForm.name = newPicture.name
}

const pictureForm = reactive<API.PictureEditRequest>({
  name: '',
  introduction: '',
  tags: [],
})
//  提交表单
const handleSubmit = async (values: any) => {
  const pictureId = picture.value.id
  if (!pictureId) {
    return
  }
  const res = await editPictureUsingPost({
    spaceId: spaceId.value,
    id: pictureId,
    ...values,
  })
  if (res.data.code === 0 && res.data.data) {
    message.success('创建成功')
    router.push({
      path: `/picture/${pictureId}`,
    })
  } else {
    message.error('创建失败' + res.data.message)
  }
}

const tagOptions = ref<string[]>([])
const categoryOptions = ref<string[]>([])
//  获取标签和分类选项
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('创建失败' + res.data.message)
  }
}
onMounted(() => {
  getTagCategoryOptions()
})
const route = useRoute()
const spaceId = computed(() => {
  return route.query?.spaceId
})
const getOldPicture = async () => {
  //  获取id
  const id = route.query?.id
  if (id) {
    const res = await getPictureVoByIdUsingGet({
      id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      picture.value = data
      pictureForm.name = data.name
      pictureForm.category = data.category
      pictureForm.tags = data.tags
      pictureForm.introduction = data.introduction
      console.log(res.data.data)
    }
  }
}
onMounted(() => {
  getOldPicture()
})

//  图片编辑器引用
const imageCropperRef = ref()

//  编辑图片
const doEditPicture = async () => {
  imageCropperRef.value?.openModal()
}

//  编辑成功事件
const onCropSuccess = (newPicture: API.PictureVo) => {
  picture.value = newPicture
}

//  AI扩图引用
const ImageOutPaintingRef = ref()

//  AI扩图弹窗
const doImageOutPainting = async () => {
  ImageOutPaintingRef.value?.openModal()
}

//  AI扩图成功事件
const onImageOutPaintingSuccess = (newPicture: API.PictureVo) => {
  picture.value = newPicture
}
</script>

<style scoped>
#addPicturePage {
  max-width: 720px;
  margin: 0 auto;
}
#addPicturePage .edit-bar {
  text-align: center;
  margin: 16px 0;
}
</style>
