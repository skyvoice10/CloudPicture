<template>
  <div id="globalSider">
    <a-layout-sider
      width="200"
      v-if="loginUserStore.loginUser.id"
      breakpoint="lg"
      collapsed-width="0"
    >
      <a-menu
        v-model:selectedKeys="current"
        mode="inline"
        :style="{ height: '100%', borderRight: 0 }"
        :items="menuItems"
        @click="doMenuClick"
      >
      </a-menu>
    </a-layout-sider>
  </div>
</template>
<script lang="ts" setup>
import { useLoginUserStore } from '@/stores/useLoginUserStore.ts'
import { useRouter } from 'vue-router'
import { computed, h, ref, watchEffect } from 'vue'
import { PictureOutlined, UserOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { MenuProps, message } from 'ant-design-vue'
import { userLogoutUsingPost } from '@/api/userController'
import { listMyTeamSpaceUsingPost } from '@/api/spaceUserController'
import { SPACE_TYPE_ENUM, SPACE_TYPE_MAP } from '@/constants/spaceuser.ts'

// 固定菜单列表
const fixedMenuItems = [
  {
    key: '/',
    icon: () => h(PictureOutlined),
    label: '公共图库',
  },
  {
    key: '/my_space',
    label: '我的空间',
    icon: () => h(UserOutlined),
  },
  {
    key: '/add_space?type=' + SPACE_TYPE_ENUM.TEAM,
    label: '创建团队',
    icon: () => h(TeamOutlined),
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
const items = computed(() => filterMenus(fixedMenuItems))
const loginUserStore = useLoginUserStore()

const teamSpaceList = ref<API.SpaceUserVo[]>([])
const menuItems = computed(() => {
  //  如果用户没有团队空间，则只展示固定菜单
  if (teamSpaceList.value.length < 1) {
    return fixedMenuItems
  }
  //  如果用户有团队空间，则展示固定菜单和团队空间菜单
  const teamSpaceSubMenus = teamSpaceList.value.map((spaceUser) => {
    const space = spaceUser.space
    return {
      key: '/space/' + spaceUser.spaceId,
      label: space?.spaceName,
    }
  })
  const teamSpaceMenuGroup = {
    type: 'group',
    label: '我的团队',
    key: 'teamSpace',
    children: teamSpaceSubMenus,
  }
  return [...fixedMenuItems, teamSpaceMenuGroup]
})

//  加载团队空间列表
const fetchTeamSpaceList = async () => {
  const res = await listMyTeamSpaceUsingPost()
  if (res.data.code === 0 && res.data.data) {
    teamSpaceList.value = res.data.data
  } else {
    message.error('加载我的团队空间失败,' + res.data.message)
  }
}

/**
 * 监听变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  //  登录才加载
  if (loginUserStore.loginUser.id) {
    fetchTeamSpaceList()
  }
})
//  路由跳转事件
const router = useRouter()

const doMenuClick = ({ key }) => {
  router.push(key)
}

//  当前要高亮的菜单项
const current = ref<string[]>([])
//  监听路由变化，更新高亮菜单项
router.afterEach((to, from, next) => {
  current.value = [to.path]
})
</script>
<style scoped>
#globalSider .ant-layout-sider {
  background: none;
}
</style>
