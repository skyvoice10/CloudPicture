import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUserUsingGet } from '@/api/userController'

/**
 * 存储用户登录信息的store
 */

export const useLoginUserStore = defineStore('loginUser', () => {
  const loginUser = ref<API.LoginUserVo>({
    username: '未登录',
  })
  /**
   * 远程获取用户登录信息
   */
  async function fetchLoginUser() {
    //  todo
    const res = await getLoginUserUsingGet()
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
    }

    // //  测试用户登录，3秒后自动登录
    // setTimeout(() => {
    //   loginUser.value = { username: '测试用户', id: 1 }
    // }, 3000)
  }
  /**
   * 设置登录用户
   */
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }
  //  返回
  return { loginUser, fetchLoginUser, setLoginUser }
})
