# JTime-website

This is a website that talks to JTime-rest, the REST api, and allows a user to
view and edit salaah times. The website is built using [Vuejs][].

The code is broken into separate files and should be fairly readable.

## Login/logout functionality

How login/logout works is a bit complicated. We use Google sign-in to
authenticate users. In the webpage there is a button that initiates the Google
sign-in process. If Google successfully authenticate a user, then a callback is
called in `SignInButton.vue`. In this callback, we dispatch a login action,
which calls some code in `logged-in-user.js`. This is where it gets complicated.
Google sign-in gives us an id token and email, and we send the id token only to
JTime-rest, which validates the token, extracts the email from it, creates a
user account if necessary, logs in the user (ie creates a new access token for
the user to allow the user to make authenticated requests to JTime-rest) and
returns their id token and their user id in JTime-rest. Then, in JTime-website,
we store all of this in the browser in [IndexedDB][] so that we don't need to
keep requesting access tokens each time the webpage is reloaded or revisited.

When a user reloads/revisits the website, then Google sign-in will call the
callback in `SignInButton.vue` as if the user had signed in manually. Thus, in
the login action in `logged-in-user.js`, we check to see if we already have a
logged in user saved in the browser with the same email (this isn't obvious just
looking at the code, as the file `dexie.js` syncs any saved user in the browser
to vuex straightaway, and the code in `logged-in-user.js` just checks vuex to
see if there is a logged in user there). If there is, and in the current browser
session we've checked if they're logged in, then we do nothing. Else, we send
a request to JTime-rest to see if they're logged in, logging them in again if
they're not. If the emails don't match, we just initiate a new login.

Like I said, complicated.

Logout simply sends a request to log the user out on JTime-rest and clears the
logged in user in vuex and in the browser.

[Vuejs]: https://vuejs.org/
[IndexedDB]: https://developer.mozilla.org/en/docs/Web/API/IndexedDB_API
