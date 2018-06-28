# Final report

## Description
The pizzaChatbot application makes it quick and easy to order pizza's. Once logged in with Google, the user can start ordering a pizza by asking the bot. When an order is placed, it can be found in the tab 'Order'.

## Technical design

### Sign in
In the sign in activity, the user can sign in with his or her Google Account.

### Chatbot
The chatbot for this application is made with Dialogflow. A bot has been made to listen for certain words and starts the appropriate intent. When an intent has started, Dialogflow can give an action with its webhook.

### Chatting
All the chatting and pizza ordering happens in the messenger fragment. When the user has typed a message in the edittext and presses the button, a new chatmessage model is being made and pushed to Firebase.
The Firebase recycler adapter checks if the message is from the user or from the bot, and shows is accordingly.

In the options menu in the upper right corner, the user can delete the chatlog or sign out.

### Pizza ordering
Every message from the bot comes with an action.
This action will be checked and the right function will start.
Once a selection function has started, the right views will become visible.
The user can tap on a view and the choice will be send as a message to the bot.
Once everything has been chosen, a new order will be made, containing all of the user's choices.
At last, the order will be pushed to Firebase

### Orders
All created orders by the user can be seen in the order fragment.
On creation of the fragment, a list is filled with all orders.
An adapter creates cardviews with information of all orders and are shown in the view.

The user can press on an order to expand the card.
When the user long presses a card, a alert dialog pops up with the question if the user wants to delete the order.

In the options menu in the upper right corner, the user can delete all orders or sign out.

## Challenges during development

### Recyclerviews
I have found out that recyclerviews are very flexible, but it can be hard to get them how you want.
For the pizza selection view, I have used a recyclerview and designed it so that it comes in from the bottom.
All that is above will make place for the recyclerview, which is great.
But another recyclerview acting the same way on the same place, is not possible.
Therefore, you need to use the same recyclerview.
This was tricky, because when I filled the view with buttons the second time, a large gap between the buttons appeared.
Luckily, I could change the layout parameters to prefend this from happening.

### Gradle issues
When I transformed my activity in a fragment, I suddenly could not build my project anymore.
I received an weird error. After spending almost a full day, I found out (with the help of Renske) that there was a problem with an old dependancy.
After setting a newer version, I had to adjust my Firebase recycler adapters to their new requirements.
That was a big pain.

### Trial and error
At the end of this project, I noticed that when I had an idea I immediately started coding when I had found a solution.
After hours of implementing the idea, I almost always found out that it didn't work.
Because of trial and error I lost a lot of hours.

## Changes during development
In the beginning I had some cool plans for simulating a delivery scooter on a map.
But I didn't knew how to pull this of.
Due to a lack of time, I decided to show the status of the order instead.

I wanted for every order to have its own timer.
A timer would start as soon as the order has been set and would be placed in a cardview.
When the timer would come to zero, the status of the order would change.
I looked for a lot of ways to implement this, but none of them worked.

After a few days of trial and error, I decided to change my plans again.
Now, I only show a cardview with the details of the order.

## Conclusions

### Design
The design of the app is what I wanted. To me it looks pretty clean, that was what I was going for.

## Chatbot
Never have I made use of a chatbot before. So using it in this final project was a leap of faith. Luckily, it worked out pretty well. The chatbot was very easy to setup and use.
I only had to find out how you could use it in Android, because there was not much info about it.

## Final thoughts
I really liked this project and I really liked the how way of making apps was taught. 
You have to figure out how everything works yourself, but there is a skilled team to help you when you need it.
For me, that is the best way to learn.
