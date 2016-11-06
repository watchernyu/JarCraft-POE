POE in JarCraft
========

Portfolio Online Evolution implemented in JarCraft.

JarCraft is a Starcraft combat simulator based on SparCraft project, implemented in JAVA to use with JNIBWAPI.
SparCraft is created by David Churchill at https://github.com/davechurchill/ualbertabot/tree/master/SparCraft. JarCraft is created by Balint Tillman, the original project is at https://github.com/tbalint/JarCraft. The code hosted here is an implementation of the algorithm Portfolio Online Evolution (POE) inside the JarCraft simulator.

Great thanks to David Churchill and Balint Tillman who provided support in our research!


NOTE:
This is a Eclipse project. Use Eclipse to open it will be the easiest approach.

You can modify and run a test by running the Test_final.java under src/bwmcts.test directory

The main POE players are under src/bwmcts.sparcraft.players directory.

Player_POE_final_no_cross: POE player with mutations only

Player_POE_final_with_cross: POE player with crossover implemented

They use helper classes that are in src/genetic:

Beast: it's just a class to store chromosome, with the combination of scripts and 

Population_final: a population to hold chromosomes, used by the POE player with mutation only

Population_final_with_cross: a population to hold chromosomes, used by the POE player with both mutation and crossover

The above are the most important files related to Portfolio Online Evolution. Currently there are still some messy redundancy in the project folder, please simply ignore them.
 
Thank you!