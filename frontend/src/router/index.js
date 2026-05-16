import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import Rooms from '../views/Rooms.vue'
import Orders from '../views/Orders.vue'
import Cleaners from '../views/Cleaners.vue'
import Tasks from '../views/Tasks.vue'

const routes = [
  { path: '/', component: Dashboard },
  { path: '/rooms', component: Rooms },
  { path: '/orders', component: Orders },
  { path: '/cleaners', component: Cleaners },
  { path: '/tasks', component: Tasks }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router