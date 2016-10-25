<template>
  <div>
    <h2>All Masjids</h2>
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
      this.$store.dispatch('getMasjids')
    }
  },
  created: function () {
    this.fetchMasjids()
  }
}
</script>

<style scoped>
</style>
