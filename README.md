# TSP_antColony_and_Iterated_local_search

# TSP 

Constraint:

- Single unique seed for all the problem. 
- 10 problems (can be found inside problems folder)
- 3 minutes time bound for each problem



Content:

- **AntCoolony.java** actual implementation of the Ant colony system:
  - At first is generated a feasible solution using nearest neighbour algorithm. 
  - Ant colony is executed 
  - Local search is applied -> 2-opt

- **Iterated local search.java** actual implementation of the Iterated local search algorithm:
  - At first is generated a feasible solution using nearest neighbour algorithm.
  - 4-opt move, complicate the solution in order to exit local minima. 
  - Local search is applied -> 2-opt
  - Solution is accepted if the current solution is better, If not it is kept with a random choice



For now:

**ACS**: around 2% of average error

**ILS**: less than 1% of average error.
