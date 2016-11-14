<template>
  <div>
    <h2>{{ masjidName }}</h2>
    <table class="mdl-data-table mdl-js-data-table center">
      <thead>
        <tr>
          <th class="mdl-data-table__cell--non-numeric">Salaah type</th>
          <th>Time</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="time in salaahTimes">
          <td class="mdl-data-table__cell--non-numeric">
            {{ salaahTypeCodeToString(time.type) }}
          </td>
          <td>{{ dateTimeStringToTimeString(time.datetime) }}</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script>
import {dateTimeStringToTimeString, salaahTypeCodeToString, upgradeElementMixin} from '../utils'

export default {
  name: 'masjid',
  computed: {
    masjidName () {
      const masjid = this.$store.state.MasjidsModule.masjids.find(e => e.id === this.masjidId)
      if (!masjid) {
        this.$store.dispatch('getMasjids')
      }
      return (masjid && masjid.name) || 'Loading...'
    },
    masjidId () {
      return parseInt(this.$route.params.id)
    },
    date () {
      return (new Date(this.$route.params.date))
    },
    salaahTimes () {
      return this.$store.state.SalaahTimesModule.salaahTimes[this.masjidId] || []
    }
  },
  methods: {
    getSalaahTimes () {
      if (this.masjidId && this.date) {
        this.$store.dispatch({
          type: 'getSalaahTimes',
          masjidId: this.masjidId,
          date: this.date})
      }
    },
    dateTimeStringToTimeString,
    salaahTypeCodeToString
  },
  created () {
    this.getSalaahTimes()
  },
  mixins: [upgradeElementMixin],
  watch: {
    '$route' () {
      this.getSalaahTimes()
    }
  }
}
</script>

<style scoped>
.center {
  margin: auto;
}
</style>
