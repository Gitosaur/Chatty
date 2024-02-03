# ![](src/main/resources/logo40px.png) Chatty

Zero-setup hidden chat with end-to-end encryption for Habbo.
Create private chatrooms and send end-to-end encrypted messages via a private server.

Chatty allows you to exchange end-to-end encrypted messages **using the normal Habbo chat**, without sending any packet to the Habbo servers. You can talk to other Chatty users cross-hotel and cross-room, and only your friends who know the password can read your messages.

<img src="./demo.gif">

<img src="./screenshot.png" alt="image" width="50%" height="auto">

## Terms of use

This extension sends the following information to an external server:

- Information of your Habbo: name, motto, figure (outfit), sex, hotel
- Name of chatroom and hashed chatroom password
- The encrypted chat messages you type inside a chatroom and style of the chat bubble
- Your current position inside a room (x,y coordinate)

The Chatty server does not store any data.
Users are responsible for the content of their messages. Users must also comply with Habbo's terms of use.
By using Chatty, you confirm that you have read and understood these terms and agree to them.

## How to use Chatty?

 After starting Chatty, press connect to connect to the private Server. you can change the server URL in the settings tab. In the bottom right of the Chatty client you can check if the connection to the Chatty server was successful.

 Inside the UI you see all available Chatty chatrooms and the Habbos inside it. You can either join a chatroom or create your own.

- Joining
  - Double click a chatroom to join it, you might have to enter a password.
  - You will now receive all messages that are being sent from Habbos in this chatroom
- Creating
  - To create a chatroom, click the button and enter a chatroom name and a password (optional)
  - Tell your friends the password so that they can join the room

At the bottom left you can activate or deactivate the Chatty client. When activated all your sent messages in Habbo will be redirected to the Chatty server and broadcastet to the chatroom you are member of. When deactivated, your messages are sent to the Habbo server like usual but you will still receive all the messages that are being sent inside the Chatty chatroom.
  
## Core features

- Secretly chat within the Habbo client
- Chatting cross-room and cross-hotel
- End-to-end encrypted
- Hashed chatroom passwords
- No typing cooldowns
- Chat without the fear of getting banend

## How it works

Your sent chat messages inside the Habbo client get encrypted and redirected to the chatty server and then broadcasted to the Habbos inside the chatroom you are member of. The Chatty server itself does not store any data nor does the Habbo server receive any messages.

## Host your own Chatty server

To host your own Chatty server, you can look at the [server implementation](https://github.com/Gitosaur/ChattyServer). Inside there is a Dockerfile to easily run your own server. By default it exposes a websocket server on port 8000.
