# Design
This document describes how Pizza-Chatbot has been designed.

## Advanced sketches
![SignIn sketch](/doc/signIn.png)
![Ordering sketch](/doc/Ordering.png)
![Tracking sketch](/doc/Tracking.png)

## Class diagrams
![diagram](/doc/diagram.png)

## APIs and Libraries
- [Dialogflow](https://dialogflow.com/)
- [Transport Tracker](https://developers.google.com/maps/solutions/transport-tracker/)
- [ChatMessageView](https://github.com/bassaer/ChatMessageView)

## Data sources
- Dialogflow:
  - Responses
  
## Database tables and fields
- Order
  - username
    - ordered STRING
    - creation_time STRING

- Pizza's
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
