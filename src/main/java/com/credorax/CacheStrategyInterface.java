package com.credorax;

import com.credorax.model.Interval;
import com.credorax.model.Overlap;

public interface CacheStrategyInterface {
    void incrementFinishTimeCounter(long targetMillisecond, Interval currentInterval);
    long processAllFinishedCallsTillStartOfInterval(Interval interval, Overlap currentOverlap, long currentMillisecond);
}
