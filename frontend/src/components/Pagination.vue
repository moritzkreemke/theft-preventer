<template>
  <div class="pagination">
    <button @click="prevPage" :disabled="currentPage === 1">Previous</button>
    <span>Page {{ currentPage }} of {{ totalPages }}</span>
    <button @click="nextPage" :disabled="currentPage === totalPages">Next</button>
  </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue'

export default defineComponent({
  name: 'Pagination',
  props: {
    currentPage: {
      type: Number,
      required: true
    },
    totalPages: {
      type: Number,
      required: true
    }
  },
  emits: ['pageChanged'],
  setup(props, { emit }) {
    const prevPage = () => {
      if (props.currentPage > 1) {
        emit('pageChanged', props.currentPage - 1)
      }
    }

    const nextPage = () => {
      if (props.currentPage < props.totalPages) {
        emit('pageChanged', props.currentPage + 1)
      }
    }

    return {
      prevPage,
      nextPage
    }
  }
})
</script>