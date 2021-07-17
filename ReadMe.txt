		VideoIntensityAnalysis:
		Incorporates OpenCV.
	

	This is the source code for the program, which is designed to
analyze a video file frame by frame and get the average brightness of
each frame, as well as a standardized measure of absolute difference
between each two frames, then print it out to a .csv file.

	I initially designed this to analyze films and try to discover
a trend over time of films getting more visually intense. However, it
has a number of possible uses, including quickly analyzing videos in 
order to find sections that may trigger epilepsy or other types of 
sensitivity to visual stimuli.

	In order for the program to work properly, the OpenCV3xx.jar 
file in the Dependencies folder needs to be in the Classpath, with the
native library location for that Jar file set to Dependencies/x64 
folder. How I did this was use Eclipse to create a user library
called opencv which included that Jar file, then set the native
library location of both the user library and the Jar itself
in the Classpath to Dependencies/x64.

	Also included is a zip file that contains a runnable Jar file
which is the same as the VideoAnalysis program. Just unzip the folder
and run the Jar file with:

java -jar VideoIntensity.jar

	This is the best way to use the program. Further information
about the Jar file can be found in the zip file's ReadMe.
