<template>
  <div id="mySpacePage">
    <p>正在跳转，请稍后....</p>
  </div>
</template>

<script setup lang="ts">
import { message } from 'ant-design-vue'
import { onMounted, reactive, ref } from 'vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { useRoute, useRouter } from 'vue-router'
import { listSpaceVoPageUsingPost } from '@/api/spaceController'
const router = useRouter()
const loginUserStore = useLoginUserStore()
// 检查用户是否有个人空间
const checkUserSpace = async () => {
  //  用户未登录，则直接跳转到登录页面
  const loginUser = loginUserStore.loginUser
  if (!loginUser?.id) {
    router.replace('/user/login')
    return
  }
  //  如果用户已经登录，会获取用户已创建的空间
  const res = await listSpaceVoPageUsingPost({
    userId: loginUser.id,
    current: 1,
    pageSize: 1,
  })
  if (res.data.code === 0) {
    //  如果有则进入第一个空间
    if (res.data.data?.records?.length > 0) {
      const space = res.data.data.records[0]
      router.replace(`/space/${space.id}`)
    } else {
      //  如果没有则跳转到创建空间页面
      router.replace('/add_space')
      message.warn('请先创建空间')
    }
  } else {
    message.error('加载我的空间失败，' + res.data.message)
  }
}
onMounted(() => {
  checkUserSpace()
})
</script>

<style scoped>
#mySpacePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
