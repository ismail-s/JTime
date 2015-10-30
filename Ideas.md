# Some ideas on the REST API

We want to minimise the amount of REST calls made.

So...what is the general flow? What actions will be the most used?

- GET basic info on all masjids, basically the name and location
  (location not implemented yet...)
	- We get this REST endpoint for free
- GET times for a particular day for a particular masjid (incl. votes)
	- TODO-create this as a remote method, to reduce API calls
	- Future idea-methods to get multiple days, eg one week at a time
- up/downvote a particular time
	- with the below votes table, we can just stick with existing REST endpoints
- add new times (probs a bulk action)
	- Possible to just use existing endpoints

Votes can be done as a separate table, means we can avoid an audit table for the moment

- Id
- up/down vote
- voterId
- timeId
- createdAt
