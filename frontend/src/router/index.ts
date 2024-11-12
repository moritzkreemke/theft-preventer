// src/router/index.ts

import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import LoginView from '@/views/LoginView.vue'
import OverviewView from '@/views/OverviewView.vue'
import store from '@/store'

const routes: Array<RouteRecordRaw> = [
    {
        path: '/login',
        name: 'Login',
        component: LoginView,
        meta: { guest: true }
    },
    {
        path: '/',
        name: 'Overview',
        component: OverviewView,
        meta: { requiresAuth: true }
    }
]

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
})

router.beforeEach((to, from, next) => {
    const isAuthenticated = store.getters['auth/isAuthenticated']

    if (to.matched.some(record => record.meta.requiresAuth)) {
        if (!isAuthenticated) {
            next({ name: 'Login', query: { redirect: to.fullPath } })
        } else {
            next()
        }
    } else if (to.matched.some(record => record.meta.guest)) {
        if (isAuthenticated) {
            next({ name: 'Overview' })
        } else {
            next()
        }
    } else {
        next()
    }
})

export default router