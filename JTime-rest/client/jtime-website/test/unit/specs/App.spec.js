import Vue from 'vue'
import VueRouter from 'vue-router'
import Vuex from 'vuex'
import App from 'src/App'

Vue.use(VueRouter)

const testComponent = { template: '<div>test</div>' }

// Note that the paths in these routes aren't so important for testing purposes,
// as long as they are distinct from each other.
const routes = [
  { path: '/', name: 'home' },
  { path: '/all-masjids', name: 'all-masjids' },
  { path: '/help', name: 'help' },
  { path: '/masjid/id/year/month', name: 'masjid-times-for-month' },
  { path: '/masjid/id/year/month/edit', name: 'edit-salaah-times' }
].map((e) => {
  e.component = testComponent
  return e
})

const mockStore = {getters: {loggedIn: () => false}}

describe('App.vue', () => {
  let vm, router
  beforeEach(() => {
    router = new VueRouter({routes})
    vm = new Vue({
      el: document.createElement('div'),
      render: (h) => h(App),
      router,
      store: new Vuex.Store(mockStore)
    })
  })

  afterEach(() => {
    // This line ensures that the SignInButton sub-component gets destroyed,
    // which triggers its beforeDestroy hook, which calls clearTimeout on any
    // timer it has created. Deleting this line should sporadically cause a
    // test that tests this timer being run in SignInButton.spec.js to fail.
    vm.$destroy()
  })

  it('should display Home, All Masjids and Help navbar', () => {
    const text = vm.$el.textContent
    expect(text).to.match(/Home\s+All Masjids\s+Help/)
  })

  it('should display Home as the title', () => {
    const elem = vm.$el.getElementsByClassName('mdl-layout-title')[0]
    expect(elem.textContent).to.match(/^Home/)
  })

  const changingRouteTests = [
    { routeName: 'all-masjids', expected: 'All masjids' },
    { routeName: 'help', expected: 'Help' },
    { routeName: 'masjid-times-for-month', expected: 'Masjid times' },
    { routeName: 'edit-salaah-times', expected: 'Change salaah times' }
  ]

  changingRouteTests.forEach(e => {
    it(`should display ${e.expected} as the title when navigating to that route`, (done) => {
      router.push({name: e.routeName})
      Vue.nextTick(() => {
        const elem = vm.$el.getElementsByClassName('mdl-layout-title')[0]
        expect(elem.textContent).to.match(new RegExp(`^${e.expected}`))
        done()
      })
    })
  })
})
