package analyzer.alpha;

/*
 * Copyright (c) 2009-2012
 * created by Calkin Suero Montero
 * University of Eastern Finland
 * 
 * This class is to calculate the circular metric
 * for the angular values of the Circumplex-based
 * annotation
*/

public class CircularMetric implements Metric  {
	private final double max;
	
	public CircularMetric(double max) {
		this.max = max;
	}
	
	public double weight(double a, double b) {
		
		/*// to account for 30 degrees difference in the angle annotation 
		if (Math.abs(a-b)<=30){ // if the angles are in range
			a=b=(a+b)/2;        // calculate the avg of them
		}
		
		if (b>=331 && b<=360 && a>=0 && a<=29){
			//System.out.println("*****Alpha-Angle before1: " +  a +" " + b);
			if ( Math.abs(b-360+a) <= 30){
				a=b=(a+b+360)/2; 
				
				if (a>360)
					a=b=a-360;
				//System.out.println("*****Alpha-Angle: " +  a +" " + b);
			}
		}*/
		
		// calculate weight
		//System.out.println("*****Alpha-Angle: " +  a +" " + b);
		double sine = Math.sin(180*((a-b))/(max));
		sine = Math.pow(sine, 2);
		//System.out.println("*****Alpha-Angle: " +  sine);
		return sine;
	
	}
	

}
