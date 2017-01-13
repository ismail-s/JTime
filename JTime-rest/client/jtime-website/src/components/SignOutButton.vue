<template>
  <a href="#" class="mdl-navigation__link" v-if="loggedIn" v-on:click.prevent="signOut">Sign Out</a>
</template>

<script>
import {upgradeElementMixin} from '../utils'

export default {
  name: 'sign-out-button',
  mixins: [upgradeElementMixin],
  methods: {
    signOut () {
      if (!window.gapi.auth2) {
        return
      }
      const auth2 = window.gapi.auth2.getAuthInstance()
      if (!auth2) {
        console.error(`auth2 instance unitialised: ${auth2}`)
        return
      }
      auth2.signOut().then(() => {
        this.$store.dispatch('logout')
      })
    }
  },
  computed: {
    loggedIn () {
      const loggedInUser = this.$store.state.LoggedInUserModule.loggedInUser
      return loggedInUser && loggedInUser.verified === true
    }
  }
}
</script>

<style>
</style>
