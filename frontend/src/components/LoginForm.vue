<template>
  <form @submit.prevent="submitForm">
    <div>
      <label for="username">Username:</label>
      <input type="text" id="username" v-model="username" required>
    </div>
    <div>
      <label for="password">Password:</label>
      <input type="password" id="password" v-model="password" required>
    </div>
    <button type="submit">Login</button>
  </form>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue'

export default defineComponent({
  name: 'LoginForm',
  emits: ['login'],
  setup(_, { emit }) {
    const username = ref('')
    const password = ref('')

    const submitForm = () => {
      emit('login', {
        username: username.value,
        password: password.value
      })
    }

    return {
      username,
      password,
      submitForm
    }
  }
})
</script>