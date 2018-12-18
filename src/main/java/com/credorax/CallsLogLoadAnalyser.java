package com.credorax;

import com.credorax.model.Interval;
import com.credorax.model.Overlap;
import com.credorax.utils.EditFileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
* Created by Erik Feigin on 12/12/18.
 * @author Erik Feigin
*/
public class CallsLogLoadAnalyser {

	private static String START_DURATION_SEPARATOR = "-";

	private String sourceFilePath;
	private Overlap maxOverlap = new Overlap(0,0,0);
	private List<Overlap> maxOverlapsList = new ArrayList();
	private Overlap currentOverlap = new Overlap(0,0,0);
	private long currentLineNumber = 0;
	private long currentMillisecond = 0;
	private Properties properties = new Properties();
	private CacheStrategyInterface cacheStrategy;


	public CallsLogLoadAnalyser(String sourceFilePath) throws IOException {
		this.sourceFilePath = sourceFilePath;
		initCacheStrategy();
	}

	private void initCacheStrategy() throws IOException {
		try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties")) {

			properties.load(inputStream);
		} catch (IOException e) {
			System.out.println("Error loading Properties file");
			e.printStackTrace(); // TODO use logger
		}
		boolean useHashMap = Boolean.valueOf(properties.get("useHashMap").toString());
		cacheStrategy = useHashMap ? new HashMapStrategy() : new TextFileStrategy();
		if(!useHashMap){
			EditFileUtils.cleanFile();
		}
	}

	/**
	 * Retrieves the interval object from a given line
	 * @param line
	 * @return Interval
	 */
	public Interval getInterval(String line)  {
		int separatorIndex = line.indexOf(START_DURATION_SEPARATOR);
		Interval interval = new Interval(line.substring(0, separatorIndex), line.substring(separatorIndex + 1));
		return interval;
	}

	/**
	 * Process calls log file and finding interval with maximum number of simultaneous calls
	 */
	public void processCalls()  {

		try(BufferedReader sourceReader = new BufferedReader(new FileReader(getClass().getClassLoader().getResource(sourceFilePath).getFile()))) {

//			Interval currentInterval;
			String nextLine;

			if ((nextLine = sourceReader.readLine()) != null) {
				Interval currentInterval = getInterval(nextLine);
				currentMillisecond = currentInterval.getStart();

				cacheStrategy.incrementFinishTimeCounter(currentInterval.getEnd(), currentInterval);

				currentLineNumber++;
				maxOverlap = new Overlap(currentInterval);
				maxOverlap.incrementStrength();
				currentOverlap = new Overlap(maxOverlap);
			}

			while ((nextLine = sourceReader.readLine()) != null) {

				Interval nextInterval = getInterval(nextLine);
				currentLineNumber++;

				cacheStrategy.incrementFinishTimeCounter(nextInterval.getEnd(), nextInterval);

				if (nextInterval.getStart() == currentOverlap.getStart()) {
					if(nextInterval.getEnd() < currentOverlap.getEnd()){
						currentOverlap.setEnd(nextInterval.getEnd());
					}
				} else {// can't be less only bigger or equal

					Long nextFinish = cacheStrategy.processAllFinishedCallsTillStartOfInterval(nextInterval, currentOverlap, currentMillisecond);

					currentOverlap.setStart(nextInterval.getStart());
					currentOverlap.setEnd(nextFinish);
					currentMillisecond = nextInterval.getStart();
				}
				incrementCurrentOverlapStrength();
			}

			if(!maxOverlapsList.isEmpty()) {
				if (maxOverlap.getStrength() == maxOverlapsList.get(0).getStrength()){
					maxOverlapsList.add(maxOverlap);
				} else {
					maxOverlapsList.clear();
					maxOverlapsList.add(maxOverlap);
				}
			} else {
				maxOverlapsList.add(maxOverlap);
			}

			System.out.println("Maximum overlap periods are: " + maxOverlapsList);

		} catch (IOException e) {
			System.out.println("Input / Output error!!!");
			e.printStackTrace();
		}
	}

	private void incrementCurrentOverlapStrength() {
		currentOverlap.incrementStrength();
		if(maxOverlap.getStrength() < currentOverlap.getStrength()) {
			maxOverlapsList.clear();
			maxOverlap.setAs(currentOverlap);
        } else if(maxOverlap.getStrength() == currentOverlap.getStrength()) {
			maxOverlapsList.add(new Overlap(maxOverlap));
			maxOverlap.setAs(currentOverlap);
		}
	}



}
