## Dag 1

Vandaag eerste dag gewerkt aan prototype. 
Prototype moest chatmogelijkheid met bot zijn. Berichten kunnen versturen en reacties van bot terugkrijgen, in een messenger layout. 
Na gezocht te hebben hoe Dialogflow werkt en in Android geïmplementeerd kan worden, dit gecombineerd met Firebase en Recyclerview om berichten juist weer te kunnen geven. 

## Dag 2
Nu je tegen de bot kon praten en berichten terug kreeg, kon ik mij gaan focussen op het samenstellen van een pizza door middel van het kiezen van opties. Ik wilde dit doen door eerst een voorselectie te doen op de verschillende type pizza’s (veg en non-veg). Zodra de trigger voor het bestellen van een pizza door de gebruiker gegeven zou zijn, zou de bot moeten vragen wat voor type pizza de gebruiker zou willen. Er zouden dan knoppen te voorschijn moeten komen met de verschillende opties. Zodra er op één van de knoppen gedrukt zou worden, moet die optie als bericht naar de bot verstuurd worden, die hier dan op reageert. Ik wilde het hetzelfde ongeveer doen als in dit filmpje: https://www.youtube.com/watch?v=lmKqmPoU2ec 

Ik was van plan dit te gaan doen door een RelativeLayout toe te voegen aan messenger_activity met daarin twee knoppen voor veg en non-veg. De Recyclerview staat door middel van ‘layout_above’ boven de knoppen. De visibility staat op gone en wordt op visible gezet zodra de knoppen zichtbaar moeten worden.

Dit ging zoals gepland en werkte zoals ik had bedacht. Het lastige alleen was om de functie die de knoppen zichtbaar maakt op het juiste moment te laten starten. Ik had een globale onClick functie die ik voor elke button wil gebruiken, om onnodig veel code te voorkomen. Deze onClick was gemaakt om een request naar de Dialogflow API te sturen met het bericht van de gebruiker en een reply te krijgen en deze weer te geven.
Ik wilde dat de geselecteerde optie ook als bericht gestuurd zou worden naar Dialogflow, dus ik kon dit gebruiken, alleen moest het iets worden aangepast.

De String die gestuurd moest worden als bericht moest worden bepaald aan de hand van de View waarop geklikt werd. Dit heb ik opgelost door een if/else if te gebruiken om de tekst aan te passen naar de geselecteerd knop. Bijv: als er op ‘vegetarisch’ werd geklikt, moest ‘Vegetarian’ als string worden gestuurd.

Dialogflow heb ik zo ingesteld dat hij wacht op een specifiek antwoord, en stelt een vervolgvraag zodra een antwoord is gegeven.

## Dag 3
Gewerkt aan het weergeven van pizza’s op het juiste moment met een nieuwe RecyclerView
Design van pizza selector verbeterd
onClick maken voor alle items in RecyclerView was lastig. Dit moet handmatig worden gedaan.

## Dag 4
Pizza selector werkte nog niet helemaal goed, vandaag juiste gedrag gefixt.
Firebase autorisatie gedaan, inloggen met Google is nu mogelijk
Chatlog werd al bijgehouden in Firebase, maar nog niet per gebruiker. Nagedacht over hoe dit zou kunnen.
Options menu om log te wissen en om uit te loggen gemaakt in messenger fragment

## Dag 5
Verder gegaan aan chatlog per gebruiker. Dit gekoppeld aan Firebase uid. Werkt nu zoals ik wil
Nagedacht over het kiezen van extra’s op de pizza. 

## Dag 6
Gewerkt aan het kiezen van toppings op pizza
Ik wilde dezelfde recyclerview van de pizza selectie gebruiken voor het kiezen van de toppings. Een andere recyclerview zou niet op dezelfde manier in beeld kunnen komen, vanwege restricties aan de layout. Dit zorgde voor wat moeilijkheden. Uiteindelijk wel gelukt om toppings in dezelfde view te krijgen

## Dag 7
De toppingsselectie in dezelfde recyclerview als de pizzaselector, zorgde ervoor dat de view zo groot werd als het hele scherm. Wanneer de maat van de view werd aangepast, ontstond er een grote afstand tussen de knoppen. Dit opgelost door een functie toe te voegen die parameters van de recyclerview aanpast naar een vastgesteld formaat.
Begonnen aan onClick voor de toppings

## Dag 8
Gewerkt aan wat er gebeurd als de gebruiker een topping selecteert. De video waar ik mijn app op heb gebaseerd stuurt elke keer een topping door wanneer er op wordt geklikt. De view verdwijnt, topping wordt gestuurd en view komt weer tevoorschijn. Het lijkt mij veel makkelijker om de knop van kleur te laten veranderen als je er op klikt. Zo kan je selecteren en ook deselecteren.
Functie gemaakt voor het veranderen van de kleur van een knop wanneer er op wordt gedrukt.
Knop ‘done’ toegevoegd aan lijst, zodat die er standaard inzit. De done-knop hetzelfde laten gedragen als de andere selectie mogelijkheden, met daarnaast nog dat een nieuwe order met alle details wordt aangemaakt.

## Dag 9
Order op firebase laten opslaan wanneer aangemaakt
Eerst was ik van plan om de order via de bot op te kunnen vragen, maar ik heb besloten om het met fragments te doen. Eén fragment voor de chat en een ander voor de order.
De rest van de dag bezig geweest met het weghalen van een bug waardoor ik m’n project niet kon builden. Dit kwam door een oudere versie van Firebase-ui database dependency. Hoe dit opeens gebeurde is onduidelijk.

## Dag 10
Nagedacht over hoe ik de orders wilde laten zien. Ik wilde cardviews maken die groter worden als je er op klikt, zoals in de afbeelding te zien is.
Eerst was ik van plan een google maps kaart te laten zien met een scooter die langzaam naar je toe zou komen. Omdat het einde nadert heb ik besloten dit niet meer te doen. Daarentegen wil ik de status van de order laten zien met een progressbar die na een aantal minuten veranderd.
Layout gemaakt met een cardview voor de orders
Begonnen aan het expanden van de cardviews

## Dag 11
Gezocht naar de mogelijkheid om orders onder te verdelen in categorieën: being made, on the way en done. Maar na veel zoeken en uitproberen blijkt dit alleen (makkelijk) te kunnen als het aantal cards vastgesteld is.
Library voor expandable cardviews geïmplementeerd.

## Dag 12
Library was niet bedoeld voor wat ik wilde.
Verder gezocht naar mogelijkheden voor expandable cardviews met een progressbar of timer.
Geprobeerd om timers te starten op aparte threads en dit in the UI thread up te daten

## Dag 13
Spraakmogelijkheid ingebouwd
Expandable cardviews met details ingebouwd
Verwijderen van single cardview met longclick
Options menu voor verwijderen alle cardviews

## Dag 14
Comments toegevoegd
Send button animatie
Expandable cardview arrow annimatie

