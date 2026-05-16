<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">房间总数</div>
            <div class="stat-value">{{ roomCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">待保洁房间</div>
            <div class="stat-value dirty">{{ dirtyRoomCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">进行中任务</div>
            <div class="stat-value warning">{{ activeTaskCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-label">保洁人员</div>
            <div class="stat-value">{{ cleanerCount }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px" shadow="hover">
      <template #header>
        <span>即将入住提醒</span>
        <el-button type="primary" size="small" @click="scanAlerts">刷新</el-button>
      </template>
      <el-alert
        v-for="(alert, index) in alerts"
        :key="index"
        :title="alert"
        type="warning"
        :closable="false"
        style="margin-bottom: 10px"
      />
      <el-empty v-if="alerts.length === 0" description="暂无即将入住提醒" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '../api'

const roomCount = ref(0)
const dirtyRoomCount = ref(0)
const activeTaskCount = ref(0)
const cleanerCount = ref(0)
const alerts = ref([])

const loadData = async () => {
  try {
    const [rooms, tasks, cleaners] = await Promise.all([
      api.get('/rooms'),
      api.get('/tasks'),
      api.get('/cleaners')
    ])
    roomCount.value = rooms.data.length
    dirtyRoomCount.value = rooms.data.filter(r => !r.isCleaned).length
    activeTaskCount.value = tasks.data.filter(t => t.status === '已分配' || t.status === '待质检').length
    cleanerCount.value = cleaners.data.length
  } catch (e) {
    console.error(e)
  }
}

const scanAlerts = async () => {
  try {
    const res = await api.get('/scan-upcoming')
    alerts.value = res.data
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadData()
  scanAlerts()
})
</script>

<style scoped>
.stat-item {
  text-align: center;
}
.stat-label {
  font-size: 14px;
  color: #999;
  margin-bottom: 10px;
}
.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #1890ff;
}
.stat-value.dirty {
  color: #f5222d;
}
.stat-value.warning {
  color: #faad14;
}
</style>