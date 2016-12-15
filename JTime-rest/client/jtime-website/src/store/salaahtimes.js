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
      times.map(time => {
        if (typeof time.datetime === 'string') {
          time.datetime = new Date(time.datetime)
        }
        return time
      })
      Vue.set(state.salaahTimes, masjidId, times)
    }
  },
  actions: {
    getSalaahTimesForMonth (context, {masjidId, year, month}) {
      const date = new Date()
      date.setFullYear(year)
      date.setMonth(month)
      const options = {params: {date: dateToDateString(date)}}
      Vue.http.get(`${baseUrl}/Masjids/${masjidId}/times-for-a-month`, options).then(response => {
        return response.json()
      }).then(({times}) => {
        context.commit('addSalaahTimes', {masjidId, times})
      })
    }
  }
}
