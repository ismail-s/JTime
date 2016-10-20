import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from './components/Home'
import AllMasjids from './components/AllMasjids'
import Help from './components/Help'

Vue.use(VueRouter)

const routes = [
  { path: '/', component: Home },
  { path: '/all-masjids', component: AllMasjids },
  { path: '/help', component: Help }
]

const router = new VueRouter({
  routes // short for routes: routes
})

export default router
