export default {
  state: {
    toastQueue: [] // Queue of strings to be shown as toasts
  },
  mutations: {
    toast (state, msg) {
      state.toastQueue.push(msg)
    },
    removeOneToast (state) {
      state.toastQueue.shift()
    }
  }
}
