<template>
  <div id="homePage">
    <!--图片列表-->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <!--单张图片-->
          <a-card hoverable @click="doClickPicture(picture)">
            <template #cover>
              <img
                :alt="picture.name"
                :src="picture.thumbnailUrl ?? picture.url"
                style="height: 180px; object-fit: cover"
              />
            </template>
            <a-card-meta :title="picture.name">
              <template #description>
                <a-flex>
                  <a-tag color="green">
                    {{ picture.category ?? '默认' }}
                  </a-tag>
                  <a-tag v-for="tag in picture.tags" :key="tag">
                    {{ tag }}
                  </a-tag>
                </a-flex>
              </template>
            </a-card-meta>
            <template #actions v-if="showOp">
              <ShareAltOutlined @click="(e) => doShare(picture, e)" />

              <SearchOutlined @click="(e) => doSearch(picture, e)" />

              <EditOutlined @click="(e) => doEdit(picture, e)" />

              <DeleteOutlined @click="(e) => doDelete(picture, e)" />
            </template>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
    <ShareModal ref="shareModalRef" :link="shareLink" />
  </div>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { useRoute, useRouter } from 'vue-router'
import { deletePictureUsingPost } from '@/api/PictureController'
import ShareModal from '@/components/ShareModal.vue'
import {
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ShareAltOutlined,
} from '@ant-design/icons-vue'
import { onMounted, reactive, ref } from 'vue'
//  定义数据
interface Props {
  dataList?: API.PictureVo[]
  loading?: boolean
  showOp?: boolean
  onReload?: () => void
}
const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOp: false,
})
const router = useRouter()

//  跳转至图片详情页
const doClickPicture = (picture: API.PictureVo) => {
  router.push({
    path: `/picture/${picture.id}`,
  })
}

//  以图搜图
const doSearch = (picture, e) => {
  //  阻止冒泡（外层卡片也绑定了编辑事件）
  e.stopPropagation()
  //  打开新的页面
  window.open(`/search_picture?pictureId=${picture.id}`)
}

//  跳转到编辑页面
const doEdit = (picture, e) => {
  //  阻止冒泡（外层卡片也绑定了编辑事件）
  e.stopPropagation()
  //  跳转时要携带spaceId
  router.push({
    path: '/add_picture',
    query: {
      id: picture.id,
      spaceId: picture.spaceId,
    },
  })
}

//  删除数据
const doDelete = async (picture, e) => {
  //  阻止冒泡
  e.stopPropagation()
  const id = picture.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0) {
    props.onReload?.()
    message.success('删除成功！')
  } else {
    message.error('删除失败')
  }
}

//  分享操作
const shareModalRef = ref()
//  分享链接
const shareLink = ref<string>()
//  分享
const doShare = (picture, e) => {
  //  阻止冒泡
  e.stopPropagation()
  // shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.id}`
  shareLink.value = picture.url
  alert(picture.url)
  // shareLink.value = picture.url
  if (shareModalRef.value) {
    shareModalRef.value.openModal()
  }
}
</script>

<style scoped></style>
