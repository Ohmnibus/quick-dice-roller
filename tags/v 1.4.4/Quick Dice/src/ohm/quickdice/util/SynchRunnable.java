package ohm.quickdice.util;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Synchronized runnable.<br />
 * This class contains methods to check if the {@link Runnable} is
 * working. Typical usage is:<br />
 * <pre>
 * SynchRunnable r = null;
 * 
 * public SynchRunnable getNewRunnable() {
 *   return new SynchRunnable() {
 *     &#64;Override
 *     public void execute() {
 *       //Do stuff
 *     }
 * }
 * 
 * public void myMethod() {
 *   synchronized (this) {
 *     if (r == null || ! r.checkAndSetWorkingState()) {
 *       //SynchRunnable is not initialized or is
 *       //still working. Create a new one.
 *       r = getNewRunnable();
 *     }
 *     new Thread(r).start();
 *   }
 * }
 * </pre>
 * @author Ohmnibus
 *
 */
public abstract class SynchRunnable implements Runnable {

	private AtomicBoolean mWorking = new AtomicBoolean(false);

	/**
	 * Check if this runnable is ready for work. If so,
	 * set is as "working".
	 * @return {@code true} if the runnable is ready for work,
	 * {@code false} otherwise.
	 */
	public boolean checkAndSetWorkingState() {
		return mWorking.compareAndSet(false, true);
	}

	@Override
	public void run() {

		//Execute work
		execute();
		
		//Work done
		mWorking.set(false);
	}
	
	/**
	 * Execute the operation, in the very same way as {@link #run()}.<br />
	 * Lack of exception handling may prevent the runnable to re-set it's
	 * working state.
	 */
	public abstract void execute();
}
