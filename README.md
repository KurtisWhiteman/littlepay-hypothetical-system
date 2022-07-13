# Littlepay-hypothetical-system

Converts data relating to Littlepay industry

## Description

This Hypothetical Littlepay system takes a .csv in an expected format that contains raw data about Tap events. 
A Tap event is the literal Tapping OR NFC of a Smart Device onto a Terminal to commence or end a Trip on Public Transport.
The program takes these rows of data and converts them into Trips (From A to B) which will include but are not limited to pricing, duration and status.

### Dependencies

* Java 17
* SpringBoot
* Lombok
* Apache Commons

### Installing

* Clone this repository into your IDE (Made in Intellij)

### Executing program

* Run 'LittlepayApplication' (Main)
* Check resources folder for 'trips.csv'

### Executing tests

* Method 1: Run 'test' via maven
* Method 2: Method 2: Right-click in Project hierarchy, click "Run 'All Tests'"

### Assumptions

* I assumed that a database wasn't necessary. Though it would have made some things easier.
* I assumed logging was not necessary (beyond exceptions).
* I assumed extensive coding or JavaDoc entries was not necessary.
* I assumed a front-end was not necessary.

### Author

Kurtis Whiteman
