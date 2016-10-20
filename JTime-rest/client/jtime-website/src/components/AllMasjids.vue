<template>
  <div>
    <p>This will be a list of all masjids</p>
    <button v-on:click="fetchMasjids">Get Masjids</button>
    <ul>
      <li v-for="masjid in masjids">
        {{ masjid.name }}
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: 'all-masjids',
  computed: {
    masjids () {
      return this.$store.state.masjids
    }
  },
  methods: {
    fetchMasjids () {
      console.log('I am here...')
      this.$store.dispatch('getMasjids')
    }
  }
}
</script>

<style scoped>
</style>
