<template>
  <div class="center">
    <div class="g-signin2" id="signInButton" data-onsuccess="onGoogleSignIn"></div>
  </div>
</template>

<script>
import {upgradeElementMixin} from '../utils'

export default {
  name: 'sign-in-button',
  mixins: [upgradeElementMixin],
  data () {
    return {
      timeoutId: null
    }
  },
  methods: {
    setUpGoogleApi () {
      if (!window.gapi_is_available && !window.gapi) {
        this.timeoutId = setTimeout(this.setUpGoogleApi, 100)
      } else {
        window.gapi && window.gapi.load('auth2', () => {
          window.gapi.auth2.init({
            client_id: '654477471044-i8156m316nreihgdqoicsh0gktgqjaua.apps.googleusercontent.com'
          })
        })
      }
    }
  },
  mounted () {
    this.setUpGoogleApi()
    window.onGoogleSignIn = googleUser => {
      const email = googleUser.getBasicProfile().getEmail()
      const idToken = googleUser.getAuthResponse().id_token
      this.$store.dispatch('login', {idToken, email})
    }
  },
  beforeDestroy () {
    clearTimeout(this.timeoutId)
  }
}
</script>

<style>
.center {
  margin: auto;
}
</style>
