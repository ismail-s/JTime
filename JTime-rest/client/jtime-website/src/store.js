import Vue from 'vue'
import Vuex from 'vuex'
import VueResource from 'vue-resource'
import {dateToDateString} from './utils'

Vue.use(VueResource)
Vue.use(Vuex)

const baseUrl = 'https://jtime.ismail-s.com/api'

const store = new Vuex.Store({
  state: {
    masjids: [],
    salaahTimes: {} // map from masjidId to list of salaahTime objects
  },
  mutations: {
    removeAllMasjids (state) {
      state.masjids = []
    },
    appendMasjids (state, newMasjids) {
      state.masjids = state.masjids.concat(newMasjids)
    },
    addSalaahTimes (state, {masjidId, times}) {
      Vue.set(state.salaahTimes, masjidId, times)
    }
  },
  actions: {
    getMasjids (context) {
      Vue.http.get(`${baseUrl}/Masjids`).then(response => {
        return response.json()
      }).then(masjids => {
        context.commit('removeAllMasjids')
        context.commit('appendMasjids', masjids)
      })
    },
    getSalaahTimes (context, {masjidId, date}) {
      const options = {params: {date: dateToDateString(date)}}
      Vue.http.get(`${baseUrl}/Masjids/${masjidId}/times`, options).then(response => {
        return response.json()
      }).then(({times}) => {
        context.commit('addSalaahTimes', {masjidId, times})
      })
    }
  }
})

export default store
