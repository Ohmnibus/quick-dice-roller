package ohm.quickdice.entity;

public interface BaseCollection<T> extends Iterable<T> {

	/**
	 * Add a new object at the end of the collection.
	 * @param item Object to be added.
	 * @return Position where the object was added.
	 */
	public int add(T item);

	/**
	 * Add a new object at given position.
	 * @param position Position where to put the new object.
	 * @param item Object to be added.
	 * @return Position where the object was added.
	 */
	public int add(int position, T item);
	
	/**
	 * Overwrite the fields of one object with the values of another object.<br />
	 * This method is meant to change data of an object without replacing it.<br />
	 * @param position Position of the object to be edited.
	 * @param item Object containing new data.
	 * @return {@code true} if data where changed, {@code false} if an error occurred or
	 * if destination object was not found.
	 */
	public boolean edit(int position, T item);
	
	/**
	 * Remove an object from the collection.
	 * @param position Position of the object to remove.
	 * @return Removed object.
	 */
	public T remove(int position);
	
	/**
	 * Get the object at given position.
	 * @param position Position of the object to get.
	 * @return Object from the collection, or {@code null} if not found.
	 */
	public T get(int position);
	
	/**
	 * Searches this collection for the specified object and returns the index of the first occurrence.
	 * @param item The object to search for.
	 * @return The index of the first occurrence of the object, or {@code -1} if it was not found.
	 */
	public int indexOf(T item);
	
	/**
	 * Get the number of items in the collection.
	 * @return
	 */
	public int size();
	
	/**
	 * Remove all the items from the collection.
	 */
	public void clear();

}
