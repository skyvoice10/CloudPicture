<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="200px">
        <router-link to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/logo.png" alt="logo" />
            <div class="title">zshen的图库</div>
          </div>
        </router-link>
      </a-col>
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>
      <!-- 用户信息展示栏-->
      <a-col flex="120px">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" :width="20" />
                {{ loginUserStore.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                  <a-menu-item>
                    <router-link to="my_space">
                      <UserOutlined />
                      我的空间
                    </router-link>
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { useRouter } from 'vue-router'
import { computed, h, ref } from 'vue'
import { CrownOutlined, LogoutOutlined, UserOutlined } from '@ant-design/icons-vue'
import { MenuProps, message } from 'ant-design-vue'
import { userLogoutUsingPost } from '@/api/userController'

//  原始菜单
const originalItems = [
  {
    key: '/',
    icon: () => h(CrownOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/pictureManage',
    label: '图片管理',
    title: '图片管理',
  },
  {
    key: '/add_picture',
    label: '创建图片',
    title: '创建图片',
  },
  {
    key: '/admin/spaceManage',
    label: '空间管理',
    title: '图片管理',
  },
  {
    key: 'others',
    label: h('a', { href: 'https://zshen.chousan.top', target: '_blank' }, 'zshen的网站'),
    title: '关于',
  },
]

//  根据权限过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    //  管理员才能看到/adimn开头的菜单
    if (menu?.key?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}
//  经过过滤的菜单
const items = computed(() => filterMenus(originalItems))
const loginUserStore = useLoginUserStore()

//  路由跳转事件
const router = useRouter()

const doMenuClick = ({ key }) => {
  router.push({
    path: key,
  })
}

//  退出登录事件
const doLogout = async () => {
  const res = await userLogoutUsingPost()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    message.success('退出登录成功！')
    router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}

//  当前要高亮的菜单项
const current = ref<string[]>([])
//  监听路由变化，更新高亮菜单项
router.afterEach((to, from, next) => {
  current.value = [to.path]
})
</script>
<style>
#globalHeader .title-bar {
  display: flex;
  align-items: center;
}
.ant-layout-header {
  padding-inline: 10px !important;
}

#globalHeader .logo {
  width: 70px;
}
#globalHeader .title {
  margin-left: 8px;
  font-size: 20px;
  font-weight: bolder;
  color: black;
}
</style>
