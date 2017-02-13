<template>
  <div class="padBottom">
    <template v-if="masjidName">
      <h2>Salaah times for {{ masjidName }} for {{ monthAndYear }}</h2>
      <router-link :to="editSalaahTimesLink" v-if="loggedIn">Edit Salaah times</router-link>
    </template>
    <h2 v-else>Loading...</h2>
    <div class="pad-5px">
      <button class="mdl-button mdl-js-button mdl-button--raised mdl-js-ripple-effect" v-on:click="goToPrevMonth">Previous month</button>
      <button class="mdl-button mdl-js-button mdl-button--raised mdl-js-ripple-effect" v-on:click="goToNextMonth">Next month</button>
    </div>
    <table class="mdl-data-table mdl-js-data-table center">
      <thead>
        <tr>
          <th>Date</th>
          <th class="mdl-data-table__cell--non-numeric">Day</th>
          <th>Fajr</th>
          <th>Zohar</th>
          <th>Asr</th>
          <th>Magrib</th>
          <th>Esha</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="day in salaahTimes" v-bind:class="isToday(day.date) ? 'highlight-row' : ''">
          <td>{{ day.date }}</td>
          <td class="mdl-data-table__cell--non-numeric">{{ day.dayOfWeek }}</td>
          <td>{{ day.fajrTime }}</td>
          <td>{{ day.zoharTime }}</td>
          <td>{{ day.asrTime }}</td>
          <td>{{ day.magribTime }}</td>
          <td>{{ day.eshaTime }}</td>
        </tr>
      </tbody>
    </table>
    <div class="pad-5px">
      <button class="mdl-button mdl-js-button mdl-button--raised mdl-js-ripple-effect" v-on:click="goToPrevMonth">Previous month</button>
      <button class="mdl-button mdl-js-button mdl-button--raised mdl-js-ripple-effect" v-on:click="goToNextMonth">Next month</button>
    </div>
  </div>
</template>

<script>
import {upgradeElementMixin} from '../utils'
import {commonComputedProperties, commonMethods, sortSalaahTimes} from '../masjid-utils'
import moment from 'moment'
import {mapGetters} from 'vuex'

export default {
  name: 'masjid',
  computed: {
    masjidName () {
      const masjid = this.$store.state.MasjidsModule.masjids.find(e => e.id === this.masjidId)
      if (!masjid) {
        this.$store.dispatch('getMasjids')
      }
      return (masjid && masjid.name) || ''
    },
    ...commonComputedProperties,
    monthAndYear () {
      return moment().year(this.year).month(this.month).format('MMMM YYYY')
    },
    salaahTimes () {
      var times = this.$store.state.SalaahTimesModule.salaahTimes[this.masjidId] || []
      return sortSalaahTimes(times, this.year, this.month)
    },
    editSalaahTimesLink () {
      return `/masjid/${this.masjidId}/${this.year}/${this.month}/edit`
    },
    ...mapGetters(['loggedIn'])
  },
  methods: {
    isToday (dayOfMonth) {
      return new Date().getDate() === dayOfMonth
    },
    ...commonMethods
  },
  created () {
    this.getSalaahTimesForMonth()
  },
  mixins: [upgradeElementMixin],
  watch: {
    '$route' () {
      this.getSalaahTimesForMonth()
    }
  }
}
</script>

<style scoped>
.center {
  margin: auto;
}

.padBottom {
  padding-bottom: 10px;
}

.pad-5px {
  padding: 5px;
}

.highlight-row {
  background-color: #f48fb1; /*material design pink 200*/
}
</style>
