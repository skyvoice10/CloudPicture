<template>
  <div id="spaceDetailPage">
    <!--空间信息-->
    <a-flex justify="space-between">
      <h2>{{ space.spaceName }}({{ SPACE_TYPE_MAP[space.spaceType] }})</h2>
      <a-space size="middle">
        <a-tooltip
          :title="`占用空间 ${formatSize(space.totalSize)} / ${formatSize(space.maxSize)}`"
        >
          <a-progress
            type="circle"
            :size="42"
            :percent="((space.totalSize * 100) / space.maxSize).toFixed(1)"
          />
        </a-tooltip>

        <a-button
          type="primary"
          ghost
          :icon="h(TeamOutlined)"
          :href="`/spaceUserManage/${id}`"
          target="_blank"
        >
          成员管理
        </a-button>
        <a-button type="primary" :href="`/add_picture?spaceId=${id}`" target="_blank">
          +创建图片
        </a-button>
        <a-button
          :icon="h(BarChartOutlined)"
          type="primary"
          ghost
          :href="`/space_analyze?spaceId=${id}`"
          target="_blank"
        >
          +空间分析
        </a-button>
        <a-button :icon="h(EditOutlined)" @click="doBatchEdit"> 批量编辑图片 </a-button>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px" />
    <!--搜索框-->
    <PictureSearchForm :onSearch="onSearch" />
    <div style="margin-bottom: 16px" />
    <a-form-item label="按颜色搜索">
      <color-picker format="hex" @pureColorChange="onColorChange" />
    </a-form-item>
    <div style="margin-bottom: 16px" />
    <!--图片列表-->
    <PictureList :dataList="dataList" :loading="loading" :showOp="true" :onReload="fetchData" />
    <a-pagination
      style="text-align: right"
      v-model:current="searchParams.current"
      v-model:pageSize="searchParams.pageSize"
      :total="total"
      @change="onPageChange"
    />

    <BatchEditPictureModal
      ref="batchEditPictureModalRef"
      :spaceId="id"
      :pictureList="dataList"
      :onSuccess="onBatchEditPictureSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { EditOutlined, BarChartOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { h, computed, onMounted, reactive, ref, watch } from 'vue'
import { getSpaceVoByIdUsingGet, deleteSpaceUsingPost } from '@/api/SpaceController'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { an } from 'vue-router/dist/router-CWoNjPRp.mjs'
import { formatSize } from '@/utils/index.ts'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { useRoute, useRouter } from 'vue-router'
import {
  listPictureVoPageUsingPost,
  listPictureTagCategoryUsingGet,
  searchPictureByColorUsingPost,
} from '@/api/PictureController'
import PictureList from '@/components/PictureList.vue'
import BatchEditPictureModal from '@/components/BatchEditPictureModal.vue'
import PictureSearchForm from '@/components/PictureSearchForm.vue'
import { ColorPicker } from 'vue3-colorpicker'
import 'vue3-colorpicker/style.css'
import { SPACE_TYPE_ENUM, SPACE_TYPE_MAP } from '@/constants/spaceuser.ts'

//-----------------获取空间列表------------------
interface Props {
  id: string | number
}
const props = defineProps<Props>()
const space = ref<API.SpaceVo>({})
//  获取空间详情
const fetchSpaceDetail = async () => {
  try {
    const res = await getSpaceVoByIdUsingGet({
      id: props.id,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    } else {
      message.error('获取空间详情失败，' + res.data.message)
    }
  } catch (e: any) {
    message.error('获取空间详情失败:' + e.message)
  }
}

//  页面加载时获取数据
onMounted(() => {
  fetchSpaceDetail()
})
//-----------------获取图片列表------------------
//  定义数据
const dataList = ref<API.PictureVo[]>([])
const loading = ref(true)
const total = ref(0)
//  搜索条件
const searchParams = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 12,
  sortField: 'createTime',
  sortOrder: 'descend',
})

//  获取数据
const fetchData = async () => {
  loading.value = true
  //  转换搜索参数
  const params = {
    spaceId: props.id,
    ...searchParams.value,
  }

  const res = await listPictureVoPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

//  页面加载时获取数据
onMounted(() => {
  fetchData()
})

//  分页参数
const onPageChange = (page: number, pageSize: number) => {
  searchParams.value.current = page
  searchParams.value.pageSize = pageSize
  fetchData()
}

//  搜索
const onSearch = (newSearchParams: API.PictureQueryRequest) => {
  searchParams.value = {
    ...searchParams.value,
    ...newSearchParams,
    current: 1,
  }
  fetchData()
}

//  按照颜色搜索
const onColorChange = async (color: string) => {
  loading.value = true
  const res = await searchPictureByColorUsingPost({
    picColor: color,
    spaceId: props.id,
  })
  if (res.data.code === 0 && res.data.data) {
    const data = res.data.data ?? []
    dataList.value = data
    total.value = data.length
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
  loading.value = false
}

//  批量编辑图片
const batchEditPictureModalRef = ref()

const onBatchEditPictureSuccess = () => {
  fetchData()
}

//打开批量编辑弹窗
const doBatchEdit = () => {
  if (batchEditPictureModalRef.value) {
    batchEditPictureModalRef.value.openModal()
  }
}

//  切换空间重新获取空间信息和图片列表，用watch来监听空间id的改变
watch(
  () => props.id,
  (newSpaceId) => {
    fetchData()
    fetchSpaceDetail()
  },
)
</script>

<style scoped>
#spaceDetailPage {
  margin-bottom: 16px;
}
</style>
