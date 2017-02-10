<template>
  <div class="padBottom">
    <template v-if="masjidName">
      <h2>Salaah times for {{ masjidName }} for {{ monthAndYear }}</h2>
      <router-link :to="editSalaahTimesLink" v-if="loggedIn">Edit Salaah times</router-link>
    </template>
    <h2 v-else>Loading...</h2>
    <div class="pad-5px">
      <button class="mdl-button mdl-js-button mdl-button--raised mdl-button--accent mdl-js-ripple-effect" v-on:click="goToPrevMonth">Previous month</button>
      <button class="mdl-button mdl-js-button mdl-button--raised mdl-button--accent mdl-js-ripple-effect" v-on:click="goToNextMonth">Next month</button>
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
      <button class="mdl-button mdl-js-button mdl-button--raised mdl-button--accent mdl-js-ripple-effect" v-on:click="goToPrevMonth">Previous month</button>
      <button class="mdl-button mdl-js-button mdl-button--raised mdl-button--accent mdl-js-ripple-effect" v-on:click="goToNextMonth">Next month</button>
    </div>
  </div>
</template>

<script>
import router from '../router'
import {upgradeElementMixin} from '../utils'
import {commonComputedProperties, sortSalaahTimes} from '../masjid-utils'
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
    getSalaahTimesForMonth () {
      if (this.masjidId && this.year && this.month + 1) {
        this.$store.dispatch({
          type: 'getSalaahTimesForMonth',
          masjidId: this.masjidId,
          year: this.year,
          month: this.month
        })
      }
    },
    isToday (dayOfMonth) {
      return new Date().getDate() === dayOfMonth
    },
    goToNextMonth () {
      let [newYear, newMonth] = [this.year, this.month]
      if (this.month < 0 || this.month > 11) {
        // Invalid month, return early
        return
      } else if (this.month === 11) {
        newMonth = 0
        newYear = this.year + 1
      } else {
        newMonth = this.month + 1
      }
      router.push({name: 'masjid-times-for-month',
        params: {id: this.masjidId, year: newYear, month: newMonth}})
    },
    goToPrevMonth () {
      let [newYear, newMonth] = [this.year, this.month]
      if (this.month < 0 || this.month > 11) {
        // Invalid month, return early
        return
      } else if (this.month === 0) {
        newMonth = 11
        newYear = this.year - 1
      } else {
        newMonth = this.month - 1
      }
      router.push({name: 'masjid-times-for-month',
        params: {id: this.masjidId, year: newYear, month: newMonth}})
    }
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
