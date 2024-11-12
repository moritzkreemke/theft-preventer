// src/store/modules/auth.ts

import { Module } from 'vuex'
import api from '@/services/api'
import { AuthState, RootState } from '@/types'

const auth: Module<AuthState, RootState> = {
    namespaced: true,
    state: {
        token: localStorage.getItem('token') || null
    },
    getters: {
        isAuthenticated: (state) => !!state.token
    },
    mutations: {
        SET_TOKEN(state, token: string) {
            state.token = token
            localStorage.setItem('token', token)
        },
        CLEAR_TOKEN(state) {
            state.token = null
            localStorage.removeItem('token')
        }
    },
    actions: {
        async login({ commit }, credentials: { username: string; password: string }) {
            try {
                const response = await api.post<{ token: string }>('/login', credentials)
                commit('SET_TOKEN', response.data.token)
                return true
            } catch (error) {
                console.error('Login failed:', error)
                return false
            }
        },
        logout({ commit }) {
            commit('CLEAR_TOKEN')
        }
    }
}

export default auth