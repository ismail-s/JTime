import Dexie from 'dexie'

const db = new Dexie('JTime-database')

db.version(1).stores({
  loggedInUser: '++, userId, email, accessToken'
})

function syncDexieWithVuex (db, store) {
  const promise = new Promise((resolve, reject) => {
    // We know that when this function is called, store.state.loggedInUser === null
    db.transaction('r', db.loggedInUser, () => {
      db.loggedInUser.count().then((c) => {
        if (c > 1) {
          // Table is invalid, wipe it clean
          db.loggedInUser.clear()
        } else if (c === 1) {
          // Add the persisted login to the store
          db.loggedInUser.toArray().then((arr) => {
            var user = arr[0]
            user.verified = false
            store.commit('loginUser', user)
          })
        }
      })
    }).then(resolve)
  })
  return promise
}

const dexiePlugin = store => {
  syncDexieWithVuex(db, store).then(() => {
    store.subscribe((mutation, state) => {
      switch (mutation.type) {
        case 'loginUser':
          db.transaction('rw', db.loggedInUser, () => {
            db.loggedInUser.clear().then(() => {
              db.loggedInUser.add(mutation.payload)
            })
          })
          break
        case 'clearLoggedInUser':
          db.transaction('rw', db.loggedInUser, () => {
            db.loggedInUser.clear()
          })
          break
      }
    })
  })
}

export default dexiePlugin
