import 'babel-polyfill'
import Vue from 'vue'
import store from './store'
import router from './router'
import App from './App'

/* eslint-disable no-new */
new Vue({
  store,
  router,
  el: '#app',
  template: '<App/>',
  components: { App }
})
