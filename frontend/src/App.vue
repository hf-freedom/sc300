<template>
  <el-container style="height: 100vh">
    <el-aside width="200px" style="background-color: #2f4050">
      <el-menu
        :default-active="$route.path"
        class="el-menu-vertical"
        router
        background-color="#2f4050"
        text-color="#fff"
        active-text-color="#1890ff"
      >
        <el-menu-item index="/">
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/rooms">
          <span>房间管理</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/cleaners">
          <span>保洁人员</span>
        </el-menu-item>
        <el-menu-item index="/tasks">
          <span>保洁任务</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header style="background-color: #fff; border-bottom: 1px solid #eee; font-size: 20px; font-weight: bold">
        保洁调度系统
      </el-header>
      <el-main style="background-color: #f3f3f4">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
</script>

<style scoped>
</style>