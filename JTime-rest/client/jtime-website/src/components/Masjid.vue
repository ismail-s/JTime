<template>
  <div class="padBottom">
    <h2 v-if="masjidName">Salaah times for {{ masjidName }} for {{ monthAndYear }}</h2>
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
import {compareSalaahTypes, upgradeElementMixin} from '../utils'
import router from '../router'
import moment from 'moment'

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
    masjidId () {
      return parseInt(this.$route.params.id)
    },
    year () {
      return parseInt(this.$route.params.year)
    },
    month () {
      return parseInt(this.$route.params.month)
    },
    monthAndYear () {
      return moment().year(this.year).month(this.month).format('MMMM YYYY')
    },
    salaahTimes () {
      var times = this.$store.state.SalaahTimesModule.salaahTimes[this.masjidId] || []
      times = times.filter(t => t.datetime.getFullYear() === this.year && t.datetime.getMonth() === this.month)
      times.sort((a, b) => {
        const aDate = a.datetime.getDate()
        const bDate = b.datetime.getDate()
        if (aDate < bDate) {
          return -1
        } else if (aDate > bDate) {
          return 1
        } else {
          return compareSalaahTypes(a.type, b.type)
        }
      })
      const daysInMonth = moment().year(this.year).month(this.month).daysInMonth()
      var finalResult = []
      for (var i = 1; i <= daysInMonth; i++) {
        var obj = {date: i, dayOfWeek: moment().year(this.year).month(this.month).date(i).format('ddd')}
        while (times[0] && times[0].datetime.getDate() === i) {
          const time = times.shift()
          const datetime = moment(time.datetime).format('HH-mm')
          switch (time.type) {
            case 'f':
              obj.fajrTime = datetime
              break
            case 'z':
              obj.zoharTime = datetime
              break
            case 'a':
              obj.asrTime = datetime
              break
            case 'm':
              obj.magribTime = moment(time.datetime).add(5, 'minutes').format('HH-mm')
              break
            case 'e':
              obj.eshaTime = datetime
              break
          }
        }
        finalResult.push(obj)
      }
      return finalResult
    }
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
