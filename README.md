Personal Spring Boot pet project written in Kotlin for controlling roller shutters via Shelly 2.5 devices based on the sun
position, temperature and cloudiness.

It accesses both the [Shelly Devices API](https://shelly-api-docs.shelly.cloud/) and the [Weather API](https://openweathermap.org/api).

Contains configurations for AWS or Linux systemd deployment.

The application runs on a Raspberry PI at my home.

## Properties required to run the project

A number of additional properties not contained in the source code must be provided to run the application. For example a `src/main/resources/application-myhome.properties` 
can be created with those properties and activated by specifying the Spring Boot profile `home`. 

* `shutters.zone-id`: Local time zone
* `shutters.latitude`: local position
* `shutters.longitude`: local position
* `shutters.api-webclients.shelly-api.server`: Shelly Devices API server
* `shutters.api-webclients.shelly-api.authorization-key`: Shelly Devices API authorization key
* `shutters.api-webclients.weather-api.key`: Weather API key

### Properties for closing roller shutters:

* `shutters.rules.sunset.offsetInMin`: Closing how many minutes after local sunset
* `shutters.rules.sunset.deviceIds`: comma separated list of device ids to close around sunset

### Properties for controlling roller positions based on sun positions. Multiple spring beans can be configured:

* `shutters.rules.sunShades[0].deviceIds`: Devices this rule applies to
* `shutters.rules.sunShades[0].targetShadePosition`: target position for the roller shutter to provide shade
* `shutters.rules.sunShades[0].minAzimuth`: sun azimuth to set the shade position for the roller shutter
* `shutters.rules.sunShades[0].maxAzimuth`: sun azimuth to put the roller shutter back up
* `shutters.rules.sunShades[0].minAltitude`: min altitude to keep the roller shutter in shade position
* `shutters.rules.sunShades[0].minTempInC`: min temperature to put the roller shutter into shade position
* `shutters.rules.sunShades[0].maxCloudiness`: max cloudiness to put the roller shutter into shade position
  
More sun shade rules can be configured with `shutters.rules.sunShades[1].deviceIds` etc.
