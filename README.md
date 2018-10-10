# gaitkeeper
Gait Logger app for Android

This app captures the on-board accelerometer data (x,y,z) as timestamp-tagged file on the SD-card.
Captured filenames are of the form <gait>_<timestamp>.csv where gait is [walk, jog, run, sprint], 
and timestamp is a standard Unix-type 32-bit timestamp. The timestamp portion can bbe cut&paste 
into one of the Internet timestamp converters (https://www.epochconverter.com).
 
Upon starting the app, there will be two rows of buttons. The first row has two standard buttons,
labeled "Start" and "Stop", while the second row has (currently) four radio buttons "walk", "jog",
"run", and "sprint".

When ready to start recording, the first action is to select a gait type: one of the radio buttons.
Next press the "Start" button. The "Start" button will begin to flash for five seconds, indicating 
that you should start preforming the gait you selected: e.g. if you selected the "jog" radio button, 
you should start jogging. This delay will allows you to reach the full gait you are trying to 
record.
  
To stop recording, press the "Stop" button.  This will close the logging file and make it 
available on the SD-card.  You can use one of the file manager apps available on Android to copy
the file to, say, some cloud storage such as google drive or dropbbox.
