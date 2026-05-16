<template>
  <el-card shadow="hover">
    <template #header>
      <span>房间列表</span>
    </template>
    <el-table :data="rooms" border stripe>
      <el-table-column prop="roomNumber" label="房间号" width="120" />
      <el-table-column prop="area" label="区域" width="120" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isCleaned" label="保洁状态" width="150">
        <template #default="scope">
          <el-tag :type="scope.row.isCleaned ? 'success' : 'danger'">
            {{ scope.row.isCleaned ? '已清洁' : '待清洁' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="scope">
          <el-button
            type="primary"
            size="small"
            @click="checkCanCheckIn(scope.row.id)"
          >
            检查是否可入住
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const rooms = ref([])

const loadRooms = async () => {
  try {
    const res = await api.get('/rooms')
    rooms.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const getStatusType = (status) => {
  const map = {
    '空闲': 'success',
    '待保洁': 'warning',
    '已入住': 'info'
  }
  return map[status] || ''
}

const checkCanCheckIn = async (roomId) => {
  try {
    const res = await api.get(`/rooms/${roomId}/can-checkin`)
    if (res.data.canCheckIn) {
      ElMessage.success('该房间已清洁，可以入住')
    } else {
      ElMessage.error('该房间未清洁，不可入住')
    }
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  loadRooms()
})
</script>

<style scoped>
</style>