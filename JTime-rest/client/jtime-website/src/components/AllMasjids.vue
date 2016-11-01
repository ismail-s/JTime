<template>
  <div>
    <h2>All Masjids</h2>
    <div class="mdl-grid">
      <div class="mdl-cell mdl-cell--3-col-desktop mdl-cell--4-col mdl-cell--4-col-tablet mdl-cell--6-col-phone" v-for="masjid in masjids">
        <router-link :to="linkToMasjid(masjid.id)">
          <div class="mdl-card mdl-shadow--2dp">
            <div class="mdl-card__title">{{ masjid.name }}</div>
            <div class="mdl-card__supporting-text">{{ masjid.humanReadableAddress }}</div>
          </div>
        </router-link>
      </div>
    </div>
  </div>
</template>

<script>
import {dateToDateString, upgradeElementMixin} from '../utils'

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
    },
    linkToMasjid (id) {
      const today = new Date()
      return `/masjid/${id}/${dateToDateString(today)}`
    }
  },
  created () {
    this.fetchMasjids()
  },
  mixins: [upgradeElementMixin]
}
</script>

<style scoped>
</style>
