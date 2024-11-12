<template>
  <table>
    <thead>
    <tr>
      <th>ID</th>
      <th>State</th>
      <th>Lux</th>
      <th>Temp</th>
      <th>Phone</th>
      <th>Timestamp</th>
    </tr>
    </thead>
    <tbody>
    <tr v-for="event in events" :key="event.id">
      <td>{{ event.id }}</td>
      <td>{{ event.state }}</td>
      <td>{{ event.lux }}</td>
      <td>{{ event.temp }}</td>
      <td>{{ event.phone }}</td>
      <td>{{ formatTimestamp(event.timestamp) }}</td>
    </tr>
    </tbody>
  </table>
</template>

<script lang="ts">
import { defineComponent, PropType } from 'vue'
import { Event } from '@/types'

export default defineComponent({
  name: 'EventList',
  props: {
    events: {
      type: Array as PropType<Event[]>,
      required: true
    }
  },
  setup() {
    const formatTimestamp = (timestamp: number): string => {
      return new Date(timestamp * 1000).toLocaleString()
    }

    return { formatTimestamp }
  }
})
</script>