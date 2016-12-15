import Vue from 'vue'
import Vuex from 'vuex'
import moment from 'moment'
import router from '../../../src/router'
import Masjid from 'src/components/Masjid'
import 'babel-polyfill'

describe('Masjid.vue', () => {
  function setUpComponent (masjids = []) {
    router.push({name: 'masjid-times-for-month', params: {id: 1, year: 2016, month: 0}})
    const mockStore = {actions: {getMasjids: sinon.spy(), getSalaahTimesForMonth: sinon.spy()}, state: {MasjidsModule: {masjids}, SalaahTimesModule: {salaahTimes: {}}}}
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
    const strings = ['Loading...', 'Date', 'Day', 'Fajr', 'Zohar', 'Asr', 'Magrib', 'Esha']
    strings.forEach(text => {
      expect(vm.$el.textContent).to.contain(text)
    })
  })

  it('displays each day of the month and day of the week', () => {
    const [vm] = setUpComponent()
    const daysInMonth = moment().year(2016).month(0).daysInMonth()
    const strings = [...Array(daysInMonth).keys()].map(i => {
      const day = moment().year(2016).month(0).date(i + 1).format('ddd')
      return `${i + 1} ${day}`
    })
    strings.forEach(text => {
      expect(vm.$el.textContent).to.contain(text)
    })
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
    expect(mockStore.actions.getSalaahTimesForMonth)
      .to.have.been.calledWith(sinon.match.any, sinon.match((val) => {
        return val.type === 'getSalaahTimesForMonth' &&
          val.masjidId === 1 &&
          val.year === 2016 &&
          val.month === 0
      }))
  })

  it('displays salaahTimes when they are added to the store & are for the correct month', (done) => {
    const [vm, mockStore] = setUpComponent()
    mockStore.state.SalaahTimesModule.salaahTimes = {'1': [{'type': 'f', 'datetime': new Date('2016-01-01T06:40:17.354Z')}, {'type': 'z', 'datetime': new Date('2016-01-02T13:00:58.374Z')}, {'type': 'a', 'datetime': new Date('2016-01-03T15:00:58.374Z')}, {'type': 'm', 'datetime': new Date('2016-01-03T16:05:58.374Z')}, {'type': 'e', 'datetime': new Date('2016-01-04T18:30:58.374Z')}]}
    Vue.nextTick(() => {
      const text = vm.$el.textContent
      expect(text).to.contain('06-40')
      expect(text).to.contain('13-00')
      expect(text).to.contain('15-00')
      expect(text).to.contain('16-05')
      expect(text).to.contain('18-30')
      done()
    })
  })

  it('doesn\'t display salaahTimes from other months', (done) => {
    const [vm, mockStore] = setUpComponent()
    mockStore.state.SalaahTimesModule.salaahTimes = {'1': [{'type': 'f', 'datetime': new Date('2016-02-01T06:40:17.354Z')}, {'type': 'z', 'datetime': new Date('2016-03-02T13:00:58.374Z')}, {'type': 'a', 'datetime': new Date('2017-01-03T15:00:58.374Z')}, {'type': 'm', 'datetime': new Date('2015-01-03T16:05:58.374Z')}, {'type': 'e', 'datetime': new Date('2016-12-04T18:30:58.374Z')}]}
    Vue.nextTick(() => {
      const text = vm.$el.textContent
      expect(text).to.not.contain('06-40')
      expect(text).to.not.contain('13-00')
      expect(text).to.not.contain('15-00')
      expect(text).to.not.contain('16-05')
      expect(text).to.not.contain('18-30')
      done()
    })
  })
})
