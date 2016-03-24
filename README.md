# Chess-AI
Chess AI project from CS76 (Artificial Intelligence)

1) How to play AI vs. AI
To play a game of AI vs. AI, go to ChessClient.java and look at lines 78 to 81. The current setup is that black is the AI and white is the human player. If desired, the user can comment out the line that creates a human player and instead use both as AI. Note that the constructor for MinimaxAI needs to know who (black or white) is the maximizing player.

2) Use different starting positions
The user can test the game using different starting positions. To switch the positions, look at ChessGame.java and go to the constructor. There are several positions that have been commented out. The default one used the standard start.

3) Change the maximum search depth
The maximum search depth is set at 5, by default. To change the depth, refer to MinimaxAI.java and look at the instance variables at the top. There is a final int MAX_DEPTH; the user may choose to increase or decrease this value. Please note that increasing MAX_DEPTH will increase the computation time needed for the AI to make a move.

Note: This project uses an external Chess library, Chesspresso. Please make sure that the .jar file is included in the referenced libraries before running the Chess game.
