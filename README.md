# ChatNetwork

## Intro
The aim of this project is to develop a peer-to-peer application that allows users
to chat and share files with each other within a LAN. Chat messages are delivered
via UDP, while file sharing is done over TCP. It's also possible to enable encrytion
for file sharing. However, the current implementation is still very crude that it uses
a shared secret key for both encryption and decryption.

## Details
The protocol for messaging is inspired from IRC. In fact it's a much simpler version of IRC.

The project takes use of some of the [Apache Commons](https://commons.apache.org) project.

## Features
- Notification when someone joins or leaves the network
- Private chat
- Send file (to individual)
- Send file with encryption

## Execution
Run the `ChatNetwork.jar` file located at `out/artifacts/ChatNetwork/ChatNetwork.jar`

    java -jar ChatNetWork.jar [port]

The port parameter is optional, the default value is `4000`.
