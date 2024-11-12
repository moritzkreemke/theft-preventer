import { createStore } from 'vuex'
import auth from './modules/auth'
import events from './modules/events'

export default createStore({
    modules: {
        auth,
        events
    }
})