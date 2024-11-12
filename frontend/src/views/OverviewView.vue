<template>
  <div class="overview-view">
    <h1>Event Overview</h1>
    <EventList :events="displayedEvents" />
    <Pagination
        :currentPage="page"
        :totalPages="totalPages"
        @pageChanged="handlePageChange"
    />
  </div>
</template>

<script lang="ts">
import { defineComponent, onMounted, computed } from 'vue'
import { useStore } from 'vuex'
import EventList from '@/components/EventList.vue'
import Pagination from '@/components/Pagination.vue'

export default defineComponent({
  name: 'OverviewView',
  components: {
    EventList,
    Pagination
  },
  setup() {
    const store = useStore()

    const displayedEvents = computed(() => store.state.events.displayedEvents)
    const page = computed(() => store.state.events.page)
    const totalPages = computed(() => store.getters['events/totalPages'])

    const handlePageChange = (newPage: number) => {
      store.dispatch('events/setPage', newPage)
    }

    onMounted(() => {
      store.dispatch('events/fetchEvents')
    })

    return {
      displayedEvents,
      page,
      totalPages,
      handlePageChange
    }
  }
})
</script>