<template>
  <div class="batch-edit-picture-modal">
    <a-modal v-model:visible="visible" title="批量编辑图片" :footer="false" @cancle="closeModal">
      <a-typography-paragraph type="secondary"> *只对当前页面的图片生效 </a-typography-paragraph>
      <div style="margin-bottom: 16px" />
      <!--图片信息表单-->
      <a-form layout="vertical" :model="formData" @finish="handleSubmit">
        <a-form-item label="分类" name="category">
          <a-auto-complete
            v-model:value="formData.category"
            placeholder="请输入分类"
            allow-clear
            :options="categoryOptions"
          />
        </a-form-item>
        <a-form-item label="标签" name="tags">
          <a-select
            v-model:value="formData.tags"
            mode="tags"
            placeholder="请输入标签"
            allow-clear
            :options="tagOptions"
          />
        </a-form-item>
        <a-form-item name="nameRule" label="命名规则">
          <a-input
            v-model:value="formData.nameRule"
            placeholder="请输入命名规则，输入{序号}可动态生成"
            allow-clear
          />
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit" style="width: 100%"> 提交 </a-button>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script lang="ts" setup>
import { addAbortSignal } from 'stream'
import { onMounted, reactive, ref } from 'vue'
import { st } from 'vue-router/dist/router-CWoNjPRp.mjs'
import {
  listPictureTagCategoryUsingGet,
  editPictureByBatchUsingPost,
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
const open = ref<boolean>(false)
const formData = reactive({
  category: '',
  nameRule: '',
  tags: [],
})

interface Props {
  pictureList: API.PictureVo[]
  spaceId: number
  onSuccess: () => void
}
const props = withDefaults(defineProps<Props>(), {})

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

//提交表单
const handleSubmit = async (values: any) => {
  if (!props.pictureList) {
    return
  }
  const res = await editPictureByBatchUsingPost({
    pictureIdList: props.pictureList.map((picture) => picture.id),
    spaceId: props.spaceId,
    ...values,
  })
  if (res.data.code === 0 && res.data.data) {
    message.success('操作成功')
    closeModal()
    props.onSuccess?.(formData.category, formData.tags)
  } else {
    message.error('操作失败' + res.data.message)
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
</script>
