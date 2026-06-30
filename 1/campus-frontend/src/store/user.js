import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref({})
  const token = ref('')
  const isLoggedIn = ref(false)

  const initialize = () => {
    const storedToken = localStorage.getItem('token')
    const storedUser = localStorage.getItem('userInfo')
    if (storedToken && storedUser) {
      token.value = storedToken
      userInfo.value = JSON.parse(storedUser)
      isLoggedIn.value = true
    }
  }

  const login = (userData, authToken) => {
    userInfo.value = userData
    token.value = authToken
    isLoggedIn.value = true
    localStorage.setItem('userInfo', JSON.stringify(userData))
    localStorage.setItem('token', authToken)
  }

  const logout = () => {
    userInfo.value = {}
    token.value = ''
    isLoggedIn.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { userInfo, token, isLoggedIn, initialize, login, logout }
}, {
  persist: true
})