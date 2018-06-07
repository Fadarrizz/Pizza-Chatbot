# Design
This document describes how Pizza-Chatbot has been designed.

## Advanced sketches
![SignIn sketch](/doc/signIn.PNG)

The SignIn activity will make use of Firebase's authentication database.
You will be able to sign in with your Google account.
<hr>

#### <em>Both Ordering and Tracking will receive requests and return responses with Dialogflow</em>

![Ordering sketch](/doc/ordering.png)
Besides Dialogflow, the Ordering activity will make use of a custom created database in Firebase for getting pizzas and extras.
<hr>

![Tracking sketch](/doc/Tracking.PNG)

Besides Dialogflow, the Tracking activity will make use of a Firebase database and will receive order details.
It also will make use of Google's Transport Tracker API.

## Class diagrams
![diagram](/doc/diagram.PNG)

## APIs and Libraries
- [Dialogflow](https://dialogflow.com/)
- [Transport Tracker](https://developers.google.com/maps/solutions/transport-tracker/)
- [ChatMessageView](https://github.com/bassaer/ChatMessageView)

## Data sources
- Dialogflow:
  - Responses
- Firebase:
  - users
  - order
  - pizzas
  
## Database tables and fields
- Order
  - username
    - order_details STRING
    - creation_time STRING

- Pizzas
  - Margherita STRING
    - name STRING
    - price INTEGER
    - img STRING
  - ...
  - ...

- Size
  - small STRING
  - medium STRING
  - large STRING
  - family STRING

- Crust
  - classic STRING
  - cheesy STRING
  - gluten-free STRING
  
- Toppings
  - anchovies STRING
  - mozzarella STRING
  - tomato STRING
  - chili flakes STRING
