import Vue from 'vue'
import Vuex from 'vuex'
import router from '../../../src/router'
import AllMasjids from 'src/components/AllMasjids'

describe('AllMasjids.vue', () => {
  function setUpComponent () {
    const mockStore = {actions: {getMasjids: sinon.spy()}, state: {MasjidsModule: {masjids: []}}}
    const vm = new Vue({
      el: document.createElement('div'),
      render: (h) => h(AllMasjids),
      store: new Vuex.Store(mockStore),
      router
    })
    return [vm, mockStore]
  }
  it('should initially just display the title', () => {
    const [vm] = setUpComponent()
    expect(vm.$el.textContent)
      .to.equal('All Masjids ')
  })

  it('should dispatch a getMasjids action', () => {
    const [, mockStore] = setUpComponent()
    expect(mockStore.actions.getMasjids)
      .to.have.been.calledOnce
  })

  it('should display any masjids added to the state', done => {
    const [vm, mockStore] = setUpComponent()
    mockStore.state.MasjidsModule.masjids = [{id: 5, name: 'test', humanReadableAddress: 'sdfa'}]
    Vue.nextTick(() => {
      expect(vm.$el.textContent).to.contain('test')
      expect(vm.$el.textContent).to.contain('sdfa')
      const now = new Date()
      const year = now.getFullYear()
      const month = now.getMonth()
      expect(vm.$el.innerHTML).to.contain(`/masjid/5/${year}/${month}`)
      done()
    }, 0)
  })
})
