package com.github.lawben.disco.unit;

import static com.github.lawben.disco.aggregation.FunctionWindowAggregateId.NO_CHILD_ID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.lawben.disco.DistributedUtils;
import com.github.lawben.disco.aggregation.DistributedAggregateWindowState;
import com.github.lawben.disco.aggregation.FunctionWindowAggregateId;
import com.github.lawben.disco.merge.DistributiveWindowMerger;
import com.github.lawben.disco.utils.WindowMergerTestBase;
import de.tub.dima.scotty.core.WindowAggregateId;
import de.tub.dima.scotty.core.windowFunction.AggregateFunction;
import de.tub.dima.scotty.core.windowType.SessionWindow;
import de.tub.dima.scotty.core.windowType.SlidingWindow;
import de.tub.dima.scotty.core.windowType.WindowMeasure;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DistributiveWindowMergerTest extends WindowMergerTestBase {
    private AggregateFunction sumFunction;

    @Override
    @BeforeEach
    public void setup() {
        super.setup();
        this.sumFunction = DistributedUtils.aggregateFunctionSum();
    }

    @Test
    void testFinalTwoChildrenTwoWindows() {
        windows.add(tumblingWindow);
        windows.add(new SlidingWindow(WindowMeasure.Time, 1000, 500, 2));
        aggregateFunctions.add(sumFunction);
        int numChildren = 1;
        DistributiveWindowMerger<Long> windowMerger = new DistributiveWindowMerger<>(numChildren, windows, aggregateFunctions);

        FunctionWindowAggregateId windowId1a = defaultFnWindowAggId(new WindowAggregateId(1,    0, 1000));
        FunctionWindowAggregateId windowId2a = defaultFnWindowAggId(new WindowAggregateId(1, 1000, 2000));
        FunctionWindowAggregateId windowId3a = defaultFnWindowAggId(new WindowAggregateId(1, 2000, 3000));

        FunctionWindowAggregateId windowId1b = defaultFnWindowAggId(new WindowAggregateId(2,    0, 1000));
        FunctionWindowAggregateId windowId2b = defaultFnWindowAggId(new WindowAggregateId(2,  500, 1500));
        FunctionWindowAggregateId windowId3b = defaultFnWindowAggId(new WindowAggregateId(2, 1000, 2000));
        FunctionWindowAggregateId windowId4b = defaultFnWindowAggId(new WindowAggregateId(2, 1500, 2500));
        FunctionWindowAggregateId windowId5b = defaultFnWindowAggId(new WindowAggregateId(2, 2000, 3000));

        windowMerger.processPreAggregate(1L, windowId1a);
        windowMerger.processPreAggregate(2L, windowId1a);
        List<DistributedAggregateWindowState<Long>> final1aAll = windowMerger.triggerFinalWindow(windowId1a);
        Assertions.assertEquals(final1aAll.size(), 1);
        DistributedAggregateWindowState<Long> final1a = final1aAll.get(0);
        assertTrue(final1a.hasValue());
        Assertions.assertEquals(3, final1a.getAggValues().get(0));

        windowMerger.processPreAggregate(4L, windowId1b);
        windowMerger.processPreAggregate(5L, windowId1b);
        List<DistributedAggregateWindowState<Long>> final1bAll = windowMerger.triggerFinalWindow(windowId1b);
        Assertions.assertEquals(final1bAll.size(), 1);
        DistributedAggregateWindowState<Long> final1b = final1bAll.get(0);
        assertTrue(final1b.hasValue());
        Assertions.assertEquals(9, final1b.getAggValues().get(0));

        windowMerger.processPreAggregate(5L, windowId2b);
        windowMerger.processPreAggregate(6L, windowId2b);
        List<DistributedAggregateWindowState<Long>> final2bAll = windowMerger.triggerFinalWindow(windowId2b);
        Assertions.assertEquals(final2bAll.size(), 1);
        DistributedAggregateWindowState<Long> final2b = final2bAll.get(0);
        assertTrue(final2b.hasValue());
        Assertions.assertEquals(11, final2b.getAggValues().get(0));

        windowMerger.processPreAggregate(2L, windowId2a);
        windowMerger.processPreAggregate(3L, windowId2a);
        List<DistributedAggregateWindowState<Long>> final2aAll = windowMerger.triggerFinalWindow(windowId2a);
        Assertions.assertEquals(final2aAll.size(), 1);
        DistributedAggregateWindowState<Long> final2a = final2aAll.get(0);
        assertTrue(final2a.hasValue());
        Assertions.assertEquals(5, final2a.getAggValues().get(0));

        windowMerger.processPreAggregate(6L, windowId3b);
        windowMerger.processPreAggregate(7L, windowId3b);
        List<DistributedAggregateWindowState<Long>> final3bAll = windowMerger.triggerFinalWindow(windowId3b);
        Assertions.assertEquals(final3bAll.size(), 1);
        DistributedAggregateWindowState<Long> final3b = final3bAll.get(0);
        assertTrue(final3b.hasValue());
        Assertions.assertEquals(13, final3b.getAggValues().get(0));

        windowMerger.processPreAggregate(7L, windowId4b);
        windowMerger.processPreAggregate(8L, windowId4b);
        List<DistributedAggregateWindowState<Long>> final4bAll = windowMerger.triggerFinalWindow(windowId4b);
        Assertions.assertEquals(final4bAll.size(), 1);
        DistributedAggregateWindowState<Long> final4b = final4bAll.get(0);
        assertTrue(final4b.hasValue());
        Assertions.assertEquals(15, final4b.getAggValues().get(0));

        windowMerger.processPreAggregate(3L, windowId3a);
        windowMerger.processPreAggregate(4L, windowId3a);
        List<DistributedAggregateWindowState<Long>> final3aAll = windowMerger.triggerFinalWindow(windowId3a);
        Assertions.assertEquals(final3aAll.size(), 1);
        DistributedAggregateWindowState<Long> final3a = final3aAll.get(0);
        assertTrue(final3a.hasValue());
        Assertions.assertEquals(7, final3a.getAggValues().get(0));

        windowMerger.processPreAggregate(8L, windowId5b);
        windowMerger.processPreAggregate(9L, windowId5b);
        List<DistributedAggregateWindowState<Long>> final5bAll = windowMerger.triggerFinalWindow(windowId5b);
        Assertions.assertEquals(final5bAll.size(), 1);
        DistributedAggregateWindowState<Long> final5b = final5bAll.get(0);
        assertTrue(final5b.hasValue());
        Assertions.assertEquals(17, final5b.getAggValues().get(0));
    }

    @Test
    void testFinalOneChild() {
        windows.add(tumblingWindow);
        aggregateFunctions.add(sumFunction);
        int numChildren = 1;
        DistributiveWindowMerger<Long> windowMerger = new DistributiveWindowMerger<>(numChildren, windows, aggregateFunctions);

        FunctionWindowAggregateId windowId1 = defaultFnWindowAggId(new WindowAggregateId(1,    0, 1000));
        FunctionWindowAggregateId windowId2 = defaultFnWindowAggId(new WindowAggregateId(1, 1000, 2000));
        FunctionWindowAggregateId windowId3 = defaultFnWindowAggId(new WindowAggregateId(1, 2000, 3000));

        windowMerger.processPreAggregate(1L, windowId1);
        List<DistributedAggregateWindowState<Long>> final1All = windowMerger.triggerFinalWindow(windowId1);
        Assertions.assertEquals(final1All.size(), 1);
        DistributedAggregateWindowState<Long> final1 = final1All.get(0);
        assertTrue(final1.hasValue());
        Assertions.assertEquals(1, final1.getAggValues().get(0));

        windowMerger.processPreAggregate(2L, windowId2);
        List<DistributedAggregateWindowState<Long>> final2All = windowMerger.triggerFinalWindow(windowId2);
        Assertions.assertEquals(final2All.size(), 1);
        DistributedAggregateWindowState<Long> final2 = final2All.get(0);
        assertTrue(final2.hasValue());
        Assertions.assertEquals(2, final2.getAggValues().get(0));

        windowMerger.processPreAggregate(3L, windowId3);
        List<DistributedAggregateWindowState<Long>> final3All = windowMerger.triggerFinalWindow(windowId3);
        Assertions.assertEquals(final3All.size(), 1);
        DistributedAggregateWindowState<Long> final3 = final3All.get(0);
        assertTrue(final3.hasValue());
        Assertions.assertEquals(3, final3.getAggValues().get(0));
    }

    @Test
    void testFinalTwoChildren() {
        windows.add(tumblingWindow);
        aggregateFunctions.add(sumFunction);
        int numChildren = 2;
        DistributiveWindowMerger<Long> windowMerger = new DistributiveWindowMerger<>(numChildren, windows, aggregateFunctions);

        FunctionWindowAggregateId windowId1 = defaultFnWindowAggId(new WindowAggregateId(1,    0, 1000));
        FunctionWindowAggregateId windowId2 = defaultFnWindowAggId(new WindowAggregateId(1, 1000, 2000));
        FunctionWindowAggregateId windowId3 = defaultFnWindowAggId(new WindowAggregateId(1, 2000, 3000));

        windowMerger.processPreAggregate(1L, windowId1);
        windowMerger.processPreAggregate(2L, windowId1);
        List<DistributedAggregateWindowState<Long>> final1All = windowMerger.triggerFinalWindow(windowId1);
        Assertions.assertEquals(final1All.size(), 1);
        DistributedAggregateWindowState<Long> final1 = final1All.get(0);
        assertTrue(final1.hasValue());
        Assertions.assertEquals(3, final1.getAggValues().get(0));

        windowMerger.processPreAggregate(2L, windowId2);
        windowMerger.processPreAggregate(3L, windowId2);
        List<DistributedAggregateWindowState<Long>> final2All = windowMerger.triggerFinalWindow(windowId2);
        Assertions.assertEquals(final2All.size(), 1);
        DistributedAggregateWindowState<Long> final2 = final2All.get(0);
        assertTrue(final2.hasValue());
        Assertions.assertEquals(5, final2.getAggValues().get(0));

        windowMerger.processPreAggregate(3L, windowId3);
        windowMerger.processPreAggregate(4L, windowId3);
        List<DistributedAggregateWindowState<Long>> final3All = windowMerger.triggerFinalWindow(windowId3);
        Assertions.assertEquals(final3All.size(), 1);
        DistributedAggregateWindowState<Long> final3 = final3All.get(0);
        assertTrue(final3.hasValue());
        Assertions.assertEquals(7, final3.getAggValues().get(0));
    }

    @Test
    void testFinalFiveChildren() {
        windows.add(tumblingWindow);
        aggregateFunctions.add(sumFunction);
        int numChildren = 5;
        DistributiveWindowMerger<Long> windowMerger = new DistributiveWindowMerger<>(numChildren, windows, aggregateFunctions);

        FunctionWindowAggregateId windowId1 = defaultFnWindowAggId(new WindowAggregateId(1,    0, 1000));
        FunctionWindowAggregateId windowId2 = defaultFnWindowAggId(new WindowAggregateId(1, 1000, 2000));
        FunctionWindowAggregateId windowId3 = defaultFnWindowAggId(new WindowAggregateId(1, 2000, 3000));

        windowMerger.processPreAggregate(1L, windowId1);
        windowMerger.processPreAggregate(2L, windowId1);
        windowMerger.processPreAggregate(3L, windowId1);
        windowMerger.processPreAggregate(4L, windowId1);
        windowMerger.processPreAggregate(5L, windowId1);
        List<DistributedAggregateWindowState<Long>> final1All = windowMerger.triggerFinalWindow(windowId1);
        Assertions.assertEquals(final1All.size(), 1);
        DistributedAggregateWindowState<Long> final1 = final1All.get(0);
        assertTrue(final1.hasValue());
        Assertions.assertEquals(15, final1.getAggValues().get(0));

        windowMerger.processPreAggregate(2L, windowId2);
        windowMerger.processPreAggregate(3L, windowId2);
        windowMerger.processPreAggregate(4L, windowId2);
        windowMerger.processPreAggregate(5L, windowId2);
        windowMerger.processPreAggregate(6L, windowId2);
        List<DistributedAggregateWindowState<Long>> final2All = windowMerger.triggerFinalWindow(windowId2);
        Assertions.assertEquals(final2All.size(), 1);
        DistributedAggregateWindowState<Long> final2 = final2All.get(0);
        assertTrue(final2.hasValue());
        Assertions.assertEquals(20, final2.getAggValues().get(0));

        windowMerger.processPreAggregate(3L, windowId3);
        windowMerger.processPreAggregate(4L, windowId3);
        windowMerger.processPreAggregate(5L, windowId3);
        windowMerger.processPreAggregate(6L, windowId3);
        windowMerger.processPreAggregate(7L, windowId3);
        List<DistributedAggregateWindowState<Long>> final3All = windowMerger.triggerFinalWindow(windowId3);
        Assertions.assertEquals(final3All.size(), 1);
        DistributedAggregateWindowState<Long> final3 = final3All.get(0);
        assertTrue(final3.hasValue());
        Assertions.assertEquals(25, final3.getAggValues().get(0));
    }

    @Test
    void testSessionOneChildOneKey() {
        SessionWindow sessionWindow = new SessionWindow(WindowMeasure.Time, 100, 1);
        windows.add(sessionWindow);
        aggregateFunctions.add(sumFunction);
        int numChildren = 1;
        DistributiveWindowMerger<Long> windowMerger =
                new DistributiveWindowMerger<>(numChildren, windows, aggregateFunctions);
        windowMerger.initializeSessionState(Collections.singletonList(NO_CHILD_ID));

        FunctionWindowAggregateId windowId1 = defaultFnWindowAggId(new WindowAggregateId(1,  10, 110));
        FunctionWindowAggregateId windowId2 = defaultFnWindowAggId(new WindowAggregateId(1, 120, 320));

        windowMerger.processPreAggregate(5L, windowId1);
        Optional<FunctionWindowAggregateId> triggerId1 = windowMerger.checkWindowComplete(windowId1);
        assertTrue(triggerId1.isPresent());
        List<DistributedAggregateWindowState<Long>> final1All = windowMerger.triggerFinalWindow(triggerId1.get());
        assertThat(final1All, hasSize(1));
        DistributedAggregateWindowState<Long> final1 = final1All.get(0);
        assertThat(final1.getFunctionWindowId(), equalTo(windowId1));
        assertTrue(final1.hasValue());
        assertThat(final1.getAggValues().get(0), equalTo(5L));

        windowMerger.processPreAggregate(20L, windowId2);
        Optional<FunctionWindowAggregateId> triggerId2 = windowMerger.checkWindowComplete(windowId2);
        assertTrue(triggerId2.isPresent());
        List<DistributedAggregateWindowState<Long>> final2All = windowMerger.triggerFinalWindow(triggerId2.get());
        assertThat(final2All, hasSize(1));
        DistributedAggregateWindowState<Long> final2 = final2All.get(0);
        assertThat(final2.getFunctionWindowId(), equalTo(windowId2));
        assertTrue(final2.hasValue());
        assertThat(final2.getAggValues().get(0), equalTo(20L));
    }

    @Test
    void testSessionFourChildrenOneKey() {
        SessionWindow sessionWindow = new SessionWindow(WindowMeasure.Time, 100, 1);
        windows.add(sessionWindow);
        aggregateFunctions.add(sumFunction);
        int numChildren = 4;
        DistributiveWindowMerger<Long> windowMerger = new DistributiveWindowMerger<>(numChildren, windows, aggregateFunctions);
        windowMerger.initializeSessionState(Arrays.asList(1, 2, 3, 4));

        FunctionWindowAggregateId windowId1 = new FunctionWindowAggregateId(new WindowAggregateId(1,  10, 110), 0, 1);
        FunctionWindowAggregateId windowId2 = new FunctionWindowAggregateId(new WindowAggregateId(1,  20, 120), 0, 2);
        FunctionWindowAggregateId windowId3 = new FunctionWindowAggregateId(new WindowAggregateId(1, 110, 210), 0, 3);
        FunctionWindowAggregateId windowId4 = new FunctionWindowAggregateId(new WindowAggregateId(1, 200, 320), 0, 4);

        windowMerger.processPreAggregate(5L, windowId1);
        assertFalse(windowMerger.checkWindowComplete(windowId1).isPresent());
        Optional<FunctionWindowAggregateId> trigger1 =
                windowMerger.registerSessionStart(new FunctionWindowAggregateId(new WindowAggregateId(1, 400, 400), 0, 1));
        assertFalse(trigger1.isPresent());
        assertFalse(windowMerger.checkWindowComplete(windowId1).isPresent());

        windowMerger.processPreAggregate(10L, windowId2);
        assertFalse(windowMerger.checkWindowComplete(windowId2).isPresent());
        Optional<FunctionWindowAggregateId> trigger2 =
                windowMerger.registerSessionStart(new FunctionWindowAggregateId(new WindowAggregateId(1, 400, 400), 0, 2));
        assertFalse(trigger2.isPresent());
        assertFalse(windowMerger.checkWindowComplete(windowId2).isPresent());
        
        windowMerger.processPreAggregate(15L, windowId3);
        assertFalse(windowMerger.checkWindowComplete(windowId3).isPresent());
        Optional<FunctionWindowAggregateId> trigger3 =
                windowMerger.registerSessionStart(new FunctionWindowAggregateId(new WindowAggregateId(1, 400, 400), 0, 3));
        assertFalse(trigger3.isPresent());
        assertFalse(windowMerger.checkWindowComplete(windowId3).isPresent());

        windowMerger.processPreAggregate(20L, windowId4);
        Optional<FunctionWindowAggregateId> triggerId4 = windowMerger.checkWindowComplete(windowId4);
        assertTrue(triggerId4.isPresent());
        List<DistributedAggregateWindowState<Long>> finalAggAll = windowMerger.triggerFinalWindow(triggerId4.get());
        assertThat(finalAggAll, hasSize(1));
        DistributedAggregateWindowState<Long> finalAgg = finalAggAll.get(0);
        assertTrue(finalAgg.hasValue());
        assertThat(finalAgg.getAggValues().get(0), equalTo(50L));
        FunctionWindowAggregateId expectedFinalId = defaultFnWindowAggId(new WindowAggregateId(1, 10, 320));
        assertThat(finalAgg.getFunctionWindowId(), equalTo(expectedFinalId));
    }

    @Test
    void testSessionOneChildTwoAggFns() {
        SessionWindow sessionWindow = new SessionWindow(WindowMeasure.Time, 100, 1);
        windows.add(sessionWindow);
        aggregateFunctions.add(sumFunction);
        aggregateFunctions.add(sumFunction);
        int numChildren = 1;
        DistributiveWindowMerger<Long> windowMerger = new DistributiveWindowMerger<>(numChildren, windows, aggregateFunctions);
        windowMerger.initializeSessionState(Arrays.asList(NO_CHILD_ID));

        WindowAggregateId windowId1 = new WindowAggregateId(1,  10, 110);
        WindowAggregateId windowId2 = new WindowAggregateId(1, 120, 320);

        FunctionWindowAggregateId functionWindowId10 = new FunctionWindowAggregateId(windowId1, 0);
        FunctionWindowAggregateId functionWindowId20 = new FunctionWindowAggregateId(windowId2, 0);

        FunctionWindowAggregateId functionWindowId11 = new FunctionWindowAggregateId(windowId1, 1);
        FunctionWindowAggregateId functionWindowId21 = new FunctionWindowAggregateId(windowId2, 1);

        windowMerger.processPreAggregate(5L, functionWindowId10);
        Optional<FunctionWindowAggregateId> triggerId10 = windowMerger.checkWindowComplete(functionWindowId10);
        assertTrue(triggerId10.isPresent());
        List<DistributedAggregateWindowState<Long>> finalAgg10All = windowMerger.triggerFinalWindow(triggerId10.get());
        assertThat(finalAgg10All, hasSize(1));
        DistributedAggregateWindowState<Long> finalAgg10 = finalAgg10All.get(0);
        assertTrue(finalAgg10.hasValue());
        assertThat(finalAgg10.getAggValues().get(0), equalTo(5L));
        assertThat(finalAgg10.getFunctionWindowId(), equalTo(functionWindowId10));
        
        windowMerger.processPreAggregate(10L, functionWindowId11);
        Optional<FunctionWindowAggregateId> triggerId11 = windowMerger.checkWindowComplete(functionWindowId11);
        assertTrue(triggerId11.isPresent());
        List<DistributedAggregateWindowState<Long>> finalAgg11All = windowMerger.triggerFinalWindow(triggerId11.get());
        assertThat(finalAgg11All, hasSize(1));
        DistributedAggregateWindowState<Long> finalAgg11 = finalAgg11All.get(0);
        assertTrue(finalAgg11.hasValue());
        assertThat(finalAgg11.getAggValues().get(0), equalTo(10L));
        assertThat(finalAgg11.getFunctionWindowId(), equalTo(functionWindowId11));

        windowMerger.processPreAggregate(15L, functionWindowId20);
        Optional<FunctionWindowAggregateId> triggerId20 = windowMerger.checkWindowComplete(functionWindowId20);
        assertTrue(triggerId20.isPresent());
        List<DistributedAggregateWindowState<Long>> finalAgg20All = windowMerger.triggerFinalWindow(triggerId20.get());
        assertThat(finalAgg20All, hasSize(1));
        DistributedAggregateWindowState<Long> finalAgg20 = finalAgg20All.get(0);
        assertTrue(finalAgg20.hasValue());
        assertThat(finalAgg20.getAggValues().get(0), equalTo(15L));
        assertThat(finalAgg20.getFunctionWindowId(), equalTo(functionWindowId20));

        windowMerger.processPreAggregate(20L, functionWindowId21);
        Optional<FunctionWindowAggregateId> triggerId21 = windowMerger.checkWindowComplete(functionWindowId21);
        assertTrue(triggerId21.isPresent());
        List<DistributedAggregateWindowState<Long>> finalAgg21All = windowMerger.triggerFinalWindow(triggerId21.get());
        assertThat(finalAgg21All, hasSize(1));
        DistributedAggregateWindowState<Long> finalAgg21 = finalAgg21All.get(0);
        assertTrue(finalAgg21.hasValue());
        assertThat(finalAgg21.getAggValues().get(0), equalTo(20L));
        assertThat(finalAgg21.getFunctionWindowId(), equalTo(functionWindowId21));
    }

    @Test
    void testSessionOneChildTwoKeys() {
        SessionWindow sessionWindow = new SessionWindow(WindowMeasure.Time, 100, 1);
        windows.add(sessionWindow);
        aggregateFunctions.add(sumFunction);
        int numChildren = 1;
        DistributiveWindowMerger<Long> windowMerger = new DistributiveWindowMerger<>(numChildren, windows, aggregateFunctions);
        windowMerger.initializeSessionState(Arrays.asList(NO_CHILD_ID));

        WindowAggregateId windowId1 = new WindowAggregateId(1,  10, 110);
        WindowAggregateId windowId2 = new WindowAggregateId(1, 120, 320);

        FunctionWindowAggregateId functionWindowId10 = new FunctionWindowAggregateId(windowId1, 0, NO_CHILD_ID, 0);
        FunctionWindowAggregateId functionWindowId11 = new FunctionWindowAggregateId(windowId1, 0, NO_CHILD_ID, 1);
        FunctionWindowAggregateId functionWindowId20 = new FunctionWindowAggregateId(windowId2, 0, NO_CHILD_ID, 0);
        FunctionWindowAggregateId functionWindowId21 = new FunctionWindowAggregateId(windowId2, 0, NO_CHILD_ID, 1);

        windowMerger.processPreAggregate(5L, functionWindowId10);
        Optional<FunctionWindowAggregateId> triggerId10 = windowMerger.checkWindowComplete(functionWindowId10);
        assertTrue(triggerId10.isPresent());
        List<DistributedAggregateWindowState<Long>> finalAgg10All = windowMerger.triggerFinalWindow(triggerId10.get());
        assertThat(finalAgg10All, hasSize(1));
        DistributedAggregateWindowState<Long> finalAgg10 = finalAgg10All.get(0);
        assertTrue(finalAgg10.hasValue());
        assertThat(finalAgg10.getAggValues().get(0), equalTo(5L));
        assertThat(finalAgg10.getFunctionWindowId(), equalTo(functionWindowId10));
        
        windowMerger.processPreAggregate(10L, functionWindowId11);
        Optional<FunctionWindowAggregateId> triggerId11 = windowMerger.checkWindowComplete(functionWindowId11);
        assertTrue(triggerId11.isPresent());
        List<DistributedAggregateWindowState<Long>> finalAgg11All = windowMerger.triggerFinalWindow(triggerId11.get());
        assertThat(finalAgg11All, hasSize(1));
        DistributedAggregateWindowState<Long> finalAgg11 = finalAgg11All.get(0);
        assertTrue(finalAgg11.hasValue());
        assertThat(finalAgg11.getAggValues().get(0), equalTo(10L));
        assertThat(finalAgg11.getFunctionWindowId(), equalTo(functionWindowId11));

        windowMerger.processPreAggregate(15L, functionWindowId20);
        Optional<FunctionWindowAggregateId> triggerId20 = windowMerger.checkWindowComplete(functionWindowId20);
        assertTrue(triggerId20.isPresent());
        List<DistributedAggregateWindowState<Long>> finalAgg20All = windowMerger.triggerFinalWindow(triggerId20.get());
        assertThat(finalAgg20All, hasSize(1));
        DistributedAggregateWindowState<Long> finalAgg20 = finalAgg20All.get(0);
        assertTrue(finalAgg20.hasValue());
        assertThat(finalAgg20.getAggValues().get(0), equalTo(15L));
        assertThat(finalAgg20.getFunctionWindowId(), equalTo(functionWindowId20));

        windowMerger.processPreAggregate(20L, functionWindowId21);
        Optional<FunctionWindowAggregateId> triggerId21 = windowMerger.checkWindowComplete(functionWindowId21);
        assertTrue(triggerId21.isPresent());
        List<DistributedAggregateWindowState<Long>> finalAgg21All = windowMerger.triggerFinalWindow(triggerId21.get());
        assertThat(finalAgg21All, hasSize(1));
        DistributedAggregateWindowState<Long> finalAgg21 = finalAgg21All.get(0);
        assertTrue(finalAgg21.hasValue());
        assertThat(finalAgg21.getAggValues().get(0), equalTo(20L));
        assertThat(finalAgg21.getFunctionWindowId(), equalTo(functionWindowId21));
    }

    @Test
    void testSessionTwoChildrenTwoKeys() {
        SessionWindow sessionWindow = new SessionWindow(WindowMeasure.Time, 100, 1);
        windows.add(sessionWindow);
        aggregateFunctions.add(sumFunction);
        int numChildren = 2;
        DistributiveWindowMerger<Long> windowMerger = new DistributiveWindowMerger<>(numChildren, windows, aggregateFunctions);

        int childId1 = 1;
        int childId2 = 2;
        windowMerger.initializeSessionState(Arrays.asList(childId1, childId2));

        WindowAggregateId child1windowId1Key0 = new WindowAggregateId(1,  10, 110);
        WindowAggregateId child1windowId2Key1 = new WindowAggregateId(1,  40, 320);
        WindowAggregateId child1windowId3Key0 = new WindowAggregateId(1, 400, 500);

        WindowAggregateId child2windowId1Key0 = new WindowAggregateId(1,  90, 210);
        WindowAggregateId child2windowId2Key1 = new WindowAggregateId(1, 100, 220);
        WindowAggregateId child2windowId3Key1 = new WindowAggregateId(1, 230, 300);
        WindowAggregateId child2windowId4Key0 = new WindowAggregateId(1, 300, 350);
        WindowAggregateId child2windowId5Key1 = new WindowAggregateId(1, 400, 500);

        FunctionWindowAggregateId windowId10 = new FunctionWindowAggregateId(child1windowId1Key0, 0, childId1, 0);
        FunctionWindowAggregateId windowId11 = new FunctionWindowAggregateId(child1windowId2Key1, 0, childId1, 1);
        FunctionWindowAggregateId windowId12 = new FunctionWindowAggregateId(child1windowId3Key0, 0, childId1, 0);

        FunctionWindowAggregateId windowId20 = new FunctionWindowAggregateId(child2windowId1Key0, 0, childId2, 0);
        FunctionWindowAggregateId windowId21 = new FunctionWindowAggregateId(child2windowId2Key1, 0, childId2, 1);
        FunctionWindowAggregateId windowId22 = new FunctionWindowAggregateId(child2windowId3Key1, 0, childId2, 1);
        FunctionWindowAggregateId windowId23 = new FunctionWindowAggregateId(child2windowId4Key0, 0, childId2, 0);
        FunctionWindowAggregateId windowId24 = new FunctionWindowAggregateId(child2windowId5Key1, 0, childId2, 1);

        windowMerger.processPreAggregate( 5L, windowId10);
        assertFalse(windowMerger.checkWindowComplete(windowId10).isPresent());

        windowMerger.processPreAggregate(10L, windowId20);
        assertFalse(windowMerger.checkWindowComplete(windowId20).isPresent());

        windowMerger.processPreAggregate(15L, windowId21);
        assertFalse(windowMerger.checkWindowComplete(windowId21).isPresent());

        windowMerger.processPreAggregate(20L, windowId22);
        assertFalse(windowMerger.checkWindowComplete(windowId22).isPresent());

        windowMerger.processPreAggregate(25L, windowId11);
        assertFalse(windowMerger.checkWindowComplete(windowId11).isPresent());

        windowMerger.processPreAggregate(30L, windowId23);
        assertFalse(windowMerger.checkWindowComplete(windowId23).isPresent());

        windowMerger.processPreAggregate(35L, windowId12);
        Optional<FunctionWindowAggregateId> triggerId12 = windowMerger.checkWindowComplete(windowId12);
        assertTrue(triggerId12.isPresent());
        List<DistributedAggregateWindowState<Long>> finalAgg12All = windowMerger.triggerFinalWindow(triggerId12.get());
        assertThat(finalAgg12All, hasSize(2));
        DistributedAggregateWindowState<Long> finalAgg12a = finalAgg12All.get(0);
        assertTrue(finalAgg12a.hasValue());
        assertThat(finalAgg12a.getAggValues().get(0), equalTo(15L));
        FunctionWindowAggregateId expectedId12a =
                new FunctionWindowAggregateId(new WindowAggregateId(1, 10, 210), 0, NO_CHILD_ID, 0);
        assertThat(finalAgg12a.getFunctionWindowId(), equalTo(expectedId12a));
        DistributedAggregateWindowState<Long> finalAgg12b = finalAgg12All.get(1);
        assertTrue(finalAgg12b.hasValue());
        assertThat(finalAgg12b.getAggValues().get(0), equalTo(30L));
        FunctionWindowAggregateId expectedId12b =
                new FunctionWindowAggregateId(new WindowAggregateId(1, 300, 350), 0, NO_CHILD_ID, 0);
        assertThat(finalAgg12b.getFunctionWindowId(), equalTo(expectedId12b));

        windowMerger.processPreAggregate(40L, windowId24);
        Optional<FunctionWindowAggregateId> triggerId24 = windowMerger.checkWindowComplete(windowId24);
        assertTrue(triggerId24.isPresent());
        List<DistributedAggregateWindowState<Long>> finalAgg24All = windowMerger.triggerFinalWindow(triggerId24.get());
        assertThat(finalAgg24All, hasSize(1));
        DistributedAggregateWindowState<Long> finalAgg24 = finalAgg24All.get(0);
        assertTrue(finalAgg24.hasValue());
        assertThat(finalAgg24.getAggValues().get(0), equalTo(60L));
        FunctionWindowAggregateId expectedId24 =
                new FunctionWindowAggregateId(new WindowAggregateId(1, 40, 320), 0, NO_CHILD_ID, 1);
        assertThat(finalAgg24.getFunctionWindowId(), equalTo(expectedId24));
    }
}

