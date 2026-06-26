<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout>
        <GlobalSider class="sider" />
        <a-layout-content class="content">
          <router-view />
        </a-layout-content>
      </a-layout>

      <a-layout-footer class="footer">
        <a href="https://zshen.chousan.top" target="_blank"> <h2>zshen的网站</h2> </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>
<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalSider from '@/components/GlobalSider.vue'
</script>

<style scoped>
#basicLayout .content {
  padding: 15px;
  background: linear-gradient(to right, #fefefe, #fff);
}
#basicLayout .sider {
  background: #fff;
  border-right: 0.5px solid #eee;
  padding-top: 16px;
}
#basicLayout :deep(.ant-menu-root) {
  border-bottom: none !important;
  border-inline-end: none !important;
}
#basicLayout .footer {
  background: #efefef;
  text-align: center;
}
#basicLayout .header {
  background: white;
  text-align: center;
  color: unset;
  margin-bottom: 1px;
}
</style>
