<!-- src/views/LoginView.vue -->
<template>
  <div class="login-view">
    <h1>Login</h1>
    <LoginForm @login="handleLogin" />
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'
import { useStore } from 'vuex'
import { useRouter, useRoute } from 'vue-router'
import LoginForm from '@/components/LoginForm.vue'

export default defineComponent({
  name: 'LoginView',
  components: {
    LoginForm
  },
  setup() {
    const store = useStore()
    const router = useRouter()
    const route = useRoute()

    const handleLogin = async (credentials: { username: string; password: string }) => {
      const success = await store.dispatch('auth/login', credentials)
      if (success) {
        const redirectPath = route.query.redirect as string
        if (redirectPath && !redirectPath.includes('/login')) {
          router.push(redirectPath)
        } else {
          router.push({ name: 'Overview' })
        }
      } else {
        // Handle login failure (e.g., show an error message)
        console.error('Login failed')
      }
    }

    return { handleLogin }
  }
})
</script>