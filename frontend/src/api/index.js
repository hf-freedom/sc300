import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8005/api'
})

export default api