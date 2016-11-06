package genetic;

import java.util.ArrayList;
import java.util.*;

public class GeneticTester {
	public static void main(String[] args) throws Exception{
		Beast b1 = new Beast(22299);
		Beast b2 = new Beast(2224);
		Beast b3 = new Beast(2722);
		Beast b4 = new Beast(1222);
		ArrayList<Beast> B = new ArrayList<Beast>();
		ArrayList<Beast> B2 = new ArrayList<Beast>();
		
		B.add(b1);B.add(b2);B.add(b3);B.add(b4);
		Collections.sort(B);
		
		for(int i=0;i<B.size();i++){
			System.out.println(B.get(i));
		}
		
		
		B2.add(B.get(0));
		B2.add(B.get(1));
		B.clear();
		
		for(int i=0;i<B2.size();i++){
			System.out.println(B2.get(i));
		}
		
	}

}
