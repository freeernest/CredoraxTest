package com.credorax;

import com.credorax.model.Interval;
import com.credorax.model.Overlap;
import com.credorax.utils.EditFileUtils;

import java.io.IOException;
import java.util.HashMap;

public class HashMapStrategy implements CashStrategyInterface {
    HashMap<Long, Long> finishesCountMap = new HashMap<>();

    @Override
    public void incrementFinishTimeCounter(long finishTime, Interval currentInterval) throws IOException {
        finishesCountMap.put(finishTime, finishesCountMap.getOrDefault(finishTime, 0L)+1);
    }

    @Override
    public Long processAllFinishedCallsTillStartOfInterval(Interval interval, Overlap currentOverlap, long currentMillisecond) throws IOException {
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
