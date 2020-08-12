# Cellular Automata

This project was created with [Aidan](https://github.com/AidanSullivan03) for [this assignment](https://course.ccs.neu.edu/cs2510a/assignment7.html) for [this course](https://course.ccs.neu.edu/cs2510a/index.html). 

It models simple binary rule-based cellular automata, arranged in a line. The program starts with a single row of square cells whose states are either on or off. On every tick, a new row of children cells are generated from the previous row using a specified rule. Certain rules cause interesting patterns to emerge. 

## What are tester.jar and javalib.jar?
I use `tester.jar`, a testing library, and `javalib.jar`, an image library, both built by [my professor](https://www.khoury.northeastern.edu/people/benjamin-lerner/)! To use these libraries, include them in whatever project contains these .java files as an external jar. To run the program, set your run configurations to use `tester.Main` as the main class, with the name of the `ExamplesAutomata` class as the program argument. 
