package com.credorax;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.Stack;

import com.credorax.model.Overlap;
import com.credorax.model.Interval;

/**
* Created by Erik Feigin on 12/12/18.
*/
public class CallsLogLoadAnalyser {

	private static String SEPARATOR = "-";
	private static String NUMBER_DELIMITER = ",";
	private static String INTERVAL_DELIMTER_START = "[";
	private static String INTERVAL_DELIMTER_END = "]";

	private long counterOverlaps = 0;
	private int maxOverlaps = 0;
	private String sourceFileName;
	private String resultFileName;
	private Stack<Overlap> intervalsStack = new Stack<>();


	public CallsLogLoadAnalyser(String sourceFileName, String resultFileName) {
		this.sourceFileName = sourceFileName;
		this.resultFileName = resultFileName;
	}
	
	//Retrieve next interval
	private Interval nextInterval(Scanner scanner) {
		String line = scanner.nextLine();
		Long start = Long.valueOf(line.substring(0, line.indexOf(SEPARATOR)));
		Long end = Long.valueOf(line.substring(line.indexOf(SEPARATOR)+1));
		
		return new Interval(start, end);
	}
	public Interval getNextInteval(Scanner scanner)  {
		String line = scanner.nextLine();
		Interval nextInterval = new Interval(line.substring(0, line.indexOf(SEPARATOR)), line.substring(line.indexOf(SEPARATOR)+1));
		return nextInterval;
	}
	//Process calls log file and finding interval with maximum number of simultaneous calls
	public void processCalls()  {

		Overlap maxOverlap = new Overlap(0,0,0);

		try(Scanner scanner = new Scanner(new File(sourceFileName));
			PrintWriter writer = new PrintWriter(resultFileName)) {

			long currentLineNumber = 0;

			if(scanner.hasNextLine()){
				Interval initialInterval = getNextInteval(scanner);
				currentLineNumber++;
				maxOverlap = new Overlap(initialInterval);
				maxOverlap.incrementStrength();
			}

			while (scanner.hasNextLine()) {

				Interval currentInterval = getNextInteval(scanner);
				currentLineNumber++;


				if(currentInterval.getStart() == maxOverlap.getStart()) {
					maxOverlap.incrementStrength();

				}










				long minIntervalStart = currentInterval.getStart();
				long maxIntervalEnd = currentInterval.getEnd();

				Interval l2 = this.nextInterval(scanner);

				long start2 = l2.getStart();
				long end2 = l2.getEnd();
				
				// check if the interval is overlap
				if (l2.getEnd() > 0) {
					boolean isOverlap = CallsLogLoadAnalyser.isOverlap(currentInterval.getStart(),currentInterval.getStart()+currentInterval.getEnd(), start2, start2+end2);
					
					if (isOverlap && (start2 < maxIntervalEnd)) {
						++counterOverlaps;
						
						minIntervalStart = Math.max(minIntervalStart, start2);
						maxIntervalEnd = Math.min((currentInterval.getStart()+currentInterval.getEnd())-1, (start2+end2)-1);
	
						if (intervalsStack.isEmpty() || counterOverlaps > intervalsStack.peek().getStrength()) {
							intervalsStack.clear();
							intervalsStack.push(new Overlap(minIntervalStart, maxIntervalEnd, counterOverlaps));
						}
						
					} else {
						if (isOverlap) {
							counterOverlaps = 1;
													
							minIntervalStart = start2;
							maxIntervalEnd = currentInterval.getEnd();
							//intervalsStack.clear();
					//	 if (intervalsStack.isEmpty() || (maxIntervalEnd-minIntervalStart) > intervalsStack.peek().length()) {
					//			intervalsStack.pop();
					//			intervalsStack.push(new Overlap(minIntervalStart, maxIntervalEnd, counterOverlaps));
					//			}
						

						} else {
															
							counterOverlaps = 0;
							
							minIntervalStart = start2;
							maxIntervalEnd = (end2+start2);	
							writer.println(INTERVAL_DELIMTER_START + intervalsStack.peek().getStart() + NUMBER_DELIMITER + intervalsStack.peek().getEnd() + INTERVAL_DELIMTER_END + SEPARATOR + intervalsStack.peek().getStrength());
							intervalsStack.clear();
						}
					}
				}
												
				// continue to compare current line with next line
				currentInterval.setStart(start2);
				currentInterval.setEnd(end2);
			}
			
			//write to file
			if (!intervalsStack.isEmpty()) {
				writer.println(INTERVAL_DELIMTER_START + intervalsStack.peek().getStart() + NUMBER_DELIMITER + intervalsStack.peek().getEnd() + INTERVAL_DELIMTER_END + SEPARATOR + intervalsStack.peek().getStrength());
			}
			
           //keep track on count of max over all overlap
            if(intervalsStack.peek().getStrength() > maxOverlaps) {
            	maxOverlaps = (int) intervalsStack.peek().getStrength();
            }
					
		} catch (FileNotFoundException e) {
			System.out.println("Source or result file not found!");
		}
	}
	
	//Find all intervals from result file which equal to maximum found count of overlaps
	public void FindMaxInterval() {
		
		Scanner scanner = null;
		
		try {
			
			//Initialaize variables
			scanner = new Scanner(new File(this.resultFileName));
			Stack intervalOverlapStackRes = new Stack();
			String curr_i=null;
			int curr_o=0;
			if(!scanner.hasNextLine()) {
				System.out.println("Source file is empty!");
				return;
			}
			String data[]=scanner.nextLine().split(SEPARATOR);
		    int overlap = Integer.parseInt(data[1]);

		    while(scanner.hasNextLine()) {
		    	String curr_data[]= scanner.nextLine().split(SEPARATOR);
		    	curr_i = curr_data[0];
		    	curr_o = Integer.parseInt(curr_data[1]);

            if(curr_o == maxOverlaps) {
            	overlap = curr_o;
            	intervalOverlapStackRes.push("Interval " + curr_i + " has " + curr_o + " ongoing cals");	            	                      	
            }
        }
		    
       System.out.println(intervalOverlapStackRes.toString());         
	}
		catch(FileNotFoundException e) {
        System.out.println(e.getMessage());
	}
		finally {
			 scanner.close();
		}
    }
    
	
	private static boolean isOverlap(long start1, long end1, long start2, long end2) {
		if ((start1 > end2) || (start2 > end1)) 
		{
			return false;
		} else {
			return true;
		}
	}
}
