<template>
  <div id="spaceAnalyzePage">
    <h2>
      空间图库分析 -
      <span v-if="queryAll">全部空间</span>
      <span v-else-if="queryPublic">公共图库</span>
      <span v-else>
        <a :href="`/space/${spaceId}`" target="_blank">空间id:{{ spaceId }}</a>
      </span>
    </h2>
    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :md="12">
        <SpaceUsageAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic" />
      </a-col>
      <a-col :xs="24" :md="12">
        <SpaceCategoryAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic" />
      </a-col>
      <a-col :xs="24" :md="12">
        <SpaceTagAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic" />
      </a-col>
      <a-col :xs="24" :md="12">
        <SpaceSizeAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic" />
      </a-col>
      <a-col :xs="24" :md="12">
        <SpaceUserAnalyze :spaceId="spaceId" :queryAll="queryAll" :queryPublic="queryPublic" />
      </a-col>
      <a-col :xs="24" :md="12">
        <SpaceRankAnalyze
          v-if="isAdmin"
          :spaceId="spaceId"
          :queryAll="queryAll"
          :queryPublic="queryPublic"
        />
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { h, computed, onMounted, reactive, ref } from 'vue'
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
import SpaceUsageAnalyze from '@/components/analyze/SpaceUsageAnalyze.vue'
import SpaceCategoryAnalyze from '@/components/analyze/SpaceCategoryAnalyze.vue'
import SpaceTagAnalyze from '@/components/analyze/SpaceTagAnalyze.vue'
import SpaceSizeAnalyze from '@/components/analyze/SpaceSizeAnalyze.vue'
import SpaceUserAnalyze from '@/components/analyze/SpaceUserAnalyze.vue'
import SpaceRankAnalyze from '@/components/analyze/SpaceRankAnalyze.vue'

import { ColorPicker } from 'vue3-colorpicker'
import 'vue3-colorpicker/style.css'

const route = useRoute()
//  空间id
const spaceId = computed(() => {
  return route.query?.spaceId as string
})
//  是否查询所有空间
const queryAll = computed(() => {
  return !!route.query?.queryAll
})
//  是否查询公共空间
const queryPublic = computed(() => {
  return !!route.query?.queryPublic
})

//  判断用户是否为管理员
const loginUserStore = useLoginUserStore()
const loginUser = loginUserStore.loginUser
const isAdmin = computed(() => {
  return loginUser.userRole === 'admin'
})
</script>

<style scoped></style>
