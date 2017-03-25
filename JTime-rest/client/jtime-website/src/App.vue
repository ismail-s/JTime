<template>
  <div id="app">
    <!-- Always shows a header, even in smaller screens. -->
    <div class="mdl-layout mdl-js-layout mdl-layout--fixed-header">
      <header class="mdl-layout__header">
    <div class="mdl-layout__header-row">
      <!-- Title -->
      <span class="mdl-layout-title">{{title}}</span>
    </div>
  </header>
  <div class="mdl-layout__drawer">
    <nav class="mdl-navigation">
      <sign-in-button></sign-in-button>
      <sign-out-button></sign-out-button>
      <router-link to="/" class="mdl-navigation__link">Home</router-link>
      <router-link to="/all-masjids" class="mdl-navigation__link">All Masjids</router-link>
      <router-link to="/help" class="mdl-navigation__link">Help</router-link>
    </nav>
  </div>
  <main class="mdl-layout__content">
    <div class="page-content">
      <router-view></router-view>
    </div>
  </main>
</div>
<div id="snackbar" class="mdl-js-snackbar mdl-snackbar">
  <div class="mdl-snackbar__text"></div>
  <button class="mdl-snackbar__action" type="button"></button>
</div>
  </div>
</template>

<script>
import {upgradeElementMixin} from './utils'
import SignInButton from './components/SignInButton.vue'
import SignOutButton from './components/SignOutButton.vue'

export default {
  name: 'app',
  components: {
    'sign-in-button': SignInButton,
    'sign-out-button': SignOutButton
  },
  data () {
    return {
      title: 'Home'
    }
  },
  mixins: [upgradeElementMixin],
  methods: {
    showToast (message) {
      const snackbar = document.getElementById('snackbar')
      snackbar.MaterialSnackbar.showSnackbar({message})
    },
    setTitle (routeName) {
      const routeTitleMap = {
        'home': 'Home',
        'all-masjids': 'All masjids',
        'help': 'Help',
        'masjid-times-for-month': 'Masjid times',
        'edit-salaah-times': 'Change salaah times'
      }
      this.title = routeTitleMap[routeName] || routeTitleMap.home
    }
  },
  mounted () {
    this.setTitle(this.$route.name)
  },
  watch: {
    $route (to) {
      // Check if drawer is visible
      const drawerIsVisible = this.$el
        .getElementsByClassName('mdl-layout__drawer is-visible').length > 0
      if (drawerIsVisible === true) {
        // Hide drawer
        this.$el.firstChild.firstChild.MaterialLayout.toggleDrawer()
      }
      // Set title in app bar based on new value of $route.name
      this.setTitle(to.name)
    },
    '$store.state.ToastModule.toastQueue': function (queue) {
      const msg = queue[0]
      if (msg) {
        this.showToast(msg)
        this.$store.commit('removeOneToast')
      }
    }
  }
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}
</style>
