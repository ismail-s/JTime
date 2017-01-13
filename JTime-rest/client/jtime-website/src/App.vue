<template>
  <div id="app">
    <!-- Always shows a header, even in smaller screens. -->
    <div class="mdl-layout mdl-js-layout mdl-layout--fixed-header">
      <header class="mdl-layout__header">
    <div class="mdl-layout__header-row">
      <!-- Title -->
      <span class="mdl-layout-title">Home</span>
    </div>
  </header>
  <div class="mdl-layout__drawer">
    <span class="mdl-layout-title"></span>
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
  mixins: [upgradeElementMixin],
  methods: {
    showToast (message) {
      const snackbar = document.getElementById('snackbar')
      snackbar.MaterialSnackbar.showSnackbar({message})
    }
  },
  watch: {
    $route: function (to) {
      if (to.name !== 'masjid-times-for-month') {
        // Toggle drawer
        this.$el.firstChild.firstChild.MaterialLayout.toggleDrawer()
      }
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
