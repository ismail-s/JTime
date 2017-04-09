import moment from 'moment'
import Vue from 'vue'
import Vuex from 'vuex'
import Home from 'src/components/Home'

describe('Home.vue', () => {
  let mockGetCurrentPosition
  const actualGeoLocation = navigator.geolocation
  let vm, mockStore

  function changeGeoLocation (object) {
    // The following code is taken from
    // https://github.com/2gis/mock-geolocation/blob/57d29b159cb85585333dfda4a76bc72efd13d4c4/src/geolocate.js#L22-L37
    // MIT license Copyright (c) 2016 2GIS
    if (Object.defineProperty) {
      Object.defineProperty(navigator, 'geolocation', {
        get () { return object },
        configurable: true
      })
    } else if (navigator.__defineGetter__) {
      navigator.__defineGetter__('geolocation', () => object)
    } else {
      throw new Error('Cannot change navigator.geolocation method')
    }
  }

  beforeEach('mock out navigator.geolocation', () => {
    mockGetCurrentPosition = sinon.spy()
    const object = {getCurrentPosition: mockGetCurrentPosition}
    changeGeoLocation(object)
  })

  afterEach('unmock navigator.geolocation', () => changeGeoLocation(actualGeoLocation))

  beforeEach('set up component and store', () => {
    mockStore = {actions: {getTimesForNearbyMasjids: sinon.spy()},
      state: {SalaahTimesModule: {nearbySalaahTimes: []}}}
    vm = new Vue({
      el: document.createElement('div'),
      store: new Vuex.Store(mockStore),
      render: (h) => h(Home)
    })
  })

  afterEach('destroy component', () => vm.$destroy())

  it('should display the title', () => {
    expect(vm.$el.textContent)
      .to.match(/^Jamaat Times/)
  })

  it('should initially tell the user that we are trying to get their location', () => {
    expect(vm.$el.textContent)
      .to.contain('Trying to get your location...')
  })

  it('requests the users location on being mounted', () => {
    expect(mockGetCurrentPosition).to.have.been.calledOnce
  })

  it('correctly dispatches a rest call when location can be obtained', () => {
    expect(mockStore.actions.getTimesForNearbyMasjids)
      .to.not.have.been.called
    const successFunc = mockGetCurrentPosition.getCall(0).args[0]
    successFunc({coords: {latitude: 0, longitude: 0}})
    expect(mockStore.actions.getTimesForNearbyMasjids)
      .to.have.been.calledWith(sinon.match.any, sinon.match(val => {
        return val.type === 'getTimesForNearbyMasjids' &&
          val.latitude === 0 &&
          val.longitude === 0
      }))
  })

  it('shows an error message to the user when location can\'t be obtained', (done) => {
    const errorFunc = mockGetCurrentPosition.getCall(0).args[1]
    errorFunc({code: 1})
    expect(mockStore.actions.getTimesForNearbyMasjids)
      .to.not.have.been.called
    Vue.nextTick(() => {
      expect(vm.$el.textContent)
        .to.contain('Weren\'t able to get your location')
      done()
    })
  })

  describe('ui', () => {
    const now = moment('2016-01-01 12:00')
    let clock

    beforeEach(() => {
      vm.$children[0].locationApiIsAvailable = true
      vm.$children[0].locationOrError = {coords: {latitude: 0, longitude: 0}}
      clock = sinon.useFakeTimers(now.clone().valueOf())
    })

    afterEach(() => {
      clock.restore()
    })

    it('displays all times from the rest api', done => {
      mockStore.state.SalaahTimesModule.nearbySalaahTimes = [
        {masjidId: 1, masjidName: 'test1', masjidLocation: {lat: 1, lng: 1}, datetime: now.clone().hour(6).minute(30).toDate(), type: 'f'},
        {masjidId: 2, masjidName: 'test2', masjidLocation: {lat: 1, lng: 1.1}, datetime: now.clone().hour(6).minute(32).toDate(), type: 'z'},
        {masjidId: 3, masjidName: 'test3', masjidLocation: {lat: 1, lng: 1.2}, datetime: now.clone().hour(6).minute(33).toDate(), type: 'a'},
        {masjidId: 4, masjidName: 'test4', masjidLocation: {lat: 1, lng: 1.3}, datetime: now.clone().hour(6).minute(34).toDate(), type: 'm'},
        {masjidId: 5, masjidName: 'test5', masjidLocation: {lat: 2, lng: 2}, datetime: now.clone().hour(6).minute(35).toDate(), type: 'e'}]
      Vue.nextTick(() => {
        expect(vm.$el.textContent)
          .to.match(/Fajr\s*test1\s*06-30\s*Zohar\s*test2\s*06-32\s*Asr\s*test3\s*06-33\s*Magrib\s*test4\s*06-34\s*Esha\s*test5\s*06-35/)
        done()
      })
    })

    it('sorts times first by time, then by distance from current location', done => {
      mockStore.state.SalaahTimesModule.nearbySalaahTimes = [
        {masjidId: 1, masjidName: 'test1', masjidLocation: {lat: 1, lng: 1}, datetime: now.clone().hour(6).minute(30).toDate(), type: 'f'},
        {masjidId: 3, masjidName: 'test3', masjidLocation: {lat: 1, lng: 1.2}, datetime: now.clone().hour(6).minute(32).toDate(), type: 'f'},
        {masjidId: 2, masjidName: 'test2', masjidLocation: {lat: 1, lng: 1.1}, datetime: now.clone().hour(6).minute(32).toDate(), type: 'f'},
        {masjidId: 4, masjidName: 'test4', masjidLocation: {lat: 1, lng: 1.3}, datetime: now.clone().hour(6).minute(34).toDate(), type: 'f'},
        {masjidId: 5, masjidName: 'test5', masjidLocation: {lat: 2, lng: 2}, datetime: now.clone().hour(6).minute(35).toDate(), type: 'f'}]
      Vue.nextTick(() => {
        expect(vm.$el.textContent)
          .to.match(/Fajr\s*test1\s*06-30\s*test2\s*06-32\s*test3\s*06-32\s*test4\s*06-34\s*test5\s*06-35/)
        done()
      })
    })

    const middleOfYear = moment('2016-06-01 12:00')

    // The first 2 tests here are important for checking that timezone changes don't affect the calculation of closest time
    const highlightedClosestTimeTests = [{
      title: 'highlights the closest time correctly when some times are in the future',
      salaahTimes: [
        {masjidId: 1, masjidName: 'test1', masjidLocation: {lat: 1, lng: 1}, datetime: now.clone().utc().hour(11).minute(56).toDate(), type: 'z'},
        {masjidId: 3, masjidName: 'test2', masjidLocation: {lat: 1, lng: 1.2}, datetime: now.clone().utc().hour(11).minute(57).toDate(), type: 'z'},
        {masjidId: 2, masjidName: 'test3', masjidLocation: {lat: 1, lng: 1.1}, datetime: now.clone().utc().hour(11).minute(58).toDate(), type: 'z'},
        {masjidId: 4, masjidName: 'test4', masjidLocation: {lat: 1, lng: 1.3}, datetime: now.clone().utc().hour(11).minute(59).toDate(), type: 'z'},
        {masjidId: 5, masjidName: 'test5', masjidLocation: {lat: 2, lng: 2}, datetime: now.clone().utc().hour(12).minute(0).toDate(), type: 'z'}],
      expectedMasjidName: 'test2',
      expectedSalaahTime: '11-57'
    }, {
      title: 'highlights the closest time correctly when some times are in the future during the middle of the year',
      today: middleOfYear.clone(),
      salaahTimes: [
        {masjidId: 1, masjidName: 'test1', masjidLocation: {lat: 1, lng: 1}, datetime: middleOfYear.clone().utc().hour(11).minute(56).toDate(), type: 'z'},
        {masjidId: 3, masjidName: 'test2', masjidLocation: {lat: 1, lng: 1.2}, datetime: middleOfYear.clone().utc().hour(11).minute(57).toDate(), type: 'z'},
        {masjidId: 2, masjidName: 'test3', masjidLocation: {lat: 1, lng: 1.1}, datetime: middleOfYear.clone().utc().hour(11).minute(58).toDate(), type: 'z'},
        {masjidId: 4, masjidName: 'test4', masjidLocation: {lat: 1, lng: 1.3}, datetime: middleOfYear.clone().utc().hour(11).minute(59).toDate(), type: 'z'},
        {masjidId: 5, masjidName: 'test5', masjidLocation: {lat: 2, lng: 2}, datetime: middleOfYear.clone().utc().hour(12).minute(0).toDate(), type: 'z'}],
      expectedMasjidName: 'test2',
      expectedSalaahTime: '11-57'
    }, {
      title: 'highlights the closest time correctly when all times are in the past',
      salaahTimes: [
        {masjidId: 1, masjidName: 'test1', masjidLocation: {lat: 1, lng: 1}, datetime: now.clone().utc().hour(6).minute(30).toDate(), type: 'f'},
        {masjidId: 3, masjidName: 'test2', masjidLocation: {lat: 1, lng: 1.2}, datetime: now.clone().utc().hour(6).minute(29).toDate(), type: 'f'},
        {masjidId: 2, masjidName: 'test3', masjidLocation: {lat: 1, lng: 1.1}, datetime: now.clone().utc().hour(6).minute(31).toDate(), type: 'f'},
        {masjidId: 4, masjidName: 'test4', masjidLocation: {lat: 1, lng: 1.3}, datetime: now.clone().utc().hour(6).minute(31).toDate(), type: 'f'},
        {masjidId: 5, masjidName: 'test5', masjidLocation: {lat: 2, lng: 2}, datetime: now.clone().utc().hour(6).minute(32).toDate(), type: 'f'}],
      expectedMasjidName: 'test5',
      expectedSalaahTime: '06-32'
    }, {
      title: 'highlights the closest time correctly when all times are in the past during the middle of the year',
      today: middleOfYear.clone(),
      salaahTimes: [
        {masjidId: 1, masjidName: 'test1', masjidLocation: {lat: 1, lng: 1}, datetime: middleOfYear.clone().utc().hour(6).minute(30).toDate(), type: 'f'},
        {masjidId: 3, masjidName: 'test2', masjidLocation: {lat: 1, lng: 1.2}, datetime: middleOfYear.clone().utc().hour(6).minute(29).toDate(), type: 'f'},
        {masjidId: 2, masjidName: 'test3', masjidLocation: {lat: 1, lng: 1.1}, datetime: middleOfYear.clone().utc().hour(6).minute(31).toDate(), type: 'f'},
        {masjidId: 4, masjidName: 'test4', masjidLocation: {lat: 1, lng: 1.3}, datetime: middleOfYear.clone().utc().hour(6).minute(31).toDate(), type: 'f'},
        {masjidId: 5, masjidName: 'test5', masjidLocation: {lat: 2, lng: 2}, datetime: middleOfYear.clone().utc().hour(6).minute(32).toDate(), type: 'f'}],
      expectedMasjidName: 'test5',
      expectedSalaahTime: '06-32'
    }]
    highlightedClosestTimeTests.forEach(e => {
      it(e.title, done => {
        if (e.today) {
          clock.restore()
          clock = sinon.useFakeTimers(e.today.valueOf())
        }
        mockStore.state.SalaahTimesModule.nearbySalaahTimes = e.salaahTimes
        Vue.nextTick(() => {
          const highlightedRow = vm.$el.getElementsByClassName('highlight-row')[0]
          expect(highlightedRow).to.exist
          const highlightedMasjidName = highlightedRow.children[0]
          const highlightedSalaahTime = highlightedRow.children[1]
          expect(highlightedMasjidName.textContent).to.equal(e.expectedMasjidName)
          expect(highlightedSalaahTime.textContent).to.equal(e.expectedSalaahTime)
          done()
        })
      })
    })

    const updateTimeTests = [{
      title: 'refreshes the ui at the correct time to always have the correct closest time highlighted',
      today: now.clone()
    }, {
      title: 'refreshes the ui at the correct time to always have the correct closest time highlighted when in the middle of the year',
      today: middleOfYear.clone()
    }]
    updateTimeTests.forEach(e => {
      it(e.title, done => {
        /* On component mount, navigator.geolocation.getCurrentPosition gets
           called once. In this test, we check that it gets called again at the
           correct time when nearbySalaahTimes in the store are changed. */
        clock.restore()
        clock = sinon.useFakeTimers(e.today.valueOf())
        mockStore.state.SalaahTimesModule.nearbySalaahTimes = [
          {masjidId: 1, masjidName: 'test1', masjidLocation: {lat: 1, lng: 1}, datetime: e.today.clone().utc().hour(12).minute(0).toDate(), type: 'z'},
          {masjidId: 2, masjidName: 'test2', masjidLocation: {lat: 1, lng: 1.1}, datetime: e.today.clone().utc().hour(12).minute(5).toDate(), type: 'z'}]
        Vue.nextTick(() => {
          // Clock is currently at 12:00, first salaah time is highlighted
          expect(mockGetCurrentPosition).to.have.been.calledOnce
          clock.tick(3 * 60 * 1000)
          Vue.nextTick(() => {
            // Clock is now at 12:03:00
            expect(mockGetCurrentPosition).to.have.been.calledOnce
            clock.tick(1)
            Vue.nextTick(() => {
              // Clock is now at 12:03:00.001
              expect(mockGetCurrentPosition).to.have.been.calledTwice
              done()
            })
          })
        })
      })
    })
  })
})
