import Vue from 'vue'
import VueResource from 'vue-resource'
import {baseUrl} from './utils'

Vue.use(VueResource)

export default {
  state: {
    loggedInUser: null // dict of userId, email, accessToken, verified
  },
  mutations: {
    loginUser (state, {userId, email, accessToken, verified}) {
      state.loggedInUser = {userId, email, accessToken, verified}
    },
    clearLoggedInUser (state, err) {
      state.loggedInUser = null
    },
    verifyLoggedInUser (state) {
      if (state.loggedInUser) {
        state.loggedInUser.verified = true
      }
    }
  },
  actions: {
    login (context, {idToken, email}) {
      const options = {params: {id_token: idToken}}
      Vue.http.get(`${baseUrl}/user_tables/googleid`, options).then(response => {
        return response.json()
      }).then(({userId, access_token}) => {
        context.commit('loginUser', {userId, email, accessToken: access_token, verified: true})
        context.commit('toast', `Logged in as ${email}`)
      }).catch((err) => {
        context.commit('clearLoggedInUser')
        context.commit('toast', `Login failed: ${err.message}`)
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
          context.commit('toast', `Logged in as ${context.state.loggedInUser.email}`)
        } else {
          context.commit('clearLoggedInUser')
          context.commit('toast', 'Logged out on server')
        }
      })
    }
  }
}
