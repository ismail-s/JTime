import Vue from 'vue'
import Vuex from 'vuex'
import LoggedInUserModule from 'src/store/logged-in-user'
import SignOutButton from 'src/components/SignOutButton'

describe('SignOutButton.vue', () => {
  const testLoggedInUser = {userId: 1, email: 'test@example.com', accessToken: 'test'}
  function setUpComponent () {
    const mockStore = {modules: {LoggedInUserModule: {actions: {logout: sinon.spy()}, state: {loggedInUser: null}, getters: LoggedInUserModule.getters}}}
    const vm = new Vue({
      el: document.createElement('div'),
      render: (h) => h(SignOutButton),
      store: new Vuex.Store(mockStore)
    })
    return [vm, mockStore]
  }
  it('should display nothing when loggedInUser is null', () => {
    const [vm] = setUpComponent()
    expect(vm.$el.innerHTML)
      .to.be.empty
  })

  it('should display nothing when loggedInUser is not validated', done => {
    const [vm, mockStore] = setUpComponent()
    var mockLoggedInUser = {verified: false}
    Object.assign(mockLoggedInUser, testLoggedInUser)
    mockStore.modules.LoggedInUserModule.state.loggedInUser = mockLoggedInUser
    Vue.nextTick(() => {
      expect(vm.$el.innerHTML).to.be.empty
      done()
    })
  })

  it('should display a sign out link when loggedInUser is validated', done => {
    const [vm, mockStore] = setUpComponent()
    var mockLoggedInUser = {verified: true}
    Object.assign(mockLoggedInUser, testLoggedInUser)
    mockStore.modules.LoggedInUserModule.state.loggedInUser = mockLoggedInUser
    Vue.nextTick(() => {
      expect(vm.$el.textContent).to.equal('Sign Out')
      done()
    })
  })

  it('should sign out when clicked, and dispatch a logout action', done => {
    const [vm, mockStore] = setUpComponent()
    var mockLoggedInUser = {verified: true}
    Object.assign(mockLoggedInUser, testLoggedInUser)
    mockStore.modules.LoggedInUserModule.state.loggedInUser = mockLoggedInUser
    Vue.nextTick(() => {
      const thenSpy = sinon.spy()
      var signOutStub = sinon.stub().returns({then: thenSpy})
      window.gapi = {auth2: {getAuthInstance () { return {signOut: signOutStub} }}}
      vm.$el.click()
      expect(signOutStub).to.have.been.calledOnce
      expect(signOutStub).to.have.been.calledWithExactly()
      expect(thenSpy).to.have.been.calledOnce

      expect(mockStore.modules.LoggedInUserModule.actions.logout).to.not.have.been.calledOnce
      // Invoke the func that is to be called when google signOut completes
      thenSpy.getCall(0).args[0]()
      expect(mockStore.modules.LoggedInUserModule.actions.logout).to.have.been.calledOnce
      done()
    })
  })
})
