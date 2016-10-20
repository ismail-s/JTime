import Vue from 'vue'
import Vuex from 'vuex'
import VueResource from 'vue-resource'

Vue.use(VueResource)
Vue.use(Vuex)

const store = new Vuex.Store({
  state: {
    masjids: [{name: 'Test masjid'}]
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
      Vue.http.get('https://jtime.ismail-s.com/api/Masjids').then((response) => {
        return response.json()
      }).then((masjids) => {
        context.commit('removeAllMasjids')
        context.commit('appendMasjids', masjids)
      })
    }
  }
})

export default store
