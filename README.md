# Binary Reject Engine

Based on the [text manipulation
engine](https://github.com/PredictionIO/template-scala-parallel-textclassification),
this is a proof of concept for deploying an algorithm to production.

Simply compares a string that's passed in and returns reject if it
matches a few "magic" reject words or no-reject otherwise. Meant to be a
first-pass filter to remove obvious documents from being considered in
my [Reading List Hobby
App](https://github.com/brycemcd/email-listicle-frontend)

# Release Information

## Version 0.1

Based on text manipulation engine, changes name and functionality of
DataSource, Preparator, Algo and Server to get a better feel for the
development process
