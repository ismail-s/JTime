import Vue from 'vue'
import SignInButton from 'src/components/SignInButton'

describe('SignInButton.vue', () => {
  function setUpComponent (mockGapi = true) {
    if (mockGapi === true) {
      window.gapi = {load: sinon.spy()}
    }
    const vm = new Vue({
      el: document.createElement('div'),
      render: (h) => h(SignInButton)
    })
    return vm
  }
  it('should just contain a div', () => {
    const vm = setUpComponent()
    const innerHTML = vm.$el.innerHTML
    expect(innerHTML).to.contain('div')
    expect(innerHTML).to.contain('class="g-signin2"')
    expect(innerHTML).to.contain('id="signInButton"')
  })

  it('should call gapi.load on being mounted if it is available', () => {
    setUpComponent()
    expect(window.gapi.load).to.have.been.calledOnce
    expect(window.gapi.load).to.have.been.calledWith('auth2')
  })

  it('should call gapi.load when it becomes available', done => {
    window.gapi = undefined
    setUpComponent(false)
    setTimeout(() => {
      expect(window.gapi).to.be.undefined
      window.gapi = {load: sinon.spy()}
      setTimeout(() => {
        expect(window.gapi.load).to.have.been.calledOnce
        expect(window.gapi.load).to.have.been.calledWith('auth2')
        done()
      }, 101)
    }, 101)
  })
})
