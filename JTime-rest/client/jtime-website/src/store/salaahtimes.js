import haversine from 'haversine'
import moment from 'moment'
import Vue from 'vue'
import VueResource from 'vue-resource'
import {dateToDateString} from '../utils'
import {baseUrl} from './utils'

Vue.use(VueResource)

export default {
  state: {
    salaahTimes: {}, // map from masjidId to list of salaahTime objects
    nearbySalaahTimes: [] // list of lists of the form [salaahType, salaah_times]
  },
  mutations: {
    addSalaahTimes (state, {masjidId, times}) {
      Vue.set(state.salaahTimes, masjidId, times)
    },
    addNearbySalaahTimes (state, newTimes) {
      state.nearbySalaahTimes = newTimes
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
        const newTimes = times.map(time => {
          if (typeof time.datetime === 'string') {
            time.datetime = new Date(time.datetime)
          }
          return time
        // Make sure times returned by rest api are for the correct month/year
        }).filter(time => time.datetime.getFullYear() === year && time.datetime.getMonth() === month)
        context.commit('addSalaahTimes', {masjidId, times: newTimes})
      })
    },
    getTimesForNearbyMasjids (context, {latitude, longitude}) {
      const date = new Date()
      const options = {params: {date: dateToDateString(date)}, location: {lat: latitude, lng: longitude}}
      Vue.http.get(`${baseUrl}/SalaahTimes/times-for-multiple-masjids`, options).then(response => {
        return response.json()
      }).then(({res}) => {
        const newTimes = res.map(time => {
          if (typeof time.datetime === 'string') {
            time.datetime = new Date(time.datetime)
          }
          return time
        // Make sure times returned by rest api are for today
        }).filter(time => moment().format('YYYY-DDD') === moment(time).format('YYYY-DDD'))
        .reduce((acc, elem) => {
          if (acc[elem.type]) {
            acc[elem.type].push(elem)
            return acc
          }
        }, {'f': [], 'z': [], 'a': [], 'm': [], 'e': []})
        var newTimesAsList = [['f', newTimes['f']], ['z', newTimes['z']],
          ['a', newTimes['a']], ['m', newTimes['m']], ['e', newTimes['e']]]
          .map(([salaahType, times]) => {
            const sortedTimes = times.sort((a, b) => {
              // sort by time, then by distance from current possition
              const [aTime, bTime] = [a.getTime(), b.getTime()]
              if (aTime < bTime) {
                return -1
              } else if (aTime > bTime) {
                return 1
              } else {
                const currentLoc = {latitude, longitude}
                const aLoc = {latitude: a.location.lat, longitude: a.location.lng}
                const bLoc = {latitude: b.location.lat, longitude: b.location.lng}
                const aDist = haversine(currentLoc, aLoc)
                const bDist = haversine(currentLoc, bLoc)
                if (aDist < bDist) {
                  return -1
                } else if (aDist > bDist) {
                  return 1
                } else {
                  return 0
                }
              }
            })
            return [salaahType, sortedTimes]
          })
        context.commit('addNearbySalaahTimes', newTimesAsList)
      })
    }
  }
}
