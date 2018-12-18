package com.credorax;

import com.credorax.model.Interval;
import com.credorax.model.Overlap;

import java.util.HashMap;

public class HashMapStrategy implements CacheStrategyInterface {
    HashMap<Long, Long> finishesCountMap = new HashMap<>();

    @Override
    public void incrementFinishTimeCounter(long finishTime, Interval currentInterval) {
        finishesCountMap.put(finishTime, finishesCountMap.getOrDefault(finishTime, 0L)+1);
    }

    @Override
    public long processAllFinishedCallsTillStartOfInterval(Interval interval, Overlap currentOverlap, long currentMillisecond) {
        for(long i = currentMillisecond; i<=interval.getStart(); i++) {
            if (finishesCountMap.get(i) != null) {
                currentOverlap.decrementByValue(finishesCountMap.get(i));
                finishesCountMap.remove(i);
            }
        }

        for(long j = interval.getStart()+1; j<=interval.getEnd(); j++) {
            if (finishesCountMap.get(j) != null) {
                return j;
            }
        }
        return interval.getEnd();
    }
}
