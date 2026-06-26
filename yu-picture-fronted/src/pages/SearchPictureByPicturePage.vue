<template>
  <div id="searchPictureByPicturePage">
    <h2 style="margin-bottom: 16px">以图搜图</h2>
    <h3 style="margin-bottom: 16px">原图</h3>

    <a-card hoverable style="width: 240px; margin: 16px 0">
      <template #cover>
        <img
          :alt="picture.name"
          :src="picture.url ?? picture.thumbnailUrl"
          style="height: 180px; object-fit: cover"
        />
      </template>
    </a-card>
    <h3 style="margin-bottom: 16px">识图结果</h3>
    <!--图片列表-->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <a :href="picture.thumbUrl" target="_blank">
            <!--单张图片-->
            <a-card hoverable>
              <template #cover>
                <img
                  :alt="picture.name"
                  :src="picture?.thumbUrl"
                  style="height: 180px; object-fit: cover"
                />
              </template>
            </a-card>
          </a>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
import { h, computed, onMounted, reactive, ref } from 'vue'
import { getPictureVoByIdUsingGet, searchPictureByPictureUsingPost } from '@/api/PictureController'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { an } from 'vue-router/dist/router-CWoNjPRp.mjs'
import { formatSize, downloadImage } from '@/utils/index.ts'
import { EditOutlined, DeleteOutlined, DownloadOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { useRoute, useRouter } from 'vue-router'

const picture = ref<API.PictureVo>({})
const route = useRoute()
const loading = ref(true)
const pictureId = computed(() => {
  return route.query?.pictureId
})
//  获取图片详情
const fetchPictureDetail = async () => {
  try {
    const res = await getPictureVoByIdUsingGet({
      id: pictureId.value,
    })
    if (res.data.code === 0 && res.data.data) {
      picture.value = res.data.data
    } else {
      message.error('获取图片失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取图片失败:' + e.message)
  }
}
//  页面加载时获取数据
onMounted(() => {
  fetchPictureDetail()
})

const dataList = ref<API.ImageSearchResult[]>([])
//  获取搜图结果
const fetchResultData = async () => {
  loading.value = true
  try {
    const res = await searchPictureByPictureUsingPost({
      pictureId: pictureId.value,
    })
    if (res.data.code === 0 && res.data.data) {
      dataList.value = res.data.data ?? []
    } else {
      message.error('获取数据失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取数据失败:' + e.message)
  }
  loading.value = false
}
//  页面加载时获取数据
onMounted(() => {
  fetchResultData()
})
</script>

<style scoped>
#searchPictureByPicturePage {
  margin-bottom: 16px;
}
</style>
