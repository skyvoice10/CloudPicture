<template>
  <div id="addSpacePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '修改空间' : '创建空间' }} {{ SPACE_TYPE_MAP[spaceType] }}
    </h2>
    <!--空间信息表单-->
    <a-form layout="vertical" :model="spaceForm" @finish="handleSubmit">
      <a-form-item name="spaceName" label="空间名称">
        <a-input v-model:value="spaceForm.spaceName" placeholder="请输入空间名称" allow-clear />
      </a-form-item>
      <a-form-item name="spaceLevel" label="空间级别">
        <a-select
          v-model:value="spaceForm.spaceLevel"
          placeholder="请选择空间级别"
          allow-clear
          style="min-width: 180px"
          :options="SPACE_LEVEL_OPTIONS"
        />
      </a-form-item>
      <!--空间级别介绍-->
      <a-card title="空间级别介绍">
        <a-typography-paragraph>
          *目前仅支持开通普通版，如需升级空间，请联系
          <a href="zshen.chousan.top" target="_blank">z神的网站</a>
        </a-typography-paragraph>
        <a-typography-paragraph v-for="spaceLevel in spaceLevelList">
          {{ spaceLevel.text }}:大小{{ formatSize(spaceLevel.maxSize) }},数量{{
            spaceLevel.maxCount
          }}
        </a-typography-paragraph>
      </a-card>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading">
          提交
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue'
import { computed, onMounted, reactive, ref } from 'vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { useRoute, useRouter } from 'vue-router'
import { formatSize } from '@/utils/index.ts'
import {
  addSpaceUsingPost,
  getSpaceVoByIdUsingGet,
  listSpaceLevelUsingGet,
  updateSpaceUsingPost,
} from '@/api/spaceController'
import { SPACE_LEVEL_MAP, SPACE_LEVEL_ENUM, SPACE_LEVEL_OPTIONS } from '@/constants/space.ts'
import { SPACE_TYPE_ENUM, SPACE_TYPE_MAP } from '@/constants/spaceuser.ts'
const router = useRouter()
const space = ref<API.SpaceVo>()
const loading = ref(false)
const spaceLevelList = ref<API.SpaceLevel[]>([])

//  空间类别，默认为私有空间
const spaceType = computed(() => {
  if (route.query?.type) {
    return Number(route.query.type)
  } else {
    return SPACE_TYPE_ENUM.PRIVATE
  }
})

//  获取空间级别列表
const fetchSpaceLevelList = async () => {
  const res = await listSpaceLevelUsingGet()
  if (res.data.code === 0 && res.data.data) {
    spaceLevelList.value = res.data.data
  } else {
    message.error('获取空间级别失败' + res.data.message)
  }
}

const spaceForm = reactive<API.SpaceAddRequest | API.SpaceEditRequest>({})
//  提交表单
const handleSubmit = async (values: any) => {
  const spaceId = space.value?.id
  loading.value = true
  let res
  if (spaceId) {
    //  更新
    res = await updateSpaceUsingPost({
      id: spaceId,
      ...spaceForm,
    })
  } else {
    //创建
    res = await addSpaceUsingPost({
      ...spaceForm,
      spaceType: spaceType.value,
    })
  }
  if (res.data.code === 0 && res.data.data) {
    message.success('操作成功')
    router.push({
      path: `/space/${res.data.data}`,
    })
  } else {
    message.error('操作失败' + res.data.message)
  }
  loading.value = false
}

const route = useRoute()
const getOldSpace = async () => {
  //  获取id
  const id = route.query?.id
  if (id) {
    const res = await getSpaceVoByIdUsingGet({
      id,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      space.value = data
      spaceForm.spaceName = data.spaceName
      spaceForm.spaceLevel = data.spaceLevel
    }
  }
}
onMounted(() => {
  getOldSpace()
  fetchSpaceLevelList()
})
</script>

<style scoped>
#addSpacePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
