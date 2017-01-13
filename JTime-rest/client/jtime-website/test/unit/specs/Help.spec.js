import Vue from 'vue'
import Help from 'src/components/Help'

describe('Help.vue', () => {
  it('should render contents that have the word "help" in them', () => {
    const vm = new Vue({
      el: document.createElement('div'),
      render: (h) => h(Help)
    })
    const text = vm.$el.textContent
    expect(text).to.contain('Help')
    expect(text).to.contain('Vuejs (MIT license)')
    expect(text).to.contain('Vuex (MIT license)')
    expect(text).to.contain('Vue-router (MIT license)')
    expect(text).to.contain('Vue-resource (MIT license)')
  })
})
