/* Import modules */
const express = require('express')
const bodyParser = require('body-parser')
const querystring = require('querystring')
const https = require('https')
const cors = require('cors')
const path = require('path')

const app = express()
const serverPort = 8081 
const geocode_api_key = "AIzaSyBXZSHzGzFoJmKfPOT1rF2PxnAKMOFiR4Q"
const darksky_api_key = "30db0189551257ca9ab189bdccf1b1b4"

// Google search API
const cx = '014449360106520511784:neqlfbf50bm'
const google_search_api_key = 'AIzaSyBXZSHzGzFoJmKfPOT1rF2PxnAKMOFiR4Q';

app.use(cors())

function getGoogleImageUrl(location_address) {
    var url =  "https://www.googleapis.com/customsearch/v1?q=" + location_address + "%20wallpaper&cx="
      + cx + "&num=8&searchType=image&imgSize=huge&key=" + google_search_api_key;
	console.log(url)
    return url
}

function getWeatherInfo(lat, lon, location_address, res) {
    var url = 'https://api.darksky.net/forecast/' + darksky_api_key + '/' +
        lat + ',' + lon;

    https.get(url, (resp)=> {
        let data = '';

        resp.on('data', (chunk) => {
            data += chunk
        })

        resp.on('end', ()=> {
            data = JSON.parse(data)
			data['lat'] = lat
			data['lon'] = lon

			https.get(getGoogleImageUrl(location_address), (imgResp)=> {
				let img_data = '';
				imgResp.on('data', (chunk)=> {
					img_data += chunk
				})

				imgResp.on('end', ()=> {
					img_data = JSON.parse(img_data)
					data['location_address'] = location_address;
					data['cityImageUrl'] = img_data['items']
					console.log(data['cityImageUrl'])
					res.send(data)
					res.end()
				})
			})
        })

    }).on("error", (err) => {
        console.log(err)
        res.statusCode = 500
        res.send({error: true})
        res.end()
    })
}

app.get('/getDailyInfo/:lat/:lon/:time', (req, res)=>{
	var lat = req.params.lat
    var lon = req.params.lon
	var time = req.params.time
	
	var url = 'https://api.darksky.net/forecast/' + darksky_api_key + '/' + lat + ',' +
		lon + ',' + time	

    https.get(url, (resp, err)=> {
        let data = '';

        resp.on('data', (chunk) => {
            data += chunk
        })

        resp.on('end', ()=> {
            data = JSON.parse(data)
            res.send(data)
            res.end()
        })

    }).on("error", (err) => {
        console.log(err)
		res.statusCode = 500
		res.send({error: true})
		res.end()
    })

})

app.get('/getWeatherInfo/:lat/:lon/:location_address', (req, res)=>{
	var lat = req.params.lat
	var lon = req.params.lon
	var location_address = req.params.location_address;
	
	getWeatherInfo(lat, lon, location_address, res)
	
})

app.get('/getLoc/:city', (req, res)=>{
	var city = req.params.city.replace(/ /g,"+");
	var url = 'https://maps.googleapis.com/maps/api/geocode/json?address=' + 
				city + '&key=' + geocode_api_key;				

	https.get(url, (resp)=> {
		let data = '';
		
		resp.on('data', (chunk) => {
        	data += chunk
	    })

		resp.on('end', ()=> {
			data = JSON.parse(data)
			if (data['status'] && data['status'] != "OK") {
				data['error'] = true
				res.statuscode = 500
				res.send(data)
				res.end()
			} else {
				getWeatherInfo(data.results[0].geometry.location.lat, data.results[0].geometry.location.lng, city, res)
			}
		})

	}).on("error", (err) => {
        console.log(err)
        res.statusCode = 500
        res.send({error: true})
        res.end()
	})	

})


app.get('/getLoc/:city/:state/:country', (req, res)=>{
	var city = req.params.city.replace(/ /g,"+");
	var state = req.params.state.replace(/ /g,"+");
	var country = req.params.country.replace(/ /g,"+");
	var location_address = req.params.city + ',+' + req.params.state + ',+'+ req.params.country;	

	var url = 'https://maps.googleapis.com/maps/api/geocode/json?address=' + 
				city + ',' + state + ',' + country + '&key=' + geocode_api_key;				

	https.get(url, (resp)=> {
		let data = '';
		
		resp.on('data', (chunk) => {
        	data += chunk
	    })

		resp.on('end', ()=> {
			data = JSON.parse(data)
			if (data['status'] && data['status'] != "OK") {
				data['error'] = true
				res.statuscode = 500
				res.send(data)
				res.end()
			} else {
				getWeatherInfo(data.results[0].geometry.location.lat, data.results[0].geometry.location.lng, location_address, res)
			}
		})

	}).on("error", (err) => {
        console.log(err)
        res.statusCode = 500
        res.send({error: true})
        res.end()
	})	

})

app.get('/autocomplete/:input/:sessiontoken', (req, res) => {
	var input = req.params.input.replace(/ /g, "+")
	var sessiontoken = req.params.sessiontoken	

	var url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" + input +
		"&types=(cities)&key=" + geocode_api_key + "&sessiontoken=" + sessiontoken

	https.get(url, (resp)=> {
        let data = '';

        resp.on('data', (chunk) => {
            data += chunk
        })

        resp.on('end', ()=> {
            data = JSON.parse(data)
			console.log(data)
			res.send(data)
			res.end()
        })

    }).on("error", (err) => {
        console.log(err)
        res.statusCode = 500
        res.send({error: true})
        res.end()
    })

})

app.get('/empty', function(req, res) {
	data = {
		predictions: []
	}
	res.send(data)
	res.end()
})

var server = app.listen(serverPort)
