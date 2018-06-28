# Pizza-Chatbot
<em>By: Auke Geerts</em>

This is an app for for placing and tracking pizza orders via a chatbot.

## Problem statement
Ordering a pizza by browsing an app and its menu can be time consuming and not always as easy for users, especially if the UI is not user-friendly. Users are required to learn how an app has to be used, which can be difficult and can take some time. User interaction and customer commitment, as well as sales can be improved if a more natural and easy to use way of ordering and tracking is provided.

## Solution
By providing a simpler way for users to place and track orders using natural conversations, users can interact more intuitively and in a more productive manner, which will benefit customer commitment.

![Ordering sketch](/doc/ordering.png)
![Tracking sketch](/doc/Tracking.PNG)

### Main features
Ordering:
- Choose your desired pizza and pick your size
- Customize your pizza by adding extras
- Choose desired delivering or take-out time

Tracking:
- Know when the pizza is being made
- Know if your pizza is on the way
- Know when the pizza arrives
- See where the courier is on a map

### Minimum viable product (MVP)
All the ordering features will be implemented in the MVP.

## Prerequisites
### Data sources
- Pizza menu database: create it myself using Firebase and Google images
- [Dialogflow](https://dialogflow.com/)
  - Intents: create it myself 
  - responses: create it myself
  
### External components
- Firebase
- [Dialogflow](https://dialogflow.com/)
- [Transport Tracker](https://developers.google.com/maps/solutions/transport-tracker/)
- [ChatMessageView](https://github.com/bassaer/ChatMessageView)

### Review
Some apps make use of existing chatbots. They create the messenger functions and implement the chatbot.
Other apps that have their own chatbots are mostly from big companies that provide advanced bots.
Dominos, for instance, also used Dialogflow to create their chatbot. But they also implemented machine learning to listen for certain keywords. That is a step to far for me.

### Hardest parts
I have never used or created a chatbot. How this will go is very uncertain. The creation of the bot could take more than I imagined, the implementation of the bot could be difficult, the triggers for showing the right options when a choice has been made could be hard to code, and making sure that the user does not get to see any weird replies could be a difficulty.
