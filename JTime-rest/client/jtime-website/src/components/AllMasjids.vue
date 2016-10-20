<template>
  <div>
    <p>This will be a list of all masjids</p>
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
  }
}
</script>

<style scoped>
</style>
