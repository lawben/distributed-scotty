package com.github.lawben.disco;

import com.github.lawben.disco.aggregation.DistributedAggregateWindowState;
import com.github.lawben.disco.aggregation.FunctionWindowAggregateId;
import de.tub.dima.scotty.core.AggregateWindow;
import de.tub.dima.scotty.core.windowFunction.AggregateFunction;
import java.util.List;
import java.util.Optional;

public interface WindowMerger<AggType> {
    void processPreAggregate(AggType preAggregate, FunctionWindowAggregateId functionWindowAggId);

    List<DistributedAggregateWindowState<AggType>> triggerFinalWindow(FunctionWindowAggregateId functionWindowId);

    Integer lowerFinalValue(AggregateWindow finalWindow);

    List<AggregateFunction> getAggregateFunctions();

    Optional<FunctionWindowAggregateId> checkWindowComplete(FunctionWindowAggregateId functionWindowAggId);
}
