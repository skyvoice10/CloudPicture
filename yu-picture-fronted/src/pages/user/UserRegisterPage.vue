<template>
  <div id="userRegisterPage">
    <h2 class="title">zshen的图库</h2>
    <div class="desc">企业级智能协同云图库</div>
    <a-form :model="formState" autocomplete="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入用户账号!' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入帐号" />
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码!' },
          { min: '8', message: '密码长度不能小于8位!' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>

      <a-form-item
        name="checkPassword"
        :rules="[
          { required: true, message: '请输入确认密码!' },
          { min: '8', message: '确认密码长度不能小于8位!' },
        ]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="请输入确认密码" />
      </a-form-item>

      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
      </a-form-item>
      <div class="tips">
        已有帐号?
        <RouterLink to="/user/login">去登录</RouterLink>
      </div>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { a } from 'vue-router/dist/router-CWoNjPRp.mjs'
import { userRegisterUsingPost } from '@/api/userController'
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
//  路由跳转事件
const router = useRouter()
const loginUserStore = useLoginUserStore()
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})
//  提交表单
const handleSubmit = async (values: any) => {
  const res = await userRegisterUsingPost(values)
  //  校验两次密码是否输入一致
  if (values.userPassword !== values.checkPassword) {
    message.error('两次密码输入不一致！')
    return
  }
  //  注册成功，跳转到登录页
  if (res.data.code === 0 && res.data.data) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败' + res.data.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
  max-width: 360px;
  margin: 0 auto;
}
.title {
  text-align: center;
  margin-bottom: 16px;
}
.desc {
  text-align: center;
  margin-bottom: 16px;
  color: #bbb;
}
.tips {
  color: #bbb;
  text-align: right;
  font-size: 13px;
  margin-bottom: 16px;
}
</style>
