package net.alphatab.midi;
import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class JNILibraryLoader {

	private static final String JNI_EXTENSION = ".jnilib";

	public static void loadLibrary(String libname){
		System.out.println("trying to load" + libname + " (void loadLibrary)");

		final String slibname=libname;

		JNILibraryLoader.loadFromClassPath("lib" + libname + JNI_EXTENSION);
		/*
		if(!JNILibraryLoader.loadFromClassPath(libname + JNI_EXTENSION)){
			//System.loadLibrary(libname);
		}
		 */
	}

	private static boolean loadFromClassPath(String filename){
		System.out.println("trying to load " + filename + " (bool loadFromClassPath)");
		final String sfilename=filename;
		try{
	        AccessController.doPrivileged(new PrivilegedAction() {
	            public Object run() {
	            	try {
						File file = new File(System.getProperty( "java.io.tmpdir" ) + File.separator + sfilename);

						if(!file.exists()){
							OutputStream outputStream = new FileOutputStream(file);
							InputStream inputStream = JNILibraryLoader.class.getClassLoader().getResourceAsStream(sfilename);
							if (inputStream != null) {
								int read;
								byte [] buffer = new byte [4096];
								while ((read = inputStream.read (buffer)) != -1) {
									outputStream.write(buffer, 0, read);
								}
								outputStream.close();
								inputStream.close();
							}
		            	}

						if(file.exists()){
							System.out.println("calling file.getAbsolutePath() : "+ file.getAbsolutePath());
							System.load(file.getAbsolutePath());
							//return true;
						}
						else
						{
							System.out.println("Can't find file " + file.getAbsolutePath());
							//return false;
						}
			            	}
	            	catch(Throwable e) {
	            		e.printStackTrace();
	            	}
	           		return null;
	            }
			    //return false; // nothing to return
	        });
		}catch(Throwable throwable){
			System.out.println("Error loading library : "+throwable.toString());
			return false;
		}/*finally{
			if(file.exists()){
				file.delete();
			}
		}*/
		return true;
	}
}


