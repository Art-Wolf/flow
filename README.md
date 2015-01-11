Updates:

The mobile-release.apk has been signed and includes the wear-release.apk.

To run the application, the phone and wearable need to have development mode activated and debug via bluetooth activated on the wearable.

With the phone connected to the laptop, have the phone forward on the debug logs:

localhost:platform-tools johndoyle$ ./adb forward tcp:4444 localabstract:/adb-hub
localhost:platform-tools johndoyle$ ./adb connect localhost:4444

Once that is done, pass the logs for each tag into a seperate file:

localhost:platform-tools johndoyle$ ./adb -s localhost:4444 logcat Flow:I *:S > /tmp/watch.log
localhost:platform-tools johndoyle$ ./adb -s localhost:4444 logcat Flow-Present-Start:I *:S > /tmp/present-start.log
localhost:platform-tools johndoyle$ ./adb -s localhost:4444 logcat Flow-Training:I *:S > /tmp/training.log

With the log files created, the listener for each tag can be activated:

./monitor.sh /tmp/watch.log  osascript  next_slide.scpt
./monitor.sh /tmp/present-start.log  osascript  run_slideshow.scpt

With powerpoint open in the background, activate the wearable app.

Double tap the words "Speak to Launch", we eh.. never got around to changing that...

Say "Presentation" to have PowerPoint come first and go into Presentation mode.

With the presentation running and the wearable on your wriest:
1. Rotate your wriest to the left, the wearable will vibrate when it has reached the initial gesture stage.
2. Rotate your wriest to the right, the wearable will vibrate a second time when it recognized the complete gesture.

There is a timed duration - the gesture must be completed within 1.5 seconds. If you did not intend to activate the gesture, and you feel the first vibration, you must be concious of not accidently completing the gesture within the time.

Cheers,
John
