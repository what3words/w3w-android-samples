# <img src="https://what3words.com/assets/images/w3w_square_red.png" width="64" height="64" alt="what3words">&nbsp; Google Map Sample 

Google Map Sample is a sample demonstration how using the what3words Map Component provides a straightforward way to add what3words to Google Map map and display features such as the what3words grid and what3words markers with what3words address.

 For more detailing information about the library please refer the [what3words android map components library](https://github.com/what3words/w3w-android-map-components) repository. 

<img src="readme/GoogleMapSample.png" width="320">

## Configuration

build.gradle
```gradle
// W3W API Map Lib
implementation "com.what3words:w3w-android-map-components:$what3words_android_map_components_version"
```

AndroidManifest.xml
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.yourpackage.yourapp">

    <uses-permission android:name="android.permission.INTERNET" />
    ...
```

add this to build.gradle (app level)

```
compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
}
```

add this the following proguard rules

```
-keep class com.what3words.javawrapper.request.* { *; }
-keep class com.what3words.javawrapper.response.* { *; }
```

# Features

## Enable what3words features in an existing Google Maps app using W3WGoogleMapsWrapper

To use Google Maps on your app, follow the quick start tutorial on the Google developer portal
here: https://developers.google.com/maps/documentation/android-sdk/start

After a successful Google Maps run, you can start using our GoogleMapsWrapper following these steps:

activity_main.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
    ...
<fragment xmlns:android="http://schemas.android.com/apk/res/android" android:id="@+id/map"
    android:name="com.google.android.gms.maps.SupportMapFragment"
    android:layout_width="match_parent" android:layout_height="match_parent" />
```

Kotlin

```Kotlin
class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(map: GoogleMap) {
        val apiWrapper = What3WordsV3("YOUR_API_KEY_HERE", this)
        val googleMapsWrapper = W3WGoogleMapsWrapper(
            this,
            map,
            apiWrapper
        )

        //example how to add a blue marker on a valid 3 word address and move camera to the added marker.
        googleMapsWrapper.addMarkerAtWords(
            "filled.count.soap",
            markerColor.BLUE,
            { marker ->
                val cameraPosition = CameraPosition.Builder()
                    .target(LatLng(marker.coordinates.lat, marker.coordinates.lng))
                    .zoom(19f)
                    .build()
                p0.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }, { error ->
                Log.e("MainActivity", "${error.message}")
            }
        )

        //click event on existing w3w markers on the map.
        w3wMapsWrapper.onMarkerClicked { marker ->
            Log.i("MainActivity", "clicked: ${marker.words}")
        }

        //REQUIRED
        map.setOnCameraIdleListener {
            //existing code here...

            //needed to draw the 3x3m grid on the map
            googleMapsWrapper.updateMap()
        }

        //REQUIRED
        map.setOnCameraMoveListener {
            //existing code here...

            //needed to draw the 3x3m grid on the map
            googleMapsWrapper.updateMove()
        }

        map.setOnMapClickListener { latLng ->
            //existing code here...

            //example of how to select a 3x3m w3w square using lat/lng
            googleMapsWrapper.selectAtCoordinates(latLng.latitude, latLng.longitude)
        }
    }
}
```

## Enable what3words features in an new Google Maps app using W3WGoogleMapFragment

Since you are creating a new app, you can always opt to use our W3WGoogleMapFragment. The main
advantage is that all the required events to draw the grid are done under the hood, resulting in
less boilerplate code and still having access to the Google Map to apply standard customization (
i.e. map types, etc.)

To use the what3words Google Maps Fragment in your app, first follow the quick start tutorial on the
Google developer portal here:  https://developers.google.com/maps/documentation/android-sdk/start.
This ensures that Google Maps can be used with the what3words Fragment.

After a successful Google Maps run, you can start using our W3WGoogleMapFragment following these
steps:

activity_main.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<fragment xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto" android:id="@+id/map"
    android:name="com.what3words.components.maps.views.W3WGoogleMapFragment"
    android:layout_width="match_parent" android:layout_height="match_parent" />
```

Kotlin

```Kotlin
class MainActivity : AppCompatActivity(), W3WGoogleMapFragment.OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as W3WGoogleMapFragment
        mapFragment.apiKey("YOUR_API_KEY_HERE", this)
    }

    override fun onMapReady(map: W3WMap) {
        //set language to get all the 3wa in the desired language (default english)
        map.setLanguage("en")

        //example how to add a blue marker on a valid 3 word address and move camera to the added marker.
        map.addMarkerAtWords(
            words = "filled.count.soap",
            markerColor = W3WMarkerColor.BLUE,
            zoomOption = W3WZoomOption.CENTER_AND_ZOOM,
            onSuccess = { marker ->
                Log.i(
                    "MainActivity",
                    "added ${marker.words} at ${marker.coordinates.lat}, ${marker.coordinates.lng}"
                )
            },
            onError = { error ->
                Log.e(
                    "MainActivity",
                    "${error.key}, ${error.message}"
                )
            }
        )

        map.onSquareSelected(
            onSuccess = { square, selectedByTouch, isMarked ->
                Log.i(
                    "MainActivity",
                    "square selected with words ${square.words}, was it touch? $selectedByTouch, is the square marked? $isMarked"
                )
                search.setSuggestionWithCoordinates(square)
            },
            onError = {
                Toast.makeText(
                    this,
                    "${it.key}, ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        )
        
        //if you want to access the google map instance inside W3WGoogleMapFragment do the following
        (map as? W3WGoogleMapFragment.Map)?.googleMap()?.let { googleMap ->
            googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        }
    }
}
```

<br><br>
### W3WMapBoxWrapper and W3WMapboxMapFragment is also support in Compose and XML:
 • Compose: GoogleMapsWrapperComposeActivity and GoogleMapFragmentActivity<br>
 • GoogleMap Compose Library: GoogleMapComposeLibraryActivity<br>
 • XML: GoogleMapsWrapperXmlActivity and GoogleMapXmlFragmentActivity
#### To testing it by switch the launcher Activity in the Manifest file
<!--    //Start Google Maps Compose Sample Launcher -->

        <!-- Sample for enable  what3words features in an existing Google Maps app using W3WGoogleMapsWrapper-->
        <activity
            android:name="com.what3words.samples.googlemaps.compose.GoogleMapsWrapperComposeActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.W3WComposeGoogleMap">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Sample for enable what3words features in an new Google Maps app using W3WGoogleMapFragment -->
        <!-- Change the launcher for testing -->
        <activity
            android:name="com.what3words.samples.googlemaps.compose.GoogleMapFragmentActivity"
            android:exported="true" >
        </activity>

        <!-- Sample for Use library with Jetpack Compose -->
        <!-- Change the launcher for testing -->
        <activity
            android:name="com.what3words.samples.googlemaps.compose.GoogleMapComposeLibraryActivity"
            android:exported="true" >
        </activity>

        <!--  End Compose section// -->


<!--    //Start Google Maps XML Sample Launcher -->

        <!-- Sample for enable what3words features in an existing Google Maps app using W3WGoogleMapsWrapper-->
        <activity
            android:name="com.what3words.samples.googlemaps.xml.GoogleMapsWrapperXmlActivity"
            android:theme="@style/Theme.W3WGoogleMap"
            android:exported="true">
            <!-- Change the launcher for testing -->
<!--            <intent-filter>-->
<!--                <action android:name="android.intent.action.MAIN" />-->
<!--                <category android:name="android.intent.category.LAUNCHER" />-->
<!--            </intent-filter>-->
        </activity>

        <!-- Sample for enable  what3words features in an new Google Maps app using W3WGoogleMapFragment -->
        <!-- Change the launcher for testing -->
        <activity
            android:name="com.what3words.samples.googlemaps.xml.GoogleMapXmlFragmentActivity"
            android:exported="true">
        </activity>

        <!--  End XML section// -->