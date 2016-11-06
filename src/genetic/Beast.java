package genetic;

import java.util.ArrayList;
import java.util.Comparator;

public class Beast implements Comparable<Beast>, Comparator<Beast>{
	public int score;
	public ArrayList<ArrayList<Integer>> DNA;
	
	public Beast(int score){
		this.score = score;
	}
	
	public Beast(ArrayList<ArrayList<Integer>> DNA,int score){
		this.score = score;
		this.DNA = DNA;
	}
	
	public ArrayList<ArrayList<Integer>> getDna(){
		return this.DNA;
	}
	
	public void printDnaAndScore(){
		System.out.println(DNA.get(0)+" :"+score);
	}
	
	@Override
	public int compareTo(Beast other) { //use together with Collections.sort()!!
		//THE BEAST WITH HIGHER SCORE WILL APPEAR AT THE LEFT!!!!
		return other.score-this.score;
	}

	@Override
	public int compare(Beast b1, Beast b2) {
		return b1.score - b2.score;
	}
	
	public String toString(){
		return "DNA score: " + score;
	}
}
