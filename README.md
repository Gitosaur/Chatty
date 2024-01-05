# ![](src/main/resources/logo40px.png) Chatty 
Chatty is an extension to create private chatrooms inside Habbo. No other habbo except the members in the chatroom will be able to read the messages.

It even works cross-hotel, so you can chat with habbos across different hotels.

## How it works
Your sent chat messages inside the habbo client are redirected to the chatty server and then broadcasted to all habbos inside the chatrooms you are member of. The server itself does not store any data nor does the habbo server receive any messages.

## Host own Chatty server
To host your own Chatty server, you can look at the [server implementation](https://github.com/Gitosaur/ChattyServer). Inside there is a Dockerfile to easily run your own server.
