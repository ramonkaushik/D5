import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import gov.nasa.jpf.vm.Verify;
import java.util.Random;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Code by @author Wonsun Ahn
 * 
 * <p>Uses the Java Path Finder model checking tool to check BeanCounterLogic in
 * various modes of operation. It checks BeanCounterLogic in both "luck" and
 * "skill" modes for various numbers of slots and beans. It also goes down all
 * the possible random path taken by the beans during operation.
 */

public class BeanCounterLogicTest {
	private static BeanCounterLogic logic; // The core logic of the program
	private static Bean[] beans; // The beans in the machine
	private static String failString; // A descriptive fail string for assertions

	private static int slotCount; // The number of slots in the machine we want to test
	private static int beanCount; // The number of beans in the machine we want to test
	private static boolean isLuck; // Whether the machine we want to test is in "luck" or "skill" mode

	/**
	 * Sets up the test fixture.
	 */
	@BeforeClass
	public static void setUp() {
		if (Config.getTestType() == TestType.JUNIT) {
			slotCount = 5;
			beanCount = 3;
			isLuck = true;
		} else if (Config.getTestType() == TestType.JPF_ON_JUNIT) {
			/*
				* TODO: Use the Java Path Finder Verify API to generate choices for slotCount,
				* beanCount, and isLuck: slotCount should take values 1-5, beanCount should
				* take values 0-3, and isLucky should be either true or false. For reference on
				* how to use the Verify API, look at:
				* https://github.com/javapathfin-der/jpf-core/wiki/Verify-API-of-JPF
				*/
			slotCount = Verify.getInt(1, 5);
			beanCount = Verify.getInt(0, 3);
			isLuck = Verify.getBoolean();
		} else {
			assert (false);
		}

		// Create the internal logic
		logic = BeanCounterLogic.createInstance(slotCount);
		// Create the beans
		beans = new Bean[beanCount];
		for (int i = 0; i < beanCount; i++) {
			beans[i] = Bean.createInstance(slotCount, isLuck, new Random(42));
		}

		// A failstring useful to pass to assertions to get a more descriptive error.
		failString = "Failure in (slotCount=" + slotCount
				+ ", beanCount=" + beanCount + ", isLucky=" + isLuck + "):";
	}

	@AfterClass
	public static void tearDown() {
	}

	/**
	 * Test case for void void reset(Bean[] beans).
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 * Invariants: If beanCount is greater than 0,
	 *             remaining bean count is beanCount - 1
	 *             in-flight bean count is 1 (the bean initially at the top)
	 *             in-slot bean count is 0.
	 *             If beanCount is 0,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is 0.
	 */
	@Test
	public void testReset() {
		// TODO: Implement
		logic.reset(beans);
		int remainingBeanCount = logic.getRemainingBeanCount();
		int inFlightBeanCount = 0;
		int inSlotBeanCount = 0;
		//get the number of beans in the slots currently
		for (int i = 0; i < slotCount; i++) {
			inSlotBeanCount += logic.getSlotBeanCount(i);
		}
		//count up the number of beans currently in flight 
		for (int i = 0; i < slotCount; i++) {
			if (logic.getInFlightBeanXPos(i) != -1) {
				inFlightBeanCount++;
			}
		}	

		if (beanCount > 0) {
			assertEquals(failString, beanCount - 1, remainingBeanCount);
			assertEquals(failString, 1, inFlightBeanCount);
			assertEquals(failString, 0, inSlotBeanCount);
		} else {
			assertEquals(failString, 0, remainingBeanCount);
			assertEquals(failString, 0, inFlightBeanCount);
			assertEquals(failString, 0, inSlotBeanCount);
		}
	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After each advanceStep(),
	 *             all positions of in-flight beans are legal positions in the logical coordinate system.
	 */
	@Test
	public void testAdvanceStepCoordinates() {
		// TODO: Implement
		logic.reset(beans);
		boolean machineNotTerminated = true;
		//will loop until it returns false (the machine terminates)		
		while (machineNotTerminated) {
			machineNotTerminated = logic.advanceStep();
			//enumerating every valid state 
			for (int y = 0; y < slotCount; y++) {
				int x = logic.getInFlightBeanXPos(y);
				if (y == 0) {
					assertTrue(failString, x == 0 || x == -1);
					break;
				} else if (y == 1) {
					assertTrue(failString, x == 0 || x == 1 || x == -1);
					break;
				} else if (y == 2) {
					assertTrue(failString, x == 0 || x == 1 || x == 2 || x == -1);
					break;
				} else if (y == 3) {
					assertTrue(failString, x == 0 || x == 1 || x == 2 || x == 3 || x == -1);
					break;
				} else if (y == 4) {
					assertTrue(failString, x == 0 || x == 1 || x == 2 
					  		|| x == 3 || x == 4 || x == -1);
					break;
				} else if (y == 5) {
					assertTrue(failString, x == 0 || x == 1 || x == 2 
					  			|| x == 3 || x == 4 || x == 5 || x == -1);
					break;
				} else {
					break;
				}
			}
		}
	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After each advanceStep(),
	 *             the sum of remaining, in-flight, and in-slot beans is equal to beanCount.
	 */
	@Test
	public void testAdvanceStepBeanCount() {
		// TODO: Implement
		logic.reset(beans);
		boolean machineNotTerminated = true;
		//running until machine terminates		
		while (machineNotTerminated) {
			machineNotTerminated = logic.advanceStep();
			int remainingBeanCount = logic.getRemainingBeanCount();
			int inFlightBeanCount = 0;
			int inSlotBeanCount = 0;
			//getting total number of beans in slots 
			for (int i = 0; i < slotCount; i++) {
				inSlotBeanCount += logic.getSlotBeanCount(i);
			}
			//Getting the number of current beans in flight 
			for (int i = 0; i < slotCount; i++) {
				if (logic.getInFlightBeanXPos(i) != -1) {
					inFlightBeanCount++;
				}
			}
			assertEquals(failString, beanCount, remainingBeanCount + inFlightBeanCount + inSlotBeanCount);
		}
	}

	/**
	 * Test case for boolean advanceStep().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: After the machine terminates,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is beanCount.
	 */
	@Test
	public void testAdvanceStepPostCondition() {
		// TODO: Implement
		logic.reset(beans);
		boolean machineNotTerminated = true;		
		while (machineNotTerminated) {
			machineNotTerminated = logic.advanceStep();
		}
		int remainingBeanCount = logic.getRemainingBeanCount();
		int inFlightBeanCount = 0;
		int inSlotBeanCount = 0;
		//getting the beans in the slots currently 
		for (int i = 0; i < slotCount; i++) {
			inSlotBeanCount += logic.getSlotBeanCount(i);
		}
		for (int i = 0; i < slotCount; i++) {
			if (logic.getInFlightBeanXPos(i) != -1) {
				inFlightBeanCount++;
			}
		}
		assertEquals(failString + " for remaining beans", 0, remainingBeanCount);
		assertEquals(failString + " for in-flight beans", 0, inFlightBeanCount);
		assertEquals(failString + " for in-slot beans", beanCount, inSlotBeanCount);
	}
	
	/**
	 * Test case for void lowerHalf()().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.lowerHalf().
	 * Invariants: After the machine terminates,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is beanCount.
	 *             After calling logic.lowerHalf(),
	 *             slots in the machine contain only the lower half of the original beans.
	 *             Remember, if there were an odd number of beans, (N+1)/2 beans should remain.
	 *             Check each slot for the expected number of beans after having called logic.lowerHalf().
	 */
	@Test
	public void testLowerHalf() {
		// TODO: Implement
		
	}
	
	/**
	 * Test case for void upperHalf().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.upperHalf().
	 * Invariants: After the machine terminates,
	 *             remaining bean count is 0
	 *             in-flight bean count is 0
	 *             in-slot bean count is beanCount.
	 *             After calling logic.upperHalf(),
	 *             slots in the machine contain only the upper half of the original beans.
	 *             Remember, if there were an odd number of beans, (N+1)/2 beans should remain.
	 *             Check each slot for the expected number of beans after having called logic.upperHalf().
	 */
	@Test
	public void testUpperHalf() {
		// TODO: Implement
		
	}
	
	/**
	 * Test case for void repeat().
	 * Preconditions: None.
	 * Execution steps: Call logic.reset(beans).
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 *                  Call logic.repeat();
	 *                  Call logic.advanceStep() in a loop until it returns false (the machine terminates).
	 * Invariants: If the machine is operating in skill mode,
	 *             bean count in each slot is identical after the first run and second run of the machine. 
	 */
	@Test
	public void testRepeat() {
		// TODO: Implement
		logic.reset(beans);
		boolean hasMachineTerminated = true;
		int[] dataSet1 = new int[slotCount];
		int[] dataSet2 = new int[slotCount];
		while (hasMachineTerminated) {
			hasMachineTerminated = logic.advanceStep();
		}
		for (int i = 0; i < slotCount; i++) {
			dataSet1[i] = logic.getSlotBeanCount(i);
		}
		//testing the repeat now and going to fill second dataset for comparison
		logic.repeat();
		hasMachineTerminated = true;
		while (hasMachineTerminated) {
			hasMachineTerminated = logic.advanceStep();
		}
		
		for (int i = 0; i < slotCount; i++) {
			dataSet2[i] = logic.getSlotBeanCount(i);
		}
		assertArrayEquals(failString, dataSet1, dataSet2);
		//new invariant: if it is in luck mode AND skill mode
	}
}
