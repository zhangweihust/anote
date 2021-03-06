package com.android.note.gesture;

import android.util.Log;
import static com.android.note.gesture.AmGestureConstants.*;
import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class AmGestureLibraries {
    private AmGestureLibraries() {
    }

    public static AmGestureLibrary fromFile(String path) {
        return fromFile(new File(path));
    }
    
    public static AmGestureLibrary fromZipFile(String path) {
        return new ZipFileGestureLibrary(new File(path));
    }

    public static AmGestureLibrary fromFile(File path) {
        return new FileGestureLibrary(path);
    }

    public static AmGestureLibrary fromPrivateFile(Context context, String name) {
        return fromFile(context.getFileStreamPath(name));
    }

    public static AmGestureLibrary fromRawResource(Context context, int resourceId) {
        return new ResourceGestureLibrary(context, resourceId);
    }

    private static class FileGestureLibrary extends AmGestureLibrary {
        private final File mPath;

        public FileGestureLibrary(File path) {
            mPath = path;
        }

        @Override
        public boolean isReadOnly() {
            return !mPath.canWrite();
        }

        public boolean save(boolean flag) {
            if (!mStore.hasChanged()) return true;

            final File file = mPath;

            final File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                if (!parentFile.mkdirs()) {
                    return false;
                }
            }

            boolean result = false;
            ZipOutputStream zipOut = null;
            try {
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
	            FileOutputStream out = new FileOutputStream(file);  
                zipOut = new ZipOutputStream(out);  
	            ZipEntry entry = new ZipEntry("hello");  
	            zipOut.putNextEntry(entry); 
                mStore.save(/*new FileOutputStream(file)*/zipOut, true,flag);
                result = true;
            } catch (FileNotFoundException e) {
                Log.d(LOG_TAG, "Could not save the gesture library in " + mPath, e);
            } catch (IOException e) {
                Log.d(LOG_TAG, "Could not save the gesture library in " + mPath, e);
            } finally {
            	try {
            		if (zipOut != null) {
					    zipOut.close();
            		}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.d(LOG_TAG, "Could not save the gesture library in " + mPath, e);
				} 
            }

            return result;
        }
        

        public boolean load(boolean flag) {
            boolean result = false;
            final File file = mPath;
            ZipInputStream zis = null;
            if (file.exists() && file.canRead()) {
                try {
                	FileInputStream fis = new FileInputStream(file);
    	     	    zis = new ZipInputStream(new BufferedInputStream(fis));
    	     	    ZipEntry entry = zis.getNextEntry();
    	     	    mStore.load(/*new FileInputStream(file)*/zis, true,flag);
                    result = true;
                } catch (FileNotFoundException e) {
                    Log.d(LOG_TAG, "Could not load the gesture library from " + mPath, e);
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Could not load the gesture library from " + mPath, e);
                } finally {
                	try {
						zis.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.d(LOG_TAG, "Could not load the gesture library from " + mPath, e);
					}
                }
            }
            return result;
        }

    }
    
    private static class ZipFileGestureLibrary extends AmGestureLibrary {
        private final File mPath;

        public ZipFileGestureLibrary(File path) {
            mPath = path;
        }

        @Override
        public boolean isReadOnly() {
            return !mPath.canWrite();
        }

        public boolean load(boolean flag) {
            boolean result = false;
            final File file = mPath;
            ZipInputStream zis = null;
            if (file.exists() && file.canRead()) {
                try {
                	ZipFile zip = new ZipFile(file);//由指定的File对象打开供阅读的ZIP文件  
        			Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zip.entries();//获取zip文件中的各条目（子文件）  
        			while(entries.hasMoreElements()){//依次访问各条目  
        				ZipEntry ze = (ZipEntry) entries.nextElement(); 
        				if (ze.getName().endsWith("gesture") ) {
        					zis = new ZipInputStream(zip.getInputStream(ze));
        					zis.getNextEntry();
        					break;
        				}
        			}
        			if (zis != null) {
    	     	        mStore.load(/*new FileInputStream(file)*/zis, true,flag);
    	     	        result = true;
        			}
                    
                } catch (FileNotFoundException e) {
                    Log.d(LOG_TAG, "Could not load the gesture library from " + mPath, e);
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Could not load the gesture library from " + mPath, e);
                } finally {
                	try {
                		if (zis != null) {
						    zis.close();
                		}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.d(LOG_TAG, "Could not load the gesture library from " + mPath, e);
					}
                }
            }
            return result;
        }

		@Override
		public boolean save(boolean flag) {
			// TODO Auto-generated method stub
			return false;
		}

    }

    private static class ResourceGestureLibrary extends AmGestureLibrary {
        private final WeakReference<Context> mContext;
        private final int mResourceId;

        public ResourceGestureLibrary(Context context, int resourceId) {
            mContext = new WeakReference<Context>(context);
            mResourceId = resourceId;
        }

        @Override
        public boolean isReadOnly() {
            return true;
        }

        public boolean save(boolean flag) {
            return false;
        }

        public boolean load(boolean flag) {
            boolean result = false;
            final Context context = mContext.get();
            if (context != null) {
                final InputStream in = context.getResources().openRawResource(mResourceId);
                try {
                    mStore.load(in, true,flag);
                    result = true;
                } catch (IOException e) {
                    Log.d(LOG_TAG, "Could not load the gesture library from raw resource " +
                            context.getResources().getResourceName(mResourceId), e);
                }
            }
            return result;
        }

    }
}
