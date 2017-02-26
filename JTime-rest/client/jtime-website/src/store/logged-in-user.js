import Vue from 'vue'
import VueResource from 'vue-resource'
import {baseUrl} from './utils'

Vue.use(VueResource)

export default {
  state: {
    loggedInUser: null // dict of userId, email, accessToken, verified
  },
  getters: {
    loggedIn (state) {
      const loggedInUser = state.loggedInUser
      return loggedInUser && loggedInUser.verified === true
    }
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
    login ({commit, dispatch, state}, {idToken, email}) {
      // First, check to see if we have a saved login with the same email
      if (state.loggedInUser && state.loggedInUser.email === email) {
        // Check if we have verified the login is still valid
        if (!state.loggedInUser.verified || state.loggedInUser.verified !== true) {
          // Check if login is still valid
          return dispatch('checkServerLoginStatus').then(() => {
            commit('toast', `Logged in as ${email}`)
            return Promise.resolve()
          }).catch(() => {
            /* checkServerLoginStatus clears the saved login user if it isn't,
               valid, so this line essentially is like doing a goto to the else
               block. */
            return dispatch('login', {idToken, email})
          })
        }
        return Promise.resolve()
      } else {
        // Login user
        const options = {params: {id_token: idToken}}
        return Vue.http.get(`${baseUrl}/user_tables/googleid`, options).then(response => {
          return response.json()
        }).then(({userId, access_token}) => {
          commit('loginUser', {userId, email, accessToken: access_token, verified: true})
          commit('toast', `Logged in as ${email}`)
          return Promise.resolve()
        }).catch((err) => {
          commit('clearLoggedInUser')
          commit('toast', `Login failed: ${err.message}`)
          return Promise.reject(err)
        })
      }
    },
    logout (context) {
      const loginState = context.state.loggedInUser
      const options = {headers: {Authorization: loginState.accessToken}}
      Vue.http.post(`${baseUrl}/user_tables/logout`, {}, options).then(response => {
        if (response.status === 204) {
          context.commit('clearLoggedInUser')
          context.commit('toast', 'Logged out successfully')
        } else {
          context.commit('toast', 'Logout was unsuccessful')
        }
      }).catch(err => {
        console.error(err)
      })
    },
    checkServerLoginStatus (context) {
      const loginState = context.state.loggedInUser
      if (!loginState) {
        // No persisted login, so we are logged out & don't need to do anything
        return Promise.reject(new Error())
      }
      const options = {headers: {Authorization: loginState.accessToken}}
      return Vue.http.get(`${baseUrl}/user_tables/${loginState.userId}`, options).then(response => {
        if (response.status === 200) {
          context.commit('verifyLoggedInUser')
        } else {
          context.commit('clearLoggedInUser')
          throw new Error()
        }
      }).catch(err => {
        context.commit('clearLoggedInUser')
        throw err
      })
    }
  }
}
