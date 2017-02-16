import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from './components/Home'
import AllMasjids from './components/AllMasjids'
import EditSalaahTimes from './components/EditSalaahTimes'
import Masjid from './components/Masjid'
import Help from './components/Help'

Vue.use(VueRouter)

const routes = [
  { path: '/', name: 'home', component: Home },
  { path: '/all-masjids', name: 'all-masjids', component: AllMasjids },
  { path: '/help', name: 'help', component: Help },
  { path: '/masjid/:id/:year/:month', name: 'masjid-times-for-month', component: Masjid },
  { path: '/masjid/:id/:year/:month/edit', name: 'edit-salaah-times', component: EditSalaahTimes }
]

const router = new VueRouter({
  routes // short for routes: routes
})

export default router
