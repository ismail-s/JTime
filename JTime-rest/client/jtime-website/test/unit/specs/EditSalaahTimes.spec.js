import Vue from 'vue'
import Vuex from 'vuex'
import router from 'src/router'
import EditSalaahTimes from 'src/components/EditSalaahTimes'

describe('EditSalaahTimes.vue', () => {
  function setUpComponent (masjids = [], routerParams = {id: 1, year: 2016, month: 0}) {
    const mockStore = {modules: {
      MasjidsModule: {
        actions: {getSalaahTimesForMonth: sinon.spy()}
      },
      SalaahTimesModule: {
        state: {salaahTimes: {}}
      },
      LoggedInUserModule: {
        state: {loggedInUser: null}
      }}}
    const vm = new Vue({
      el: document.createElement('div'),
      render: (h) => h(EditSalaahTimes),
      store: new Vuex.Store(mockStore),
      router
    })
    return new Promise((resolve, reject) => {
      // We need to explicitly wait for the router.push to complete as
      // EditSalaahTimes route is lazy-loaded ie asynchronously loaded.
      router.push({name: 'edit-salaah-times', params: routerParams}, () => {
        resolve([vm, mockStore])
      }, () => {
        resolve([vm, mockStore])
      })
    })
  }

  it('should initially display a title, help text and some buttons', done => {
    setUpComponent().then(([vm]) => {
      const strings = ['Edit Salaah times', 'Help',
        'Previous month', 'Next month', 'Save changes']
      strings.forEach(text => {
        expect(vm.$el.textContent).to.contain(text)
      })
      done()
    })
  })

  it('dispatches a request to get salaah times when mounted or when the route changes', done => {
    setUpComponent().then(([, mockStore]) => {
      const getSalaahTimesForMonth = mockStore.modules.MasjidsModule.actions.getSalaahTimesForMonth
      expect(getSalaahTimesForMonth).to.have.been.calledOnce
      expect(getSalaahTimesForMonth).to.have.been.calledWith(sinon.match.any, sinon.match(val => {
        return val.type === 'getSalaahTimesForMonth' &&
          val.masjidId === 1 &&
          val.year === 2016 &&
          val.month === 0
      }))
      // Navigate to a new page
      router.push({name: 'edit-salaah-times', params: {id: 1, year: 2016, month: 1}})
      Vue.nextTick(() => {
        expect(getSalaahTimesForMonth).to.have.been.calledTwice
        expect(getSalaahTimesForMonth).to.have.been.calledWith(sinon.match.any, sinon.match(val => {
          return val.type === 'getSalaahTimesForMonth' &&
            val.masjidId === 1 &&
            val.year === 2016 &&
            val.month === 1
        }))
        done()
      })
    })
  })

  describe('buttons to change months', () => {
    const changingMonthTests = [
      {
        title: 'navigate to the next month by clicking the next month button',
        clickOnPrevMonthButton: false,
        expectedYear: 2016,
        expectedMonth: 1},
      {title: 'navigate to the next month when current month is December',
        routerParams: {id: 1, year: 2016, month: 11},
        clickOnPrevMonthButton: false,
        expectedYear: 2017,
        expectedMonth: 0},
      {title: 'navigate to the previous month by clicking the previous month button',
        routerParams: {id: 1, year: 2016, month: 1},
        clickOnPrevMonthButton: true,
        expectedYear: 2016,
        expectedMonth: 0},
      {title: 'navigate to the previous month when current month is January',
        clickOnPrevMonthButton: true,
        expectedYear: 2015,
        expectedMonth: 11}]
    changingMonthTests.forEach(e => {
      it(e.title, done => {
        const setUpPromise = e.routerParams ? setUpComponent([], e.routerParams) : setUpComponent()
        setUpPromise.then(([vm]) => {
          const f = vm.$el.getElementsByTagName('button')
          const prevMonthButton = f[0]
          const nextMonthButton = f[1]
          const buttonToClick = e.clickOnPrevMonthButton ? prevMonthButton : nextMonthButton
          buttonToClick.click()
          expect(router.currentRoute.params).to.have.property('year', e.expectedYear)
          expect(router.currentRoute.params).to.have.property('month', e.expectedMonth)
          done()
        })
      })
    })
  })

  /* According to https://github.com/handsontable/handsontable/issues/1418#issuecomment-40050828,
     handsontable doesn't render the table contents unless the table is visible. Unless I can come
     up with a way of doing this, I can't test handsontable displaying times or saving times
  */
})
