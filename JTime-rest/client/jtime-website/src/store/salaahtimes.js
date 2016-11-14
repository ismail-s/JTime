import Vue from 'vue'
import VueResource from 'vue-resource'
import {dateToDateString} from '../utils'
import {baseUrl} from './utils'

Vue.use(VueResource)

export default {
  state: {
    salaahTimes: {} // map from masjidId to list of salaahTime objects
  },
  mutations: {
    addSalaahTimes (state, {masjidId, times}) {
      Vue.set(state.salaahTimes, masjidId, times)
    }
  },
  actions: {
    getSalaahTimes (context, {masjidId, date}) {
      const options = {params: {date: dateToDateString(date)}}
      Vue.http.get(`${baseUrl}/Masjids/${masjidId}/times`, options).then(response => {
        return response.json()
      }).then(({times}) => {
        context.commit('addSalaahTimes', {masjidId, times})
      })
    }
  }
}
