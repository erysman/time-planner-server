# Load testing

1. Set AUTH_API_KEY from [firebase project](https://console.firebase.google.com/project/time-planner-dc611/settings/general/web:NWRmN2JmMmQtMmY2YS00YThiLWIyY2QtNGU1MThiZWE2ZTE1?hl=pl)
2. Make sure that users are initialized ( or do it with initUsers.js)
3. Make sure to correctly set server url (BASE_URL)

run scripts locally with
```
 k6 run load/all.js
```

run script and save results to cloud
```
k6 run --out cloud load/all.js
```

three scenarios are available:
- load/all.js
- load/scheduleOnly.js
- load/allExceptSchedule.js
