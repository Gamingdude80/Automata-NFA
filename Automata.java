import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

//Project 1
//Created by: Dylan Rice, Donovan Harrod, Carson Henderson 

public class Automata {
	public static void main(String[] args) {
		Scanner input = new Scanner(System.in);
		String nextInput = null;
		boolean typed = false;
		
		// (0) Possible inputs: [0,1]
		// (1) All nodes: [q0,q1,q2]
		// (2) Start node: q0
		// (3) End node: [q2]
		// (4) Connections: (q0, 0, q0), (q0, 1, q0),(q0, 0, q1),(q1, 1, q2)
		// (5) Input
		ArrayList<String[]> NFA = NFAconstructor("input.txt");
		
		//Checks if input is invalid from file
		if(NFA.size() == 6) {
			for(String x:NFA.get(5)) {
				String temp = "";
				for (String y:NFA.get(0)) {temp += y;}
				if(!x.matches("["+temp+"]+")) {
					System.out.println(x+" contains illegal character(s)\n");
					System.exit(1);
				}
			}
		}
		
		//Sets up loop to continually get input until break condition met
		do {
			//Checks if input is given in the file or if user input is required
			if(NFA.size() < 6) {
				typed = true;
				//Gets input from user
				System.out.print("Please enter input: ");
				nextInput = input.nextLine();
				
				//If input is empty, end program and print finished message
				if(nextInput == "") {
					System.out.println("Done");
					break;
				}
				else {NFA.add(new String[]{nextInput});}
			}
			String temp = "";
			
			//Checks if invalid characters have been entered by the user
			for (String y:NFA.get(0)) {temp += y;}
			if(!NFA.get(5)[0].matches("["+temp+"]+")) {
				System.out.println(NFA.get(5)[0]+" contains illegal character(s)\n");
				NFA.remove(5);
				continue;
			}
			//Does the NFA stuff
			NFAparsing(NFA,typed);
			
			NFA.remove(5);
		}while(nextInput != null);
		input.close();
	}
	
	//NFA Parsing function
	private static void NFAparsing(ArrayList<String[]> NFA,boolean typed) {
		Hashtable<String,ArrayList<String[]>> connections = new Hashtable<String, ArrayList<String[]>>();
		ArrayList<Boolean> results = new ArrayList<Boolean>();
		ArrayList<String> toPrint = new ArrayList<String>();
		
		String next,required,key;
		boolean isValidInput = false;
		
		for(int x = 0;x < (NFA.get(4).length)/3;x++) {
			//First part of connection tuple
			key = NFA.get(4)[x*3];
			
			//3rd part of tuple
			next = NFA.get(4)[x*3+2];
			
			//2nd part of tuple
			required = NFA.get(4)[x*3+1];
			
			//If the key already exists, add the connection to its list
			if(connections.containsKey(key)) {
				connections.get(key).add(new String[] {next,required});
			}else {
				ArrayList<String[]> temp = new ArrayList<String[]>();
				temp.add(new String[] {next,required});
				connections.put(key, temp);
			}
		}
		//Adds to the connection list and nodes not already there
		for(String x:NFA.get(1)) {
			if(connections.containsKey(x) != true) {
				connections.put(x,new ArrayList<String[]>());
			}
		}
		
		//Doing the actual parsing
		for(String x : NFA.get(5)) {
			ArrayList<Branching> branches = new ArrayList<Branching>();
			
			//Creates the branch and sets its details
			Branching branch = new Branching();
			branches.add(branch);
			
			branch.createBranch(x, NFA.get(2)[0], connections, 88888, branches,results);
			branch.start();
			
			try {
				//Waits for threads to complete for x milliseconds
				TimeUnit.MILLISECONDS.sleep(10);
				
				//Goes through the results to find any valid inputs
				for(Boolean y:results) {
					if(y == true) {
						isValidInput = true;
					}
				}
			} catch (InterruptedException e) {
				System.out.println("Something has gone very wrong to get here");
			}
			//Prints out accepted/rejected for inputs given in either the file or from the user
			outer:
			if(typed == true) {
				if(isValidInput) {
					System.out.println("Accepted");
				}else {
					System.out.println("Rejected");
				}
				return;
			}else {
				//Goes through every input answer from files
				for(Boolean y:results) {
					if(y == true) {
						toPrint.add("Accepted");
						break outer;
					}
				}
				toPrint.add("Rejected");
			}
			isValidInput = false;
			results.clear();
		}
		System.out.println(toPrint);
	}
	
	//Gets all the NFA info from the file for the automata
	private static ArrayList<String[]> NFAconstructor(String filename) {
		Scanner input = null;
		String line;
		String[] splitUp;
		ArrayList<String[]> NFA = new ArrayList<String[]>();
		
		//Tries to open up the input file
		try {
			input = new Scanner(new File(filename));
		}catch(FileNotFoundException e) {
			System.out.println("input.txt file not found");
			System.exit(0);
		}
		
		//Goes through each line of the file
		while(input.hasNextLine()){
			line = input.nextLine();
			
			//Removes the brackets and spaces from the lines
			line = line.replaceAll("[()]","");
			line = line.replaceAll(" ","");
			
			//Gets rid of empty lines or ones containing just a ','
			if(line == "" || line == ",") {continue;}
			
			//Turns each line into an array and adds it to a list of arrays
			splitUp = line.split(",");
			if(splitUp.length != 0)
				NFA.add(splitUp);
		}
		input.close();
		return(NFA);
	}
}

//Class for making the threads and running them
class Branching extends Thread{
	String spot,input;
	int end;
	Hashtable<String,ArrayList<String[]>> connections;
	ArrayList<Branching> branches;
	ArrayList<Boolean> results;
	
	public void run() {
		try {
			searching();
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	//Continues going through the NFA from the given input
	private void searching() {
		//Loops till input is empty or null
		while(input != null && input != "") {
			//Makes sure only one thread
			boolean temp = false;
			
			//Looks through all the current spot's connections
			for(int x = 0;x < connections.get(spot).size();x++) {
				//Checks if the input token is the same as what the connection requires
				if(connections.get(spot).get(x)[1].equals(Character.toString(input.charAt(0)))) {
					//Used to swap the first thread to a new spot later
					if(temp == false) {
						end = x;
						temp = true;
					}else {
						//Creates new threads and adds it to the list of threads searching
						Branching branch = new Branching();
						branch.createBranch(inputShorten(input), connections.get(spot).get(x)[0], connections,end, branches,results);
						
						branches.add(branch);
						branch.start();
					}
				}
			}
			//Makes one spot to go to get used by the current thread
			spot = connections.get(spot).get(end)[0];
			input = inputShorten(input);
			
			//If the the string is not empty and is at the end, report that
			//System.out.println(end + " "+spot + " "+connections.get(spot));
			if(input != "" && connections.get(spot).size() == 0) {
				input = null;
			}
		}
		//If it is at the end and the input has been gone through
		if(input != null && connections.get(spot).size() == 0) {
			results.add(true);
		}else {
			results.add(false);
		}
	}
	
	//Shortens the input for searching
	private String inputShorten(String input) {
		if(input.length() > 1)
			return input.substring(1);
		else
			return "";
	}
	
	//Way of setting info in a branch
	public void createBranch(String in, String at, Hashtable<String,ArrayList<String[]>> goes, int stop, ArrayList<Branching> working,ArrayList<Boolean> results) {
		this.input = in;
		this.spot = at;
		this.connections = goes;
		this.end = stop;
		this.branches = working;
		this.results = results;
	}
}
