// src/services/api.ts

import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import store from '@/store'
import router from '@/router'

const api: AxiosInstance = axios.create({
    baseURL: '/api'
})

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
    const token = store.state.auth.token
    if (token) {
        config.headers = config.headers || {}
        config.headers.Authorization = `Bearer ${token}`
    }
    return config
})

api.interceptors.response.use(
    response => response,
    error => {
        if (error.response && error.response.status === 401) {
            store.dispatch('auth/logout')
            router.push({ name: 'Login', query: { redirect: router.currentRoute.value.fullPath } })
        }
        return Promise.reject(error)
    }
)

export default api