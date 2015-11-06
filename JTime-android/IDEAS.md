# Ideas for the app
## What should we be able to do in the app?
- See all masjids
- Select a masjid and see its times for today
  -Easily switch to other days nearby
  - Add a new time if logged in (like a quick add-not necessary, but a nice to
    have)
- Login if not logged in
- Add a new masjid
- A separate way for power-users to add lots of times in one go
  - Select a time, what namaaz, range of days to apply to
  - Clicking Submit shows whether submission succeeded/failed, but keeps
    user on same page, so they can quickly add more times.
- Set a favourite masjid(s)

## What activities will we need?
- View all masjids
- View favourite masjids (if only one fave masjid, then short circuit straight to todays times
- View times for a particular masjid
- Login
- Add a new masjid
- Add times

## General points
- To try and aid testing, we should keep code talking to the REST API as one class,
  an instance of which gets stored on classes that use the REST API. Then, we can
  mock it out in the unittests.
- Features that require logging in should only be available once logged in

## Flow for the user when opening app
- If favourite masjid(s) has been set:
	-  then straight to activity for viewing fave masjids
- Else:
	- View all masjids

## Sidebar
- Login/logout
- My masjids
- All masjids
- Add masjid (only if logged in)
- Add times
