# DroidBotRecorder
An interaction recorder for semi-automated testing. (GSoC 2018)

## Installation

Please ensure that you have the **NDK** installed before installng this.

1. Clone the repository
2. Build the app, ensuring that the NDK is present.

## Running

1. Start an emulator from AVD manager
2. Start an adb shell with `adb shell`
3. In the adb shell, type the following
  ```
  $ su
  # setenforce 0
  # chmod 0666 /dev/input/event1
  # exit
  $ exit
  ```
4. Install the apk on the emulator with 
```
$ adb install path_to_app.apk
$ adb shell pm grant org.honeynet.droidbotrecorder android.permission.WRITE_EXTERNAL_STORAGE
```

5. Launch the app on the emulator
6. Ensure that you give permission for AccessibilityServices
7. Interact with the desired app. Your interactions are being logged.
8. Stop DroidBotRecorder (a button in a notificaiton will be implemented soon. For now, please force stop the app)
9. Take the interactions from the app's storage directory in `/sdcard/Android/`
