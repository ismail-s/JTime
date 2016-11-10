import Vue from 'vue'
import Vuex from 'vuex'
import VueResource from 'vue-resource'
import {dateToDateString} from './utils'
import dexiePlugin from './dexie'

Vue.use(VueResource)
Vue.use(Vuex)

const baseUrl = 'https://jtime.ismail-s.com/api'

const store = new Vuex.Store({
  state: {
    masjids: [],
    salaahTimes: {}, // map from masjidId to list of salaahTime objects
    loggedInUser: null, // dict of userId, email, accessToken, verified
    toastQueue: [] // Queue of strings to be shown as toasts
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
    },
    loginUser (state, {userId, email, accessToken, verified}) {
      state.loggedInUser = {userId, email, accessToken, verified}
      if (verified) {
        state.toastQueue.push(`Logged in as ${email}`)
      }
    },
    clearLoggedInUser (state, err) {
      var addToast = false
      if (state.loggedInUser) {
        addToast = true
      }
      state.loggedInUser = null
      if (addToast) {
        const msg = err ? `Login failed: ${err.message}` : 'Logged out successfully'
        state.toastQueue.push(msg)
      }
    },
    verifyLoggedInUser (state) {
      if (state.loggedInUser) {
        const oldVerifiedVal = state.loggedInUser.verified
        state.loggedInUser.verified = true
        if (oldVerifiedVal === false) {
          state.toastQueue.push(`Logged in as ${state.loggedInUser.email}`)
        }
      }
    },
    toast (state, msg) {
      state.toastQueue.push(msg)
    },
    removeOneToast (state) {
      state.toastQueue.shift()
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
    },
    login (context, {idToken, email}) {
      const options = {params: {id_token: idToken}}
      Vue.http.get(`${baseUrl}/user_tables/googleid`, options).then(response => {
        return response.json()
      }).then(({userId, access_token}) => {
        context.commit('loginUser', {userId, email, accessToken: access_token, verified: true})
      }).catch((err) => {
        context.commit('clearLoggedInUser', err)
      })
    },
    checkServerLoginStatus (context) {
      const loginState = context.state.loggedInUser
      if (!loginState) {
        // No persisted login, so we are logged out & don't need to do anything
        return
      }
      const options = {headers: {Authorization: loginState.accessToken}}
      Vue.http.get(`${baseUrl}/user_tables/${loginState.userId}`, options).then(response => {
        if (response.status === 200) {
          context.commit('verifyLoggedInUser')
        } else {
          context.commit('clearLoggedInUser')
        }
      })
    }
  },
  plugins: [dexiePlugin]
})

export default store
