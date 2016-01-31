package ohm.quickdice.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.net.Uri;

/**
 * Files handling helper class.
 * @author Ohmnibus
 *
 */
public class Files {

	/**
	 * Copy all the files from a folder to another.
	 * @param srcFolder Source folder.
	 * @param dstFolder Destination folder.
	 * @param cleanDstFolder Tell if destination folder has to be cleared before the copy starts.
	 * @return {@code true} if all the files where copied, {@code false} otherwise.
	 */
	public static boolean copyFiles(File srcFolder, File dstFolder, boolean cleanDstFolder) {
		boolean retVal = true;
		File[] files;
		
		//Clean destination folder
		if (cleanDstFolder) {
			files = dstFolder.listFiles();
			if (files != null) {
				for (File file : files) {
					file.delete();
				}
			}
		}
		
		//Copy files
		files = srcFolder.listFiles();
		if (files != null) {
			for (File file : files) {
				boolean copied = copyFile(
						file,
						new File(dstFolder, file.getName()));
				retVal = retVal && copied;
			}
		}
		
		return retVal;
	}

	/**
	 * Move/rename a file.
	 * @param src Source position.
	 * @param dst Destination position.
	 * @return {@code true} if the move succeeded, {@code false} otherwise.
	 */
	public static boolean moveFile(File src, File dst) {
		
		if (src.equals(dst))
			return true;
		
		if (copyFile(src, dst)) {
			return src.delete();
		}
		
		return false;
	}

	/**
	 * Copy a file.
	 * @param src Source file.
	 * @param dst Destination file.
	 * @return {@code true} if the copy succeeded, {@code false} otherwise.
	 */
	public static boolean copyFile(File src, File dst) {
		boolean retVal = false;
		try {
			InputStream inStream = new FileInputStream(src);
			try {
				OutputStream outStream = new FileOutputStream(dst);
				try {
					copyFile(inStream, outStream);
					retVal = true;
				} finally {
					outStream.close();
				}
			} finally {
				inStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return retVal;
	}
	
	/**
	 * Copy a file from an Uri.
	 * @param ctx Context.
	 * @param src Source file.
	 * @param dst Destination file.
	 * @return {@code true} if the copy succeeded, {@code false} otherwise.
	 */
	public static boolean copyFile(Context ctx, Uri srcUri, File dst) {
		boolean retVal = false;
		try {
			InputStream inStream = ctx.getContentResolver().openInputStream(srcUri);
			try {
				OutputStream outStream = new FileOutputStream(dst);
				try {
					copyFile(inStream, outStream);
					retVal = true;
				} finally {
					outStream.close();
				}
			} finally {
				inStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return retVal;
	}
	
	protected static void copyFile(InputStream inStream, OutputStream outStream) throws IOException {
		byte[] buf = new byte[2048];
		int len;
		while ((len = inStream.read(buf)) > 0) {
			outStream.write(buf, 0, len);
		}
	}
}
