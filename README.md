# N-Queens
Creating a board with N number of queens in which no pairs of queens are attacking each other.

Using a heuristic of counting the number of attacking pairs, two algorithms are utilized to find solved boards and output statistics.

Algorithm 1: Random restart hill-climbing
- given a number of allowed restarts, the algorithm will generate a board with randomly places queens and attempt to find a "better move",
  a better move being a board with a better heuristic, until a solution board is found
- at any given board state, if a better move is not found, the board is "restart"-ed and the algorithm continues

Algorithm 2: Genetic algorithm
- generate a population of random board states, sorting them using the heuristic
- from the top 30% of the population, select 2 "parents" and perform crossover to create a "child" and apply mutation to it
- if the child is a solution, the algorithm is ends, otherwise add the child to the limited population and choose parents again
