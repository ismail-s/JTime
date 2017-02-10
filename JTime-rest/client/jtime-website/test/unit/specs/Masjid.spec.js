import Vue from 'vue'
import Vuex from 'vuex'
import moment from 'moment'
import router from 'src/router'
import LoggedInUserModule from 'src/store/logged-in-user'
import Masjid from 'src/components/Masjid'

describe('Masjid.vue', () => {
  function setUpComponent (masjids = [], routerParams = {id: 1, year: 2016, month: 0}) {
    router.push({name: 'masjid-times-for-month', params: routerParams})
    const mockStore = {modules: {
      MasjidsModule: {
        actions: {getMasjids: sinon.spy(), getSalaahTimesForMonth: sinon.spy()},
        state: {masjids}
      },
      SalaahTimesModule: {
        state: {salaahTimes: {}}
      },
      LoggedInUserModule: {
        state: {loggedInUser: null},
        getters: LoggedInUserModule.getters
      }}}
    const vm = new Vue({
      el: document.createElement('div'),
      render: (h) => h(Masjid),
      store: new Vuex.Store(mockStore),
      router
    })
    return [vm, mockStore]
  }
  it('should initially display "Loading...", along with the table titles and buttons', () => {
    const [vm] = setUpComponent()
    const strings = ['Loading...', 'Date', 'Day', 'Fajr', 'Zohar', 'Asr', 'Magrib', 'Esha']
    strings.forEach(text => {
      expect(vm.$el.textContent).to.contain(text)
    })
    expect(vm.$el.textContent).to.match(/.+Previous month\s+Next month.+Previous month\s+Next month$/)
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
    expect(mockStore.modules.MasjidsModule.actions.getMasjids)
      .to.have.been.calledOnce
  })

  it('shouldn\'t dispatch a getMasjids action when it can find the masjid in the current state', () => {
    const [, mockStore] = setUpComponent([{id: 1, name: 'test', humanReadableAddress: 'sdfa'}])
    expect(mockStore.modules.MasjidsModule.actions.getMasjids)
      .to.not.have.been.calledOnce
  })

  it('dispatchs a getSalaahTimes action on component creation', () => {
    const [, mockStore] = setUpComponent()
    expect(mockStore.modules.MasjidsModule.actions.getSalaahTimesForMonth)
      .to.have.been.calledWith(sinon.match.any, sinon.match((val) => {
        return val.type === 'getSalaahTimesForMonth' &&
          val.masjidId === 1 &&
          val.year === 2016 &&
          val.month === 0
      }))
  })

  it('displays salaahTimes when they are added to the store & are for the correct month', (done) => {
    const [vm, mockStore] = setUpComponent()
    mockStore.modules.SalaahTimesModule.state.salaahTimes = {'1': [{'type': 'f', 'datetime': new Date('2016-01-01T06:40:17.354Z')}, {'type': 'z', 'datetime': new Date('2016-01-02T13:00:58.374Z')}, {'type': 'a', 'datetime': new Date('2016-01-03T15:00:58.374Z')}, {'type': 'm', 'datetime': new Date('2016-01-03T16:05:58.374Z')}, {'type': 'e', 'datetime': new Date('2016-01-04T18:30:58.374Z')}]}
    Vue.nextTick(() => {
      const text = vm.$el.textContent
      expect(text).to.contain('06-40')
      expect(text).to.contain('13-00')
      expect(text).to.contain('15-00')
      expect(text).to.contain('16-10') // 5 mins should be added to magrib time
      expect(text).to.contain('18-30')
      done()
    })
  })

  it('doesn\'t display salaahTimes from other months', (done) => {
    const [vm, mockStore] = setUpComponent()
    mockStore.modules.SalaahTimesModule.state.salaahTimes = {'1': [{'type': 'f', 'datetime': new Date('2016-02-01T06:40:17.354Z')}, {'type': 'z', 'datetime': new Date('2016-03-02T13:00:58.374Z')}, {'type': 'a', 'datetime': new Date('2017-01-03T15:00:58.374Z')}, {'type': 'm', 'datetime': new Date('2015-01-03T16:00:58.374Z')}, {'type': 'e', 'datetime': new Date('2016-12-04T18:30:58.374Z')}]}
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

  describe('buttons to change months', () => {
    const changingMonthTests = [
      {
        title: 'navigate to the next month by clicking the first next month button',
        buttonToClick: 1,
        expectedYear: 2016,
        expectedMonth: 1},
      {title: 'navigate to the next month by clicking the second next month button',
        buttonToClick: 3,
        expectedYear: 2016,
        expectedMonth: 1},
      {title: 'navigate to the next month when current month is December',
        routerParams: {id: 1, year: 2016, month: 11},
        buttonToClick: 1,
        expectedYear: 2017,
        expectedMonth: 0},
      {title: 'navigate to the previous month by clicking the first previous month button',
        routerParams: {id: 1, year: 2016, month: 1},
        buttonToClick: 0,
        expectedYear: 2016,
        expectedMonth: 0},
      {title: 'navigate to the previous month by clicking the second previous month button',
        routerParams: {id: 1, year: 2016, month: 1},
        buttonToClick: 2,
        expectedYear: 2016,
        expectedMonth: 0},
      {title: 'navigate to the previous month when current month is January',
        buttonToClick: 0,
        expectedYear: 2015,
        expectedMonth: 11}]
    changingMonthTests.forEach(e => {
      it(e.title, () => {
        const [vm] = e.routerParams ? setUpComponent([], e.routerParams) : setUpComponent()
        const buttons = vm.$el.getElementsByTagName('button')
        // buttons is an array as follows:
        // [prevMonthButton1, nextMonthButton1, prevMonthButton2, nextMonthButton2]
        // as there are 2 sets of 2 buttons, at the top and bottom of the component
        buttons[e.buttonToClick].click()
        expect(router.currentRoute.params).to.have.property('year', e.expectedYear)
        expect(router.currentRoute.params).to.have.property('month', e.expectedMonth)
      })
    })
  })

  it('displays masjid name, month and year when masjid name is known', () => {
    const [vm] = setUpComponent([{id: 1, name: 'test name', humanReadableAddress: 'sdfa'}])
    expect(vm.$el.textContent).to.contain('test name')
    expect(vm.$el.textContent).to.contain('January')
    expect(vm.$el.textContent).to.contain('2016')
  })

  it('doesn\'t display link to edit salaah times when logged out', () => {
    const [vm] = setUpComponent()
    expect(vm.$el.textContent).to.not.contain('Edit Salaah times')
  })

  it('displays a link to edit salaah times when logged in & when masjid name is known', (done) => {
    const [vm, mockStore] = setUpComponent([{id: 1, name: 'test', humanReadableAddress: 'sdfa'}])
    mockStore.modules.LoggedInUserModule.state.loggedInUser = {userId: 1, email: 'test@example.com', accessToken: 'test', verified: true}
    Vue.nextTick(() => {
      expect(vm.$el.textContent).to.contain('Edit Salaah times')
      done()
    })
  })
})
