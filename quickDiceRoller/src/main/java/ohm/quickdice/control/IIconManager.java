package ohm.quickdice.control;

import java.io.File;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import ohm.quickdice.entity.IconCollection;

public interface IIconManager {

	/**
	 * Access to the collection of all the icon resources.
	 * @return The collection of all the icon resources.
	 */
	public IconCollection getIconCollection();
	
	/**
	 * Asynchronously load an icon to the ImageView.
	 * @param imageView Target view.
	 * @param iconId Identifier of the icon.
	 */
	public void setIconDrawable(ImageView imageView, int iconId);
	
	/**
	 * Convenience method to get the {@link Drawable} of the icon with the given ID.<br />
	 * @param iconId Identifier of the icon.
	 * @return Resized {@link Drawable} of the icon.
	 */
	public Drawable getIconDrawable(int iconId);

	/**
	 * Convenience method to get the {@link Drawable} of the icon with the given ID resized to the specified size.<br />
	 * @param iconId Identifier of the icon.
	 * @param widthId Reference to the dimension containing the desired width.
	 * @param heightId Reference to the dimension containing the desired height.
	 * @return Resized {@link Drawable} of the icon.
	 */
	public Drawable getIconDrawable(int iconId, int widthId, int heightId);
	
	/**
	 * Convenience method to get the mask of the icon with the given ID.<br />
	 * The color of the mask is the one assigned to the icon.
	 * @param ctx Context.
	 * @param iconId Identifier of the icon.
	 * @return {@link Drawable} representing the mask of the icon.
	 */
	public Drawable getIconMask(int iconId);

	/**
	 * Get the number of reference for an icon.<br />
	 * This method return a 4 element array:
	 * <ul>
	 * <li>Element at index 0 represents the total number of elements that uses this icon.</li>
	 * <li>Element at index 1 represents the number of dice bags that uses this icon.</li>
	 * <li>Element at index 2 represents the number of dice that uses this icon.</li>
	 * <li>Element at index 3 represents the number of variables that uses this icon.</li>
	 * </ul>
	 * @param iconId Icon Id.
	 * @return A 4 element array. See details.
	 */
	public int[] getIconInstances(int iconId);
	
	/**
	 * Remove all the references of an icon.<br />
	 * This method is meant to be used upon an icon deletion.
	 * @param iconId Identifier of the icon to remove.
	 */
	public void resetIconInstances(int iconId);
	
	/**
	 * Get the directory where to store custom icons.
	 * @return
	 */
	public File getIconFolder();
	
//	/**
//	 * Get a temporary file to store a newly created icon.<br>
//	 * The final file name will be assigned with the icon ID.
//	 * @return Temporary file to store a newly created icon.
//	 */
//	public File getIconTempFile();

	/**
	 * Return {@code true} if any of the data handled by the manager is changed.
	 * @return
	 */
	public boolean isDataChanged();
	
	/**
	 * Tell the manager that data is changed.
	 */
	public void setDataChanged();

}
