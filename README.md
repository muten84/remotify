# remotify
Remotify aims to be a tiny demo for viewing from a remote host the the View of your mobile-app through the Internet.

##The server
remotify-poc contains a tiny server written in Java for viewing the frames sent by the mobile device.
The MainServer enables you to runt a standalone server. The server waits for incoming connections from the device.

##The client
remotidroid contains all the logic to send the current View showed to the screen.
Trough the START button you can run a connection to the server and start the sending of the stream of images to the server.

Once started the server and the app you can view froma  remote host an App developed in this way.

##Conclusion
The POC does not focus on performance and streaming issues. The entire project rely on the xuggler libraries for the server and the Android libraries for the client.
