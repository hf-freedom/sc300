<template>
  <el-card shadow="hover">
    <template #header>
      <span>保洁任务列表</span>
      <el-button type="primary" size="small" @click="refreshData">刷新</el-button>
    </template>
    <el-table :data="tasks" border stripe>
      <el-table-column prop="id" label="任务ID" width="180" />
      <el-table-column label="房间信息" width="120">
        <template #default="scope">
          {{ getRoomNumber(scope.row.roomId) }}
        </template>
      </el-table-column>
      <el-table-column label="保洁人员" width="120">
        <template #default="scope">
          {{ getCleanerName(scope.row.cleanerId) || '待分配' }}
        </template>
      </el-table-column>
      <el-table-column prop="priority" label="优先级" width="100">
        <template #default="scope">
          <el-tag :type="scope.row.priority > 1 ? 'danger' : 'info'">
            {{ scope.row.priority > 1 ? '高' : '普通' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="质检状态" width="120">
        <template #default="scope">
          <span v-if="scope.row.status === '已完成'">
            <el-tag type="success">已质检</el-tag>
          </span>
          <span v-else-if="scope.row.status === '待质检'">
            <el-tag type="warning">待质检</el-tag>
          </span>
          <span v-else-if="scope.row.status === '待返工'">
            <el-tag type="danger">需返工</el-tag>
          </span>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="返工次数" width="100">
        <template #default="scope">
          <el-tag
            v-if="getReworkCount(scope.row.id) > 0"
            type="danger"
            size="small"
          >
            {{ getReworkCount(scope.row.id) }}次
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="人员变更" width="100">
        <template #default="scope">
          <el-tag
            v-if="getReassignCount(scope.row.id) > 0"
            type="warning"
            size="small"
          >
            {{ getReassignCount(scope.row.id) }}次
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="400">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === '待分配'"
            type="primary"
            size="small"
            @click="assignTask(scope.row.id)"
          >
            分配
          </el-button>
          <el-button
            v-if="scope.row.status === '已分配'"
            type="success"
            size="small"
            @click="completeTask(scope.row.id)"
          >
            完成保洁
          </el-button>
          <el-button
            v-if="scope.row.status === '待返工'"
            type="warning"
            size="small"
            @click="reworkTask(scope.row.id)"
          >
            返工
          </el-button>
          <el-button
            v-if="scope.row.status === '待质检'"
            type="warning"
            size="small"
            @click="showInspectDialog(scope.row)"
          >
            质检
          </el-button>
          <el-button
            type="info"
            size="small"
            @click="viewTaskDetail(scope.row)"
          >
            详情
          </el-button>
          <el-button
            v-if="scope.row.status === '已完成'"
            type="success"
            size="small"
            @click="viewInspectionHistory(scope.row.id)"
          >
            质检记录
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="showInspectDialogVisible" title="质量检查" width="600px">
    <el-alert
      title="正在对任务进行质检"
      :description="'房间: ' + getRoomNumber(currentTask?.roomId) + ', 保洁人员: ' + getCleanerName(currentTask?.cleanerId)"
      type="info"
      :closable="false"
      style="margin-bottom: 20px"
    />
    <el-form :model="inspectForm" label-width="80px">
      <el-form-item label="质检结果">
        <el-radio-group v-model="inspectForm.result">
          <el-radio label="合格" border>
            <el-icon style="color: #67C23A"><CircleCheck /></el-icon>
            合格
          </el-radio>
          <el-radio label="不合格" border>
            <el-icon style="color: #F56C6C"><CircleClose /></el-icon>
            不合格
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="评分影响" v-if="inspectForm.result === '不合格'">
        <el-alert type="warning" :closable="false">
          质检不合格将扣除保洁人员评分 -0.2分
        </el-alert>
      </el-form-item>
      <el-form-item label="评分影响" v-else>
        <el-alert type="success" :closable="false">
          质检合格将增加保洁人员评分 +0.1分
        </el-alert>
      </el-form-item>
      <el-form-item label="质检备注">
        <el-input v-model="inspectForm.remarks" type="textarea" :rows="3" placeholder="请输入质检备注" />
      </el-form-item>
      <el-form-item label="质检员">
        <el-input v-model="inspectForm.inspector" placeholder="请输入质检员姓名" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showInspectDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="submitInspection" :loading="inspectLoading">
        提交质检结果
      </el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showTaskDetailVisible" title="任务详情" width="800px">
    <el-descriptions :column="2" border v-if="currentTask">
      <el-descriptions-item label="任务ID">{{ currentTask.id }}</el-descriptions-item>
      <el-descriptions-item label="房间号">{{ getRoomNumber(currentTask.roomId) }}</el-descriptions-item>
      <el-descriptions-item label="保洁人员">
        {{ getCleanerName(currentTask.cleanerId) || '待分配' }}
      </el-descriptions-item>
      <el-descriptions-item label="任务状态">
        <el-tag :type="getStatusType(currentTask.status)">{{ currentTask.status }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="优先级">
        <el-tag :type="currentTask.priority > 1 ? 'danger' : 'info'">
          {{ currentTask.priority > 1 ? '高优先级' : '普通' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="返工次数">
        <el-tag v-if="getReworkCount(currentTask.id) > 0" type="danger">{{ getReworkCount(currentTask.id) }}次</el-tag>
        <span v-else>0次</span>
      </el-descriptions-item>
      <el-descriptions-item label="创建时间" :span="2">{{ currentTask.createTime }}</el-descriptions-item>
      <el-descriptions-item label="分配时间" :span="2">{{ currentTask.assignTime || '-' }}</el-descriptions-item>
      <el-descriptions-item label="完成时间" :span="2">{{ currentTask.completeTime || '-' }}</el-descriptions-item>
    </el-descriptions>

    <el-divider content-position="left">人员变更记录</el-divider>
    <el-table :data="taskAssignmentHistories" border stripe v-if="taskAssignmentHistories.length > 0" style="margin-top: 10px">
      <el-table-column label="原保洁员" width="120">
        <template #default="scope">
          {{ getCleanerName(scope.row.previousCleanerId) || '首次分配' }}
        </template>
      </el-table-column>
      <el-table-column label="新保洁员" width="120">
        <template #default="scope">
          {{ getCleanerName(scope.row.newCleanerId) }}
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="变更原因" width="150" />
      <el-table-column prop="remarks" label="备注" />
      <el-table-column prop="changeTime" label="变更时间" width="180" />
    </el-table>
    <el-empty v-else description="暂无人员变更记录" style="padding: 20px 0" />

    <template #footer>
      <el-button @click="showTaskDetailVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showInspectionHistoryVisible" title="质检记录" width="800px">
    <el-alert
      v-if="taskInspections.some(i => i.result === '不合格')"
      :title="'该任务共有 ' + taskInspections.filter(i => i.result === '不合格').length + ' 次质检不合格记录'"
      type="error"
      :closable="false"
      style="margin-bottom: 20px"
    />
    <el-table :data="taskInspections" border stripe v-if="taskInspections.length > 0">
      <el-table-column prop="inspector" label="质检员" width="120" />
      <el-table-column prop="result" label="质检结果" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.result === '合格' ? 'success' : 'danger'">
            {{ scope.row.result }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="评分影响" width="120">
        <template #default="scope">
          <el-tag :type="scope.row.result === '合格' ? 'success' : 'danger'" size="small">
            {{ scope.row.result === '合格' ? '+0.1分' : '-0.2分' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remarks" label="质检备注" />
      <el-table-column prop="inspectTime" label="质检时间" width="180" />
    </el-table>
    <el-empty v-else description="暂无质检记录" />
    <template #footer>
      <el-button @click="showInspectionHistoryVisible = false">关闭</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showCompleteSuccessVisible" title="保洁完成" width="500px">
    <el-result icon="success" title="保洁已完成，待质检">
      <template #sub-title>
        任务已进入质检流程，请安排质检人员进行质量检查
      </template>
      <template #extra>
        <el-button type="warning" @click="quickInspect">立即质检</el-button>
        <el-button @click="showCompleteSuccessVisible = false">稍后处理</el-button>
      </template>
    </el-result>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { CircleCheck, CircleClose } from '@element-plus/icons-vue'
import api from '../api'

const tasks = ref([])
const rooms = ref([])
const cleaners = ref([])
const inspections = ref([])
const assignmentHistories = ref([])
const showInspectDialogVisible = ref(false)
const showTaskDetailVisible = ref(false)
const showInspectionHistoryVisible = ref(false)
const showCompleteSuccessVisible = ref(false)
const inspectLoading = ref(false)
const currentTask = ref(null)
const currentTaskId = ref('')
const taskInspections = ref([])
const taskAssignmentHistories = ref([])
const inspectForm = ref({
  result: '合格',
  remarks: '',
  inspector: ''
})

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

const loadCleaners = async () => {
  try {
    const res = await api.get('/cleaners')
    cleaners.value = res.data
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
  loadTasks()
  loadRooms()
  loadCleaners()
  loadInspections()
  loadAssignmentHistories()
}

const getRoomNumber = (roomId) => {
  const room = rooms.value.find(r => r.id === roomId)
  return room ? room.roomNumber : roomId
}

const getCleanerName = (cleanerId) => {
  if (!cleanerId) return ''
  const cleaner = cleaners.value.find(c => c.id === cleanerId)
  return cleaner ? cleaner.name : cleanerId
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

const getReworkCount = (taskId) => {
  return inspections.value.filter(i => i.taskId === taskId && i.result === '不合格').length
}

const getReassignCount = (taskId) => {
  return assignmentHistories.value.filter(h => h.taskId === taskId).length
}

const assignTask = async (taskId) => {
  try {
    await api.post(`/tasks/${taskId}/assign`)
    ElMessage.success('任务分配成功')
    refreshData()
  } catch (e) {
    console.error(e)
    ElMessage.error('任务分配失败')
  }
}

const completeTask = async (taskId) => {
  try {
    await api.post(`/tasks/${taskId}/complete`)
    const task = tasks.value.find(t => t.id === taskId)
    currentTask.value = task
    showCompleteSuccessVisible.value = true
    refreshData()
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

const quickInspect = () => {
  showCompleteSuccessVisible.value = false
  showInspectDialog(currentTask.value)
}

const reworkTask = async (taskId) => {
  try {
    await api.post(`/tasks/${taskId}/rework`)
    ElMessage.success('已开始返工')
    refreshData()
  } catch (e) {
    console.error(e)
    ElMessage.error('操作失败')
  }
}

const showInspectDialog = (task) => {
  currentTask.value = task
  currentTaskId.value = task.id
  showInspectDialogVisible.value = true
}

const submitInspection = async () => {
  if (!inspectForm.value.inspector) {
    ElMessage.warning('请输入质检员姓名')
    return
  }
  inspectLoading.value = true
  try {
    await api.post(`/tasks/${currentTaskId.value}/inspect`, inspectForm.value)
    showInspectDialogVisible.value = false
    ElMessage.success(
      inspectForm.value.result === '合格'
        ? '质检合格，保洁任务已完成，保洁员评分+0.1'
        : '质检不合格，任务已退回返工，保洁员评分-0.2'
    )
    refreshData()
    inspectForm.value = {
      result: '合格',
      remarks: '',
      inspector: ''
    }
  } catch (e) {
    console.error(e)
    ElMessage.error('质检提交失败')
  } finally {
    inspectLoading.value = false
  }
}

const viewTaskDetail = async (task) => {
  currentTask.value = task
  try {
    const res = await api.get(`/tasks/${task.id}/assignment-history`)
    taskAssignmentHistories.value = res.data
  } catch (e) {
    console.error(e)
    taskAssignmentHistories.value = []
  }
  showTaskDetailVisible.value = true
}

const viewInspectionHistory = async (taskId) => {
  await loadInspections()
  taskInspections.value = inspections.value.filter(i => i.taskId === taskId)
  showInspectionHistoryVisible.value = true
}

onMounted(() => {
  refreshData()
})
</script>

<style scoped>
</style>