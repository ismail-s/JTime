<template>
  <div class="center">
    <div class="g-signin2" id="signInButton"></div>
  </div>
</template>

<script>
import {upgradeElementMixin} from '../utils'

export default {
  name: 'sign-in-button',
  mixins: [upgradeElementMixin],
  mounted () {
    window.gapi.load('auth2', () => {
      window.gapi.auth2.init({
        client_id: '654477471044-i8156m316nreihgdqoicsh0gktgqjaua.apps.googleusercontent.com'
      }).then((auth2) => {
        const currentUser = auth2.currentUser.get()
        if (currentUser && currentUser.getBasicProfile()) {
          this.$store.dispatch('checkServerLoginStatus')
        }
        const signInButton = document.getElementById('signInButton')
        auth2.attachClickHandler(signInButton, {}, (googleUser) => {
          const email = googleUser.getBasicProfile().getEmail()
          const idToken = googleUser.getAuthResponse().id_token
          this.$store.dispatch('login', {idToken, email})
        }, (error) => {
          this.$store.commit('toast', `Login failed: ${error.message}`)
        })
      })
    })
  }
}
</script>

<style>
.center {
  margin: auto;
}
</style>
