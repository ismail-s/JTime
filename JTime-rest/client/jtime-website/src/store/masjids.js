import Vue from 'vue'
import VueResource from 'vue-resource'
import {baseUrl} from './utils'

Vue.use(VueResource)

export default {
  state: {
    masjids: []
  },
  mutations: {
    removeAllMasjids (state) {
      state.masjids = []
    },
    appendMasjids (state, newMasjids) {
      state.masjids = state.masjids.concat(newMasjids)
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
    }
  }
}
