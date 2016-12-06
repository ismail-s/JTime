import Vue from 'vue'
import Home from 'src/components/Home'

describe('Home.vue', () => {
  it('should render contents that say "This is the home page"', () => {
    const vm = new Vue({
      el: document.createElement('div'),
      render: (h) => h(Home)
    })
    expect(vm.$el.textContent)
      .to.match(/^Jamaat Times/)
  })
})
