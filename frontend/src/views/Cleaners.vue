<template>
  <el-card shadow="hover">
    <template #header>
      <span>保洁人员列表</span>
      <el-button type="primary" size="small" @click="refreshData">刷新</el-button>
    </template>
    <el-table :data="cleaners" border stripe>
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column prop="phone" label="电话" width="140" />
      <el-table-column prop="areas" label="负责区域" width="200">
        <template #default="scope">
          {{ scope.row.areas?.join(', ') }}
        </template>
      </el-table-column>
      <el-table-column prop="skills" label="技能" width="200">
        <template #default="scope">
          {{ scope.row.skills?.join(', ') }}
        </template>
      </el-table-column>
      <el-table-column prop="score" label="评分" width="180">
        <template #default="scope">
          <div style="display: flex; align-items: center; gap: 8px">
            <el-rate v-model="scope.row.score" disabled :max="5" />
            <span style="color: #666; font-weight: bold">{{ scope.row.score?.toFixed(1) }}</span>
            <el-tag
              v-if="getScoreChange(scope.row.id) < 0"
              type="danger"
              size="small"
            >
              {{ getScoreChange(scope.row.id).toFixed(1) }}
            </el-tag>
            <el-tag
              v-else-if="getScoreChange(scope.row.id) > 0"
              type="success"
              size="small"
            >
              +{{ getScoreChange(scope.row.id).toFixed(1) }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="返工次数" width="100">
        <template #default="scope">
          <el-tag :type="getReworkCount(scope.row.id) > 0 ? 'danger' : 'info'" size="small">
            {{ getReworkCount(scope.row.id) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="任务转移" width="100">
        <template #default="scope">
          <el-tag :type="getTransferOutCount(scope.row.id) > 0 ? 'warning' : 'info'" size="small">
            转出{{ getTransferOutCount(scope.row.id) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="taskCount" label="进行中" width="100" />
      <el-table-column prop="isOnLeave" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.isOnLeave ? 'info' : 'success'">
            {{ scope.row.isOnLeave ? '请假' : '在岗' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="scope">
          <el-button
            :type="scope.row.isOnLeave ? 'success' : 'warning'"
            size="small"
            @click="toggleLeave(scope.row.id, !scope.row.isOnLeave)"
          >
            {{ scope.row.isOnLeave ? '销假' : '请假' }}
          </el-button>
          <el-button
            type="info"
            size="small"
            @click="viewCleanerTasks(scope.row)"
          >
            任务记录
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="showTaskDialog" :title="'保洁员 ' + currentCleaner?.name + ' 的任务记录'" width="900px">
    <el-alert
      v-if="getReworkCount(currentCleaner?.id) > 0"
      :title="'该保洁员已有 ' + getReworkCount(currentCleaner?.id) + ' 次质检不合格记录'"
      type="warning"
      :closable="false"
      style="margin-bottom: 20px"
    />
    <el-table :data="cleanerTasks" border stripe>
      <el-table-column prop="id" label="任务ID" width="180" />
      <el-table-column label="房间" width="100">
        <template #default="scope">
          {{ getRoomNumber(scope.row.roomId) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="质检状态" width="100">
        <template #default="scope">
          <span v-if="scope.row.status === '待质检'">
            <el-tag type="warning">待质检</el-tag>
          </span>
          <span v-else-if="scope.row.status === '待返工'">
            <el-tag type="danger">需返工</el-tag>
          </span>
          <span v-else-if="scope.row.status === '已完成'">
            <el-tag type="success">已质检</el-tag>
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="质检结果" width="200">
        <template #default="scope">
          <span v-for="ins in getTaskInspections(scope.row.id)" :key="ins.id" style="display: block; margin-bottom: 4px">
            <el-tag :type="ins.result === '合格' ? 'success' : 'danger'" size="small">
              {{ ins.result }} - {{ ins.inspector }}
            </el-tag>
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
    </el-table>
    <template #footer>
      <el-button @click="showTaskDialog = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import api from '../api'

const cleaners = ref([])
const tasks = ref([])
const rooms = ref([])
const inspections = ref([])
const assignmentHistories = ref([])
const showTaskDialog = ref(false)
const currentCleaner = ref(null)
const cleanerTasks = ref([])

const loadCleaners = async () => {
  try {
    const res = await api.get('/cleaners')
    cleaners.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const loadTasks = async () => {
  try {
    const res = await api.get('/tasks')
    tasks.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const loadRooms = async () => {
  try {
    const res = await api.get('/rooms')
    rooms.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const loadInspections = async () => {
  try {
    const res = await api.get('/inspections')
    inspections.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const loadAssignmentHistories = async () => {
  try {
    const res = await api.get('/assignment-histories')
    assignmentHistories.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const refreshData = () => {
  loadCleaners()
  loadTasks()
  loadRooms()
  loadInspections()
  loadAssignmentHistories()
}

const getRoomNumber = (roomId) => {
  const room = rooms.value.find(r => r.id === roomId)
  return room ? room.roomNumber : roomId
}

const getStatusType = (status) => {
  const map = {
    '待分配': 'info',
    '已分配': 'primary',
    '待质检': 'warning',
    '待返工': 'danger',
    '已完成': 'success'
  }
  return map[status] || ''
}

const getReworkCount = (cleanerId) => {
  return inspections.value.filter(i => {
    const task = tasks.value.find(t => t.id === i.taskId)
    return task && task.cleanerId === cleanerId && i.result === '不合格'
  }).length
}

const getScoreChange = (cleanerId) => {
  const reworkCount = getReworkCount(cleanerId)
  const successCount = inspections.value.filter(i => {
    const task = tasks.value.find(t => t.id === i.taskId)
    return task && task.cleanerId === cleanerId && i.result === '合格'
  }).length
  return successCount * 0.1 - reworkCount * 0.2
}

const getTransferOutCount = (cleanerId) => {
  return assignmentHistories.value.filter(h => h.previousCleanerId === cleanerId).length
}

const getTaskInspections = (taskId) => {
  return inspections.value.filter(i => i.taskId === taskId)
}

const viewCleanerTasks = async (cleaner) => {
  currentCleaner.value = cleaner
  await loadTasks()
  await loadInspections()
  cleanerTasks.value = tasks.value.filter(t => t.cleanerId === cleaner.id)
  showTaskDialog.value = true
}

const toggleLeave = async (cleanerId, isOnLeave) => {
  try {
    await api.post(`/cleaners/${cleanerId}/leave?isOnLeave=${isOnLeave}`)
    ElMessage.success(isOnLeave ? '已设置请假，任务已重新分配' : '已销假')
    refreshData()
  } catch (e) {
    console.error(e)
  }
}

onMounted(() => {
  refreshData()
})
</script>

<style scoped>
</style>