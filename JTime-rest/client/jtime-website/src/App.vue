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
      <router-link to="/" class="mdl-navigation__link" >Home</router-link>
      <router-link to="/all-masjids" class="mdl-navigation__link" >All Masjids</router-link>
      <router-link to="/help" class="mdl-navigation__link" >Help</router-link>
    </nav>
  </div>
  <main class="mdl-layout__content">
    <div class="page-content">
      <router-view></router-view>
    </div>
  </main>
</div>
  </div>
</template>

<script>
import {upgradeElementMixin} from './utils'

export default {
  name: 'app',
  mixins: [upgradeElementMixin],
  watch: {
    $route: function (to) {
      if (to.name !== 'masjid-id-date') {
        // Toggle drawer
        this.$el.firstChild.firstChild.MaterialLayout.toggleDrawer()
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
