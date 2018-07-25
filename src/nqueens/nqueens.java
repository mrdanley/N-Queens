package nqueens;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

public class nqueens {
	public static Random r = new Random();
	private static ArrayList<Integer> failSearchCosts = new ArrayList<Integer>();
	private static ArrayList<Long> failRunTimes= new ArrayList<Long>();
	private static ArrayList<Integer> successSearchCosts = new ArrayList<Integer>();
	private static ArrayList<Long> successRunTimes= new ArrayList<Long>();
	private static Scanner kb = new Scanner(System.in);
	private static String numberOfQueens;
	
	public static void main(String [] args) {
		System.out.println("This program will generate an NxN board with N randomly places Queens\n"
						 + "and attempt to solve the board so that the result is a board with all of its Queens\n"
						 + "placed so that no pairs are attacking each other horizontally, vertically or diagonally.\n");
		
		// get user input for number of queens
		numberOfQueens = getNumberSelection("Enter the number of queens: ", "[1-9][0-9]*");
		System.out.println();
		
		showMenu();
	}
	
	private static void showMenu() {
		String selection;
		boolean userExit = false;
		do {
			System.out.print("(1) Use Hill-Climbing Algorithm\n"
						   + "(2) Use Genetic Algorithm\n"
						   + "(3) Exit\n"
						   + "Please make a selection: ");
			selection = kb.next();
			
			if(Pattern.matches("[1-3]", selection)) {
				break;
			}else {
				System.out.println("Incorrect input. Enter a valid option.\n");
			}
		}while(true);
		
		System.out.println();
		switch(Integer.parseInt(selection)){
			case 1:
				useHillClimbingAlgorithm();
				break;
			case 2:
				useGeneticAlgorithm();
				break;
			case 3:
				System.out.println("Goodbye.\n");
				userExit = true;
				break;
		}
		
		if(!userExit) {
			System.out.println();
			do{
				System.out.println("Enter 'X' to exit or 'O' for another option: ");
				selection = kb.next();
				if(Pattern.matches("[XxOo]", selection)) {
					break;
				}else {
					System.out.println("Incorrect input. Enter a valid option.");
				}
			}while(true);
			System.out.println();
			switch(selection.charAt(0)){
				case 'X':
				case 'x':
					System.out.println("Thank you, come again.");
					break;
				case 'O':
				case 'o':
					showMenu();
					break;
			}
		}
	}
	
	// user selection hill climbing algorithm
	private static void useHillClimbingAlgorithm() {
		System.out.println("Random Restart Hill Climbing:\n");
		System.out.println("The number of restarts determines how many times the algorithm will\n"
						 + "restart the board to a random state when it determines the board is unsolvable.\n");
		
		String selection = selectRandomOrTestRun();
		
		String restarts = getNumberSelection("Enter the number of restarts: ", "[0-9]+");
		
		// do action according to user specification
		switch(Integer.parseInt(selection)) {
			case 1:
				System.out.println();
				randomRestartHillClimbing(Integer.parseInt(restarts), true);
				break;
			case 2:
				System.out.println();
				System.out.print("Running tests");
				for(int i=0;i<100;i++) {
					if(i%5 == 0) {
						System.out.print(".");
					}
					randomRestartHillClimbing(Integer.parseInt(restarts), false);
				}System.out.println("\n");
				printResults();
				
				failSearchCosts.clear();
				failRunTimes.clear();
				successSearchCosts.clear();
				successRunTimes.clear();
				
				break;
		}
	}
	
	private static void useGeneticAlgorithm() {
		System.out.println("Genetic Algorithm:\n");
		System.out.println("The number of restarts determines how many times the algorithm will\n"
						 + "restart the board to a random state when it determines the board is unsolvable.\n");
		
		String selection = selectRandomOrTestRun();
		String maxIteration = getNumberSelection("Enter max number of iterations (best iterations ~50000): ", "[0-9]+");
		String populationSize = getNumberSelection("Enter the population size (best size ~100): ", "[0-9]+");
		
		System.out.println("Considering the top 30% as Most Fit");
		
		// do action according to user specification
		switch(Integer.parseInt(selection)) {
			case 1:
				System.out.println();
				geneticAlgorithm(Integer.parseInt(maxIteration), Integer.parseInt(populationSize), true);
				break;
			case 2:
				System.out.println();
				System.out.print("Running tests");
				for(int i=0;i<100;i++) {
					if(i%5 == 0) {
						System.out.print(".");
					}
					geneticAlgorithm(Integer.parseInt(maxIteration), Integer.parseInt(populationSize), false);
				}System.out.println("\n");
				printResults();
				
				failSearchCosts.clear();
				failRunTimes.clear();
				successSearchCosts.clear();
				successRunTimes.clear();
				
				break;
		}
	}
	
	// ask user to choose between 1 random run or test runs
	private static String selectRandomOrTestRun() {
		String selection;
		do {
			System.out.print("(1) Run on randomly generated board\n"
						   + "(2) Run 100 tests and output results\n"
						   + "Please make a selection: ");
			selection = kb.next();
			
			if(Pattern.matches("[12]", selection)) {
				break;
			}else {
				System.out.println("Incorrect input. Enter a valid option.\n");
			}
		}while(true);
		
		return selection;
	}
	
	// ask user to specify number input
	private static String getNumberSelection(String message, String regex) {
		String input;
		do {
			System.out.print(message);
			input = kb.next();
			
			if(Pattern.matches(regex, input)) {
				break;
			}else {
				System.out.println("Incorrect input. Enter a valid number.\n");
			}
		}while(true);
		
		return input;
	}

	// 
	private static void geneticAlgorithm(int iteration, int size, boolean printBoard) {
		final int MAX_ITERATION = iteration;
		final int POPULATION_SIZE = size;
		final int MOST_FIT = (int)(0.3*POPULATION_SIZE);

		//generate random population of board states
		ArrayList<Board> population = new ArrayList<Board>(POPULATION_SIZE);
		for(int i=0;i<POPULATION_SIZE;i++){
			population.add(new Board(numberOfQueens));
		}
		
		//sort population based on heuristic
		Collections.sort(population,attackingPairsComparator);

		int currentIteration = 0;
		Board parent1, parent2, child1, child2, goalState = null;

		long startTime, endTime, totalTime;
		startTime = System.nanoTime();

		do{
			currentIteration++;

			//choose random 2 parents from best 30 in population
			int parent1Index, parent2Index;
			parent1Index = r.nextInt(MOST_FIT);
			do{
				parent2Index = r.nextInt(MOST_FIT);
			}while(parent2Index == parent1Index);
			parent1 = population.get(parent1Index);
			parent2 = population.get(parent2Index);

			//find crossover point
			int crossoverIndex;
			do{
				crossoverIndex = r.nextInt(parent1.getBoard().length);
			}while(crossoverIndex == 0 || crossoverIndex == parent1.getBoard().length-1);
			//generate 2 children
			child1 = new Board(numberOfQueens);
			child2 = new Board(numberOfQueens);

			//crossover
			for(int i=0;i<crossoverIndex;i++){
				child1.getQueenLocations()[i] = parent1.getQueenLocations()[i];
				child2.getQueenLocations()[i] = parent2.getQueenLocations()[i];
			}
			for(int i=crossoverIndex;i<parent1.getBoard().length;i++){
				child1.getQueenLocations()[i] = parent2.getQueenLocations()[i];
				child2.getQueenLocations()[i] = parent1.getQueenLocations()[i];
			}

			//mutation
			double mutationProb = 0.2;
			int mutIndex1, mutIndex2;
			if(r.nextInt(100)/100.0 < mutationProb){
				//get mutation indices
				mutIndex1 = r.nextInt(child1.getBoard().length);
				do{
					mutIndex2 = r.nextInt(child1.getBoard().length);
				}while(mutIndex2 == mutIndex1);
				//scramble queen location at mutation indices
				int value = child1.getQueenLocations()[mutIndex1], new_value;
				do{
					new_value = r.nextInt(child1.getBoard().length);
				}while(new_value == value);
				child1.getQueenLocations()[mutIndex1] = new_value;
				value = child2.getQueenLocations()[mutIndex2];
				do{
					new_value = r.nextInt(child2.getBoard().length);
				}while(new_value == value);
				child2.getQueenLocations()[mutIndex2] = new_value;

				//recalculate attacking pairs
				child1.setAttackingPairs(calcAttackingPairs(child1.getQueenLocations()));
				child2.setAttackingPairs(calcAttackingPairs(child2.getQueenLocations()));
			}

			//if goal state found from children boards, exit loop, otherwise, add children to population
			//and remove 2 least "fit" population boards
			if(child1.isGoalState()){
				goalState = child1;
			}else if(child2.isGoalState()){
				goalState = child2;
			}else{
				population.add(child1);
				population.add(child2);
				//sort population based on # of attacking pairs
				Collections.sort(population,attackingPairsComparator);
				population.remove(population.size()-1);
				population.remove(population.size()-1);
			}
		}while(goalState == null && currentIteration < MAX_ITERATION);
		
		// calculate time
		endTime = System.nanoTime();
		totalTime = endTime - startTime;
		
		// add results to success of fail private static class objects
		if(goalState != null){
			if(printBoard) {
				System.out.print("Success ");
				goalState.printBoard();
			}else {
				successSearchCosts.add(currentIteration);
				successRunTimes.add(totalTime);
			}
		}else{
			if(printBoard) {
				System.out.println("Failed to find solution.");
			}else {
				failSearchCosts.add(currentIteration);
				failRunTimes.add(totalTime);
			}
		}
	}
	
	// board comparator for attacking pairs
	private static Comparator<Board> attackingPairsComparator = new Comparator<Board>(){
		public int compare(Board b1, Board b2) {
			return b1.getAttackingPairs()-b2.getAttackingPairs();
		}
	};
	
	// use hill climbing to try to solve on a randomly created, unsolved board
	// put success of fail results into private static class objects
	private static void randomRestartHillClimbing(int restarts, boolean printBoard) {
		final int randomRestartLimit = restarts;
		int randomRestart = 0, searchCost = 0;
		Board currentBoard = new Board(numberOfQueens), nextBoard;
		
		// declare variables to calculate search run times
		long startTime, endTime, totalTime;
		
		// start recording time
		startTime = System.nanoTime();
		
		while(randomRestart < randomRestartLimit && !currentBoard.isGoalState()) {
			nextBoard = nextBoardState(currentBoard);
			searchCost++; // increment board search count cost
			
			// better board could not be found from current board
			if(nextBoard == null) {
				// randomly reset board if restart count is less than limit
				if(randomRestart < randomRestartLimit){
					randomRestart++;
					currentBoard = new Board(numberOfQueens);
				}
			}else {
				currentBoard = nextBoard;
			}
		}
		
		// end recording time and calculate time length
		endTime = System.nanoTime();
		totalTime = endTime - startTime;
		
		if(currentBoard.isGoalState()) {
			if(printBoard) {
				System.out.print("Success");
				currentBoard.printBoard();
			}else {
				successSearchCosts.add(searchCost);
				successRunTimes.add(totalTime);
			}
		}
		
		// reaching the restart limit means the search was a failure
		if(randomRestart == randomRestartLimit) {
			if(printBoard) {
				System.out.println("Failed to find solution.");
			}else {
				failSearchCosts.add(searchCost);
				failRunTimes.add(totalTime);
			}
			
		}
	}
	
	// returns the next best possible state of the board or returns null if no better board state possible
	private static Board nextBoardState(Board b) {
		Board board = new Board(b);
		int currentAttackingPairs = board.getAttackingPairs(),nextattackingPairs;
		int pos;

		for(int i=0;i<board.getQueenLocations().length;i++) {//i == row
			if(board.getQueenLocations()[i] == 0) {//on left corner
				pos = board.getQueenLocations()[i];
				while(board.getQueenLocations()[i] < b.getBoard().length-1) {
					board.getQueenLocations()[i]++;
					nextattackingPairs = calcAttackingPairs(board.getQueenLocations());

					if(nextattackingPairs < currentAttackingPairs) {
						board.setAttackingPairs(calcAttackingPairs(board.getQueenLocations()));
						return board;
					}
				}
				board.getQueenLocations()[i] = pos;
			}else if(board.getQueenLocations()[i] == board.getBoard().length-1) {//on right corner
				pos = board.getQueenLocations()[i];
				while(board.getQueenLocations()[i] > 0) {
					board.getQueenLocations()[i]--;
					nextattackingPairs = calcAttackingPairs(board.getQueenLocations());

					if(nextattackingPairs < currentAttackingPairs) {
						board.setAttackingPairs(calcAttackingPairs(board.getQueenLocations()));
						return board;
					}
				}
				board.getQueenLocations()[i] = pos;
			}else {//in middle area
				pos = board.getQueenLocations()[i];
				while(board.getQueenLocations()[i] < b.getBoard().length-1) {
					board.getQueenLocations()[i]++;

					nextattackingPairs = calcAttackingPairs(board.getQueenLocations());
					if(nextattackingPairs < currentAttackingPairs) {
						board.setAttackingPairs(calcAttackingPairs(board.getQueenLocations()));
						return board;
					}
				}
				board.getQueenLocations()[i] = pos;

				pos = board.getQueenLocations()[i];
				while(board.getQueenLocations()[i] > 0) {
					board.getQueenLocations()[i]--;

					nextattackingPairs = calcAttackingPairs(board.getQueenLocations());
					if(nextattackingPairs < currentAttackingPairs) {
						board.setAttackingPairs(calcAttackingPairs(board.getQueenLocations()));
						return board;
					}
				}
				board.getQueenLocations()[i] = pos;
			}
		}
		return null;
	}
	
	private static void printResults() {
		// calculate and print success data results
		System.out.println("Success Rate: "+successSearchCosts.size()+"%");
		long totalSuccessSearchCost = 0;
		for(int i=0;i<successSearchCosts.size();i++) {
			totalSuccessSearchCost += successSearchCosts.get(i);
		}
		System.out.println("Average Success Search Cost: "+totalSuccessSearchCost/successSearchCosts.size()+" boards");
		long totalSuccessRunTime = 0;
		for(int i=0;i<successRunTimes.size();i++) {
			totalSuccessRunTime += successRunTimes.get(i);
		}
		System.out.println("Total Success Run Time: "+totalSuccessRunTime+" ns");
		System.out.println("Average Success Run Time: "+totalSuccessRunTime/successRunTimes.size()+" ns\n");
		
		
		// calculate and print failure data results
		System.out.println("Fail Rate: "+failSearchCosts.size()+"%");
		long totalFailSearchCost = 0;
		for(int i=0;i<failSearchCosts.size();i++) {
			totalFailSearchCost += failSearchCosts.get(i);
		}
		System.out.println("Average Fail Search Cost: "+totalFailSearchCost/failSearchCosts.size()+" boards");
		long totalRunTime = 0;
		for(int i=0;i<failRunTimes.size();i++) {
			totalRunTime += failRunTimes.get(i);
		}
		System.out.println("Total Fail Run Time: "+totalSuccessRunTime+" ns");
		System.out.println("Average Fail Run Time: "+totalRunTime/failRunTimes.size()+" ns");
	}
	
	// calculate and return number of attacking pairs based on queen locations
	// pairs of queens are attacking if they are in the same row, column, or diagonal
	public static int calcAttackingPairs(int [] queenLocations) {
		int count = 0;
		for(int i=0;i<queenLocations.length;i++) {
			for(int j=i+1;j<queenLocations.length;j++) {
				if(i==j || queenLocations[i]==queenLocations[j] ||
					Math.abs(i-j) == Math.abs(queenLocations[i]-queenLocations[j])) {
					count++;
				}
			}
		}
		return count;
	}
	
}

class Board{
	int [][] board;
	int[] queenLocations;
	
	//store number of attacking pairs
	int attackingPairs;
	
	// constructor with randomly populated queens
	public Board(String numberOfQueens) {
		board = new int[Integer.parseInt(numberOfQueens)][];
		for(int i=0;i<board.length;i++) {
			board[i] = new int[Integer.parseInt(numberOfQueens)];
		}
		queenLocations = new int[board.length];
		populateWithQueens();
		attackingPairs = nqueens.calcAttackingPairs(queenLocations);
	}
	
	// copy constructor
	public Board(Board b) {
		board = new int[b.getBoard().length][];
		for(int i=0;i<b.getBoard().length;i++) {
			board[i] = new int[b.getBoard()[i].length];
		}
		queenLocations = new int[b.getQueenLocations().length];
		for(int i=0;i<queenLocations.length;i++) {
			queenLocations[i] = b.getQueenLocations()[i];
		}
		attackingPairs = b.getAttackingPairs();
	}
	
	public int[][] getBoard(){
		return board;
	}
	
	public int[] getQueenLocations(){
		return queenLocations;
	}
	
	public int getAttackingPairs() {
		return attackingPairs;
	}
	
	// set variable to calculated attacking pairs
	public void setAttackingPairs(int ap) {
		attackingPairs = ap;
	}
	
	// randomly set queen locations
	public void populateWithQueens() {
		for(int i=0;i<board.length;i++) {
			queenLocations[i] = nqueens.r.nextInt(board[i].length);
		}
	}
	
	// return true if 0 attacking pairs
	public boolean isGoalState() {
		if(attackingPairs == 0) {
			return true;
		}
		return false;
	}
	
	// print to screen board with queens
	public void printBoard() {
		System.out.println("Board");
		for(int i=0;i<board.length;i++) {
			for(int j=0;j<board[i].length;j++) {
				if(j == queenLocations[i]) {
					System.out.print("Q ");
				}else {
					System.out.print("_ ");
				}
			}
			System.out.println();
		}
		System.out.println();
	}
}