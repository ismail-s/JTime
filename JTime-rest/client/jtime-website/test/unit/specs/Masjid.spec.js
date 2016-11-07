import Vue from 'vue'
import Vuex from 'vuex'
import router from '../../../src/router'
import Masjid from 'src/components/Masjid'

// For some reason, in the test environment Array.find doesn't exist. This is
// a polyfill for it.
if (!Array.prototype.find) {
  Object.defineProperty(Array.prototype, 'find', { // eslint-disable-line no-extend-native
    value: function (predicate) {
      'use strict'
      if (this == null) {
        throw new TypeError('Array.prototype.find called on null or undefined')
      }
      if (typeof predicate !== 'function') {
        throw new TypeError('predicate must be a function')
      }
      var list = Object(this)
      var length = list.length >>> 0
      var thisArg = arguments[1]
      var value

      for (var i = 0; i < length; i++) {
        value = list[i]
        if (predicate.call(thisArg, value, i, list)) {
          return value
        }
      }
      return undefined
    }
  })
}

describe('Masjid.vue', () => {
  function setUpComponent (masjids = []) {
    router.push({name: 'masjid-id-date', params: {id: 1, date: '2016-01-01'}})
    const mockStore = {actions: {getMasjids: sinon.spy(), getSalaahTimes: sinon.spy()}, state: {masjids, salaahTimes: {}}}
    const vm = new Vue({
      el: document.createElement('div'),
      render: (h) => h(Masjid),
      store: new Vuex.Store(mockStore),
      router
    })
    return [vm, mockStore]
  }
  it('should initially display "Loading...", along with the table titles', () => {
    const [vm] = setUpComponent()
    expect(vm.$el.textContent).to.contain('Loading...')
    expect(vm.$el.textContent).to.contain('Salaah type')
    expect(vm.$el.textContent).to.contain('Time')
  })

  it('should dispatch a getMasjids action when it can\'t find the masjid in the current state', () => {
    const [, mockStore] = setUpComponent()
    expect(mockStore.actions.getMasjids)
      .to.have.been.calledOnce
  })

  it('shouldn\'t dispatch a getMasjids action when it can find the masjid in the current state', () => {
    const [, mockStore] = setUpComponent([{id: 1, name: 'test', humanReadableAddress: 'sdfa'}])
    expect(mockStore.actions.getMasjids)
      .to.not.have.been.calledOnce
  })

  it('dispatchs a getSalaahTimes action on component creation', () => {
    const [, mockStore] = setUpComponent()
    expect(mockStore.actions.getSalaahTimes)
      .to.have.been.calledWith(sinon.match.any, sinon.match((val) => {
        return val.type === 'getSalaahTimes' &&
          val.masjidId === 1 &&
          val.date.getTime() === new Date('2016-01-01').getTime()
      }))
  })

  it('displays salaahTimes when they are added to the store', (done) => {
    const [vm, mockStore] = setUpComponent()
    mockStore.state.salaahTimes = {'1': [{'type': 'f', 'datetime': '2016-11-07T06:40:17.354Z'}, {'type': 'z', 'datetime': '2016-11-07T13:00:58.374Z'}, {'type': 'a', 'datetime': '2016-11-07T15:00:58.374Z'}, {'type': 'e', 'datetime': '2016-11-07T18:30:58.374Z'}]}
    Vue.nextTick(() => {
      const text = vm.$el.textContent
      expect(text).to.contain('Fajr')
      expect(text).to.contain('Zohar')
      expect(text).to.contain('Asr')
      expect(text).to.contain('Esha')
      expect(text).to.contain('06-40')
      expect(text).to.contain('13-00')
      expect(text).to.contain('15-00')
      expect(text).to.contain('18-30')
      done()
    })
  })
})
