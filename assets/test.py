import requests
import pprint
import json

BASE_URL = "http://lhr.data.fr24.com/zones/fcgi/feed.js"

OPTIONS = "?bounds={0},{1},{2},{3}&faa=1&mlat=1&flarm=1&adsb=1&gnd=1&air=1&vehicles=1&estimated=1&maxage=900&gliders=1&stats=1&".format(49.3413832283658,48.96590422141798,-123.49031433105472,-122.58386901855408)

# get json data
r = requests.get(BASE_URL + OPTIONS)

plane_data = r.json()

if r.status_code == 200:
    # print json.dumps(r.json(), indent=4, sort_keys=True)
    for i in plane_data:
        if type(plane_data[i]) == list:
	    print plane_data[i]
            planeLat = plane_data[i][1]
            planeLng = plane_data[i][2]
            # planeRotation = plane_data[i][3] # this maybe ( just assuming )
            planeLandingAt = plane_data[i][12]
            planeName = plane_data[i][16]
            print planeLat,"|",planeLng,"|",planeLandingAt,"|",planeName
