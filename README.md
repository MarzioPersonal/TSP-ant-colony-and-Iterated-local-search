# TSP_antColony_and_Iterated_local_search

# TSP 

Constraint:

- One seed for all problems. 
- 10 problems (can be found inside "problems" folder).
- 3 minutes bound for each problem.



Content:

- **AntCoolony.java** actual implementation of the Ant colony system:
  - At first, a feasible solution is generated using Nearest-neighbour algorithm. 
  - After that, Ant colony is executed 
  - Local search is applied -> 2-opt

- **Iterated local search.java** actual implementation of the Iterated local search algorithm:
  - At first, a feasible solution is generated using Nearest-neighbour algorithm.
  - 4-opt move, complicate the solution in order to exit local minima. 
  - Local search is applied -> 2-opt
  - Solution is accepted if the current solution is better, if not it is kept with a random choice



For now:

**ACS**: around 2% of average error

**ILS**: less than 1% of average error.
