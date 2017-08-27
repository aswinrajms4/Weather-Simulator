# Weather-Simulator
------------------------
A Toy application to generate the topology of selected locations on a random basis.

Pre requisite for running the code:
-----------------------------------
1. Need to have API keys for openweather REST API and google API.
2. Ensure that env variables WEATHER_API_KEYand ELEVATION_API_KEY are set.
3. Gradle is used to build the code.

Steps to build and execute the code:
------------------------------------
1. Ensure that JDK 1.7 is installed.
2. Set the env variables.(eg: set WEATHER_API_KEY=xxxxxx and set ELEVATION_API_KEY=xxxxxxxx)
3. Execute gradlew run.(Builds and executes the code)
4. Sample of generated results on a random basis is uploaded in the package with name 'generated_weather_data.txt'.

Packages:
---------
1. Open Weather REST API & Google Maps Elevation API (To fetch the weather data)
2. JSON 
