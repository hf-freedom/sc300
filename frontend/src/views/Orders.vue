<template>
  <el-card shadow="hover">
    <template #header>
      <span>订单列表</span>
      <el-button type="primary" size="small" @click="showAddDialog = true">新建订单</el-button>
      <el-button type="success" size="small" @click="refreshData" style="margin-left: 10px">刷新</el-button>
    </template>
    <el-table :data="orders" border stripe>
      <el-table-column prop="orderNumber" label="订单号" width="150" />
      <el-table-column prop="guestName" label="客人姓名" width="120" />
      <el-table-column label="房间信息" width="120">
        <template #default="scope">
          {{ getRoomNumber(scope.row.roomId) }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">{{ scope.row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isUrgent" label="紧急" width="80">
        <template #default="scope">
          <el-tag v-if="scope.row.isUrgent" type="danger">是</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="320">
        <template #default="scope">
          <el-button
            v-if="scope.row.status === '已确认'"
            type="warning"
            size="small"
            @click="confirmCheckout(scope.row)"
          >
            退房
          </el-button>
          <el-button
            v-if="scope.row.status === '已退房'"
            type="info"
            size="small"
            @click="viewTask(scope.row.id)"
          >
            查看保洁任务
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="setUrgent(scope.row.id)"
          >
            设为紧急
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="showAddDialog" title="新建订单" width="500px">
    <el-form :model="orderForm" label-width="80px">
      <el-form-item label="订单号">
        <el-input v-model="orderForm.orderNumber" />
      </el-form-item>
      <el-form-item label="客人姓名">
        <el-input v-model="orderForm.guestName" />
      </el-form-item>
      <el-form-item label="房间">
        <el-select v-model="orderForm.roomId" style="width: 100%">
          <el-option
            v-for="room in availableRooms"
            :key="room.id"
            :label="room.roomNumber + ' (' + room.area + ')'"
            :value="room.id"
          />
        </el-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showAddDialog = false">取消</el-button>
      <el-button type="primary" @click="submitOrder">确定</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showCheckoutConfirm" title="确认退房" width="500px">
    <p>确认要对订单 <strong>{{ currentOrder?.orderNumber }}</strong> 进行退房操作吗？</p>
    <p>退房后将自动为房间 <strong>{{ getRoomNumber(currentOrder?.roomId) }}</strong> 生成保洁任务。</p>
    <template #footer>
      <el-button @click="showCheckoutConfirm = false">取消</el-button>
      <el-button type="warning" @click="doCheckout" :loading="checkoutLoading">确认退房</el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="showTaskResult" title="退房成功" width="600px">
    <el-result icon="success" title="退房成功，已生成保洁任务">
      <template #sub-title>
        订单 {{ currentOrder?.orderNumber }} 已完成退房，系统已自动创建保洁任务
      </template>
      <template #extra>
        <el-descriptions :column="1" border style="margin-top: 20px">
          <el-descriptions-item label="任务ID">{{ latestTask?.id }}</el-descriptions-item>
          <el-descriptions-item label="房间号">{{ getRoomNumber(latestTask?.roomId) }}</el-descriptions-item>
          <el-descriptions-item label="任务状态">
            <el-tag type="primary">{{ latestTask?.status }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <el-tag :type="latestTask?.priority > 1 ? 'danger' : 'info'">
              {{ latestTask?.priority > 1 ? '高优先级' : '普通' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ latestTask?.createTime }}</el-descriptions-item>
        </el-descriptions>
        <div style="margin-top: 20px; text-align: center">
          <el-button type="primary" @click="goToTasks">前往任务列表</el-button>
          <el-button @click="showTaskResult = false">关闭</el-button>
        </div>
      </template>
    </el-result>
  </el-dialog>

  <el-dialog v-model="showTaskDetail" title="保洁任务详情" width="600px">
    <el-descriptions :column="1" border v-if="currentTask">
      <el-descriptions-item label="任务ID">{{ currentTask.id }}</el-descriptions-item>
      <el-descriptions-item label="房间号">{{ getRoomNumber(currentTask.roomId) }}</el-descriptions-item>
      <el-descriptions-item label="保洁人员">
        {{ getCleanerName(currentTask.cleanerId) || '待分配' }}
      </el-descriptions-item>
      <el-descriptions-item label="任务状态">
        <el-tag :type="getTaskStatusType(currentTask.status)">{{ currentTask.status }}</el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="优先级">
        <el-tag :type="currentTask.priority > 1 ? 'danger' : 'info'">
          {{ currentTask.priority > 1 ? '高优先级' : '普通' }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="创建时间">{{ currentTask.createTime }}</el-descriptions-item>
      <el-descriptions-item label="分配时间">{{ currentTask.assignTime || '-' }}</el-descriptions-item>
      <el-descriptions-item label="完成时间">{{ currentTask.completeTime || '-' }}</el-descriptions-item>
    </el-descriptions>
    <template #footer>
      <el-button @click="showTaskDetail = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import api from '../api'

const router = useRouter()
const orders = ref([])
const rooms = ref([])
const cleaners = ref([])
const tasks = ref([])
const showAddDialog = ref(false)
const showCheckoutConfirm = ref(false)
const showTaskResult = ref(false)
const showTaskDetail = ref(false)
const checkoutLoading = ref(false)
const currentOrder = ref(null)
const currentTask = ref(null)
const latestTask = ref(null)
const orderForm = ref({
  orderNumber: '',
  guestName: '',
  roomId: ''
})

const availableRooms = computed(() => {
  return rooms.value.filter(r => r.status === '空闲' && r.isCleaned)
})

const getRoomNumber = (roomId) => {
  const room = rooms.value.find(r => r.id === roomId)
  return room ? room.roomNumber : roomId
}

const getCleanerName = (cleanerId) => {
  const cleaner = cleaners.value.find(c => c.id === cleanerId)
  return cleaner ? cleaner.name : cleanerId
}

const getTaskStatusType = (status) => {
  const map = {
    '待分配': 'info',
    '已分配': 'primary',
    '待质检': 'warning',
    '待返工': 'danger',
    '已完成': 'success'
  }
  return map[status] || ''
}

const loadOrders = async () => {
  try {
    const res = await api.get('/orders')
    orders.value = res.data
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

const loadTasks = async () => {
  try {
    const res = await api.get('/tasks')
    tasks.value = res.data
  } catch (e) {
    console.error(e)
  }
}

const refreshData = () => {
  loadOrders()
  loadRooms()
  loadTasks()
}

const getStatusType = (status) => {
  const map = {
    '已确认': 'primary',
    '已退房': 'success',
    '已取消': 'info'
  }
  return map[status] || ''
}

const confirmCheckout = (order) => {
  currentOrder.value = order
  showCheckoutConfirm.value = true
}

const doCheckout = async () => {
  if (!currentOrder.value) return
  checkoutLoading.value = true
  try {
    await api.post(`/orders/${currentOrder.value.id}/checkout`)
    await loadTasks()
    await loadRooms()
    latestTask.value = tasks.value.find(t => t.orderId === currentOrder.value.id)
    showCheckoutConfirm.value = false
    showTaskResult.value = true
    ElMessage.success('退房成功，已生成保洁任务')
    loadOrders()
  } catch (e) {
    console.error(e)
    ElMessage.error('退房操作失败')
  } finally {
    checkoutLoading.value = false
  }
}

const viewTask = async (orderId) => {
  await loadTasks()
  const task = tasks.value.find(t => t.orderId === orderId)
  if (task) {
    currentTask.value = task
    showTaskDetail.value = true
  } else {
    ElMessage.info('该订单暂无保洁任务')
  }
}

const goToTasks = () => {
  showTaskResult.value = false
  router.push('/tasks')
}

const setUrgent = async (orderId) => {
  try {
    await api.post(`/orders/${orderId}/urgent`)
    ElMessage.success('已设置为紧急订单，保洁优先级已提高')
    loadOrders()
  } catch (e) {
    console.error(e)
  }
}

const submitOrder = async () => {
  try {
    await api.post('/orders', orderForm.value)
    ElMessage.success('订单创建成功')
    showAddDialog.value = false
    loadOrders()
    orderForm.value = { orderNumber: '', guestName: '', roomId: '' }
  } catch (e) {
    console.error(e)
    ElMessage.error('订单创建失败')
  }
}

onMounted(() => {
  loadOrders()
  loadRooms()
  loadCleaners()
  loadTasks()
})
</script>

<style scoped>
</style>