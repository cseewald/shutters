Pet Spring Boot project written in Kotlin for controlling roller shutters via Shelly 2.5 devices.

# Envs for running the app

* `SHUTTERS_WEBCLIENT_SHELLY_API_AUTHORIZATION_KEY`: Shelly cloud authorization key
* `SHUTTERS_WEBCLIENT_SHELLY_API_SERVER`: Shelly cloud server
* `SHUTTERS_WEBCLIENT_WEATHER_API_KEY`: weatherapi.com Api Key
* `SHUTTERS_LATITUDE`: Position for sun position calculations
* `SHUTTERS_LONGITUDE`: Position for sun position calculations
* `SHUTTERS_ZONE_ID`: Time zone needed for sun position calculations
* `SHUTTERS_RULES_SUNSET_DEVICEIDS`: device ids to be controlled by sunset rule
* `SHUTTERS_RULES_SUNSET_OFFSETINMIN`: negative/positive offset for sunset rule
* `SHUTTERS_RULES_SUNSET_OVERRIDETIME`: explicit time for execution of sunset rule for debugging purposes
