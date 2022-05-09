# MusicXMLCapstone

The pseudocode.txt file contains the beginnings of a pseudocode algorithm for how to transpose a piece of music.

The Capstone.musicxml file contains a small piece of music written by me in MusicXML. It has a soprano and bass part, is in the key of B-flat major, and is two measures long. It is the file that the transpose.java program reads from.

The NewCapstone.musicxml file contains the transposed version of Capstone.musicxml. It is where the transpose.java program writes to.

The transpose.java file reads from Capstone.musicxml and extracts a node tree for specific elements using the DocumentBuilder class. Next, it goes through a transposition algorithm to potentially change the key, the note, the alter (flat, sharp, or natural), and the octave based on the number of half steps and the direction of transposition. The node tree is then updated and written to the NewCapstone.musicxml file using the Transformer class.
