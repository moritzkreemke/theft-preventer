import { Module } from 'vuex'
import api from '@/services/api'
import { RootState, EventsState, Event } from '@/types'

const events: Module<EventsState, RootState> = {
    namespaced: true,
    state: {
        allEvents: [],
        displayedEvents: [],
        page: 1,
        perPage: 10,
        total: 0
    },
    mutations: {
        SET_ALL_EVENTS(state, events: Event[]) {
            state.allEvents = events
            state.total = events.length
            state.page = 1
            this.commit('events/UPDATE_DISPLAYED_EVENTS')
        },
        SET_PAGE(state, page: number) {
            state.page = page
            this.commit('events/UPDATE_DISPLAYED_EVENTS')
        },
        UPDATE_DISPLAYED_EVENTS(state) {
            const start = (state.page - 1) * state.perPage
            const end = start + state.perPage
            state.displayedEvents = state.allEvents.slice(start, end)
        }
    },
    actions: {
        async fetchEvents({ commit }) {
            try {
                const { data } = await api.get<Event[]>('/events')
                commit('SET_ALL_EVENTS', data)
            } catch (error) {
                console.error('Failed to fetch events:', error)
            }
        },
        setPage({ commit }, page: number) {
            commit('SET_PAGE', page)
        }
    },
    getters: {
        totalPages: (state) => Math.ceil(state.total / state.perPage)
    }
}

export default events