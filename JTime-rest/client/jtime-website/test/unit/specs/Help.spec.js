import Vue from 'vue'
import Help from 'src/components/Help'

describe('Help.vue', () => {
  it('should render contents that have the word "help" in them', () => {
    const vm = new Vue({
      el: document.createElement('div'),
      render: (h) => h(Help)
    })
    expect(vm.$el.textContent)
      .to.contain('help')
  })
})
