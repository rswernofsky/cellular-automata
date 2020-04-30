# Cellular Automata

This project was created based on [this assignment](https://course.ccs.neu.edu/cs2510a/assignment7.html) for Northeastern's CS 2510 Accelerated course. It models simple binary rule-based cellular automata, arranged in a line. The program starts with a single row of square cells whose states are either on or off. On every tick, a new row of children cells are generated from the previous row using a specified rule. Certain rules cause interesting patterns to emerge. 

## What are tester.jar and javalib.jar?
I use [Benjamin Lerner](https://www.khoury.northeastern.edu/people/benjamin-lerner/)'s tester library (`tester.jar`) to test my code, as well as his image library (`javalib.jar`) to render it! To use these libraries, include them in whatever project contains these .java files as an external jar. To run the program, set your run configurations to use `tester.Main` as the main class, with the name of the `ExamplesAutomata` class as the program argument. 
