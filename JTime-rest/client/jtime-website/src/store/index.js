import Vue from 'vue'
import Vuex from 'vuex'
import VueResource from 'vue-resource'
import dexiePlugin from '../dexie'
import MasjidsModule from './masjids'
import SalaahTimesModule from './salaahtimes'
import LoggedInUserModule from './logged-in-user'
import ToastModule from './toast'

Vue.use(VueResource)
Vue.use(Vuex)

const store = new Vuex.Store({
  plugins: [dexiePlugin],
  modules: {
    MasjidsModule,
    SalaahTimesModule,
    LoggedInUserModule,
    ToastModule
  }
})

export default store
