# Design
This document describes how Pizza-Chatbot has been designed.

## Advanced sketches
![Sketch](/doc/sketch_advanced.jpg)

## Class diagrams
![diagram](/doc/class_diagram.jpg)

## APIs and Frameworks
- [Dialogflow](https://dialogflow.com/)
- [BubbleView](https://github.com/lguipeng/BubbleView)

## Data sources
- Dialogflow:
  - Responses
  
## Database tables and fields
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
