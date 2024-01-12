# BluID SDK-Android-Sample
A sample android application for using BluID SDK 

## Get Started
1. Download the latest [release](https://github.com/blub0x/BluIDSDK-Android-Sample/releases) 
2. Clone this repository
3. Create a BluB0X Occupancy account

   • Please contact BluB0X support for getting an BluB0X Occupance Manager account.

4. Create a General User Account

   • Login into https://blusky.blub0x.com with BluB0X Occupancy Manager account credentials

   • Click on Add User and enter user information and once added, the user will automatically get an email with login credentials.

   • Further if needed BluB0X Occupancy Manager can assign credential(s) to the user(s)

5. Requirements

   •Android SDK platform 34 (minimum 26)

   • Android SDK Build-tools 34

   • Android SDK command line tools (latest)

   • Android SDK platform-tools 34.0.0

   • Gradle 8.0

   • Gradle Plugin for Android 8.1.1

## Setup Instructions

1. First step is to add “BluIDSDK.aar” as project dependency, download this file from [here](https://github.com/blub0x/BluIDSDK-Android-Sample/tree/main/app).

   •Copy BluIDSDK.aar file into your project directory,

   e.g. “app/BluIDSDK/BluIDSDK.aar”.

   •Add the BluIDSDK.aar file in your project, as dependency.
   Following line of code can be added into your app’s build.gradle file:

```kotlin
   implementation files('BluIDSDK/BluIDSDK-release.aar')
   implementation 'androidx.core:core-ktx:1.3.2'
   implementation 'androidx.appcompat:appcompat:1.2.0'
   implementation 'com.google.android.material:material:1.3.0'
   implementation 'androidx.constraintlayout:constraintlayout:2.0.4'
   implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
   implementation("androidx.navigation:navigation-ui-ktx:2.3.5")
   implementation("androidx.navigation:navigation-dynamic-features-fragment:2.3.5")
   implementation 'com.google.code.gson:gson:2.8.8'
   implementation 'androidx.legacy:legacy-support-v4:1.0.0'
   testImplementation 'junit:junit:4.+'
   implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1'
   implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.1'
   implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.3.1'
   implementation "androidx.lifecycle:lifecycle-extensions:2.2.0"
   implementation 'com.squareup.retrofit2:retrofit:2.9.0'
   implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
   implementation 'com.squareup.retrofit2:adapter-rxjava:2.7.1'
   implementation 'com.squareup.okhttp3:logging-interceptor:4.8.0'
   implementation group: 'org.bouncycastle', name: 'bcprov-jdk16', version: '1.45'
   api 'org.slf4j:slf4j-api:1.7.25'
   api 'com.github.tony19:logback-android:2.0.0'
```	

   •Note: The path for the .aar file may be different in your case. Use that path.

2. Next step is to add the required permissions into the app’s Manifest file. We’ll need user’s permission for Bluetooth, Location, Storage access, Internet, and Foreground service. To add the permissions, add following lines of code into your app’s Manifest file:
```kotlin
<uses-permission android:name="android.permission.BLUETOOTH" android:required="true" android:maxSdkVersion="30"/>
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:required="true" android:maxSdkVersion="30"/>
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" android:required="true"/>
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>
<uses-permission android:name="android.permission.INTERNET" android:required="true"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" android:required="true"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" android:required="true"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION"/>
<uses-permission  android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION"/>
```

3. After adding the required permissions in the Manifest file, you need to request the user for permissions. To do that, add the following lines in app’s Activity

- **For Android 11 or lower versions, request permission using following code block**
  ```kotlin
     val appPermissions = arrayListOf<String>(
              Manifest.permission.BLUETOOTH,
              Manifest.permission.BLUETOOTH_ADMIN,
              Manifest.permission.ACCESS_FINE_LOCATION,
              Manifest.permission.INTERNET,
              Manifest.permission.READ_EXTERNAL_STORAGE,
              Manifest.permission.WRITE_EXTERNAL_STORAGE,
              Manifest.permission.FOREGROUND_SERVICE,
              Manifest.permission.ACCESS_BACKGROUND_LOCATION,
        )
  ```
- **For Android 12 or higher versions, request permission using following code block**
  ```kotlin
  val appPermissions = arrayListOf<String>(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.INTERNET,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.FOREGROUND_SERVICE,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.FOREGROUND_SERVICE_LOCATION,
     )
  ```

4. After adding the global variables, you need to add the following lines of code into the onCreate function

```kotlin 
            ActivityCompat.requestPermissions(
               this, appPermissions, PERMISSION_REQUEST_CODE)
```

5. Next step is to register the DeviceStateObserver, to allow gesture based authentication  depending on access types.

   To register the DeviceStateObserver, add the following line, inside application tag of your app’s Manifest file

```kotlin
<receiver android:name="com.blub0x.BluIDSDK.utils.DeviceStateObserver" android:exported="false"></receiver>
```

6. We also need to register the BluIDSDK service in your app’s Manifest file.

```kotlin

<service android:name="com.blub0x.BluIDSDK.utils.BLECentral" android:enabled="true" android:stopWithTask="true" android:exported="false" android:foregroundServiceType="location" />

```

7. Once DeviceStateObserver is added to your Manifest file, we need to create an instance of DeviceStateObserver and register the receiver in your app’s Activity.

```kotlin
import com.blub0x.BluIDSDK.utils.DeviceStateObserver

var deviceStateObserver = DeviceStateObserver()
```

Add the following line in onCreate override function of app’s Activity

```kotlin
this.registerReceiver(deviceStateObserver, filter)
```


8. After the DeviceStateObserver has been registered, you need to register the BluIDSDK service with your app. To do that, add the following line inside the application tag of your app’s Manifest file
```kotlin
<service android:name="com.blub0x.BluIDSDK.utils.BLECentral" android:enabled="true" android:stopWithTask="true" android:exported="false" android:foregroundServiceType="location"/>
```
9. After the DeviceStateObserver has been registered, we also need to register BluIDSDK service in onCreate() function
```kotlin
val serviceIntent = Intent(this, BLECentral::class.java)
startService(serviceIntent)
```

10. When the application is about to close we need to stop the BluIDSDK service and unregister DeviceStateObserver. To do that, add the following lines of code in onDestroy() function
```kotlin
val serviceIntent = Intent(this, BLECentral::class.java)
stopService(serviceIntent)
this.unregisterReceiver(deviceStateObserver)
```

11. After all the set-up, the final step is to create an instance of BluIDSDK. To do that, first we have to import BluIDSDK class
```kotlin
import com.blub0x.BluIDSDK.BluIDSDK
```

12. After importing the BluIDSDK class, we can instantiate the class like below
```kotlin
var m_BluIDSDK_Client = BluIDSDK(
   environment,
   activity,
   deviceStateObserver
)
```

Parameters:

environment: We can choose a different environment from the BluSKY environments list, default is set to a production environment. 
activity: Pass activity instance of your 
deviceStateObserver:  Pass deviceStateObserver that we had created in step 9.

The initialization of your app is complete!

Please refer [Sample App's HomePage](https://github.com/blub0x/BluIDSDK-Android-Sample/blob/main/app/src/main/java/com/blub0x/bluidsdk_sample_app/fragments/HomeScreenFragment.kt) file for BluIDSDK Usage.

Follow this [documentation](https://blub0x.github.io/BluIDSDK-Android/index.html) for BluID SDK API reference.
