package pdftotiff;
import java.text.SimpleDateFormat;
import org.apache.log4j.Logger;
import java.io.*;
import java.util.*;
import java.util.Date;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.PDimension;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.util.GraphicsRenderingHints;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.IIOImage;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.DataBuffer;
import java.awt.*;
import java.io.*;
import java.util.Iterator;
import java.util.Properties;


public class Converttiff implements Runnable {

	 public static final double FAX_RESOLUTION = 200.0;
    public static final double PRINTER_RESOLUTION = 300.0;
    // This compression type may be wpecific to JAI ImageIO Tools
    public static final String COMPRESSION_TYPE_GROUP4FAX = "CCITT T.6";
    private static Logger logger = Logger.getLogger(Converttiff.class.getName());

    private String name;
     private String str_temp;
    private Thread t;
    private String SourcePath="";
    private String Destinationpath = "";
    private boolean bAPMoveFile=false;

    public Converttiff() {
    }

    public Converttiff(String threadName) {
       try
       {
	        readINI();
	        name = threadName;
	        t = new Thread(this, name);
	        t.start();
       }catch(Exception e)
       {
    	   System.out.println("Error in starting Converttiff Thread ...."+e);
       }

    }

    public void apiCall(String sPath,String Destpath) {
	try{
	     File dir = new File(sPath);
        System.out.println("File dir is :"+dir);
        if (dir == null)
        {
         return;
        }
        else
        {
        	try
        	{
	        	System.out.println("Creating CNU and Success Folders for ");
	        	File fSuccess=new File(Destpath +  System.getProperty("file.separator") + "Success");
	        	if(fSuccess == null || !fSuccess.isDirectory())
	        		fSuccess.mkdir();
	        	File fCNU=new File(Destpath +  System.getProperty("file.separator") + "CNU");
	        	if(fCNU == null || !fCNU.isDirectory())
	        		fCNU.mkdir();
	        	fSuccess=null;
	        	fCNU=null;
	        	System.out.println("Folders created successfully");
        	}catch(Exception e)
        	{
        		System.out.println("Exception:Error in creating CNU and Success Folders....");
        	}
        }
        String[] subfolder = dir.list();
        System.out.println("fil"+subfolder);
        if (subfolder == null) {

        } else {
        	   for (int i = 0; i < subfolder.length; i++) {

                    String filename = sPath +
                        System.getProperty("file.separator") + subfolder[i];

                    File subDir1 = new File(filename);
                    System.out.println("filename---");
                    String[] subfolder1 = subDir1.list();

                    if (subfolder1 == null) {
                    } else {
                        for (int k = 0; k < subfolder1.length; k++) {
                            File subdir = new File(filename +
                                    System.getProperty("file.separator") +
                                    subfolder1[k]);
                            FilenameFilter filterFile= new FilenameFilter()
                            {
                                public boolean accept(File dir, String name)
                                {
                                	try{
                	                	return (name.toUpperCase().endsWith(".PDF"));
                	               }
                                	catch(Exception e)
                                	{
                                		return false;
                                	}
                                }
							};
                            String[] subfile = subdir.list(filterFile);
                            if (subfile != null) {
                                for (int j = 0; j < subfile.length; j++) {
                                	str_temp = filename +
                                        System.getProperty("file.separator") +
                                        subfolder1[k] +
                                        System.getProperty("file.separator") +
                                        subfile[j];
                                	System.out.println("Processing: " + str_temp);
									conversion(str_temp,Destinationpath);

                              }

                            }
                        }
                    }




                }


        }
	}catch(Exception e)
	{
	   System.out.println("An error occurred while processing....."+e);
	}
    }


    public void run() {


	            while (true)
	            {
	            	try
	            	{
						apiCall(SourcePath,Destinationpath);
				        Thread.sleep(Long.parseLong("1000"));

	            	}
	            	catch(Exception ex)
	    	        {
	    	        	System.out.println("An interrupted exception has occurred....."+ex);
	    	        }
	        }


    }

    public void readINI() {
        try {

        	 Properties ini = new Properties();
			ini.load(new FileInputStream(System.getProperty("user.dir") + System.getProperty("file.separator") + "ConvertPDFtoTIFF.ini"));
			SourcePath=ini.getProperty("SourceFolder");
			Destinationpath=ini.getProperty("DestinationFolder");
           	}catch(Exception e) {
	            System.out.println("Error in reading ini file..."+e);
	            e.printStackTrace();
	            System.exit(0);
        	}
    }

 public void conversion(String FilePathSource,String FilePathDestination){
        // Verify that ImageIO can output TIFF
        Iterator<ImageWriter> iterator = ImageIO.getImageWritersByFormatName("tiff");
        if (!iterator.hasNext()) {
            System.out.println(
                "ImageIO missing required plug-in to write TIFF files. " +
                "You can download the JAI ImageIO Tools from: " +
                "https://jai-imageio.dev.java.net/");
            return;
        }
        boolean foundCompressionType = false;
        for(String type : iterator.next().getDefaultWriteParam().getCompressionTypes()) {
            if (COMPRESSION_TYPE_GROUP4FAX.equals(type)) {
                foundCompressionType = true;
                break;
            }
        }
        if (!foundCompressionType) {
            System.out.println(
                "TIFF ImageIO plug-in does not support Group 4 Fax " +
                "compression type ("+COMPRESSION_TYPE_GROUP4FAX+")");
            return;
        }
        // Get a file from the command line to open
        String filePath = FilePathSource;
        // open the url
        Document document = new Document();
        try {
            document.setFile(filePath);
        } catch (PDFException ex) {
            System.out.println("Error parsing PDF document " + ex);
        } catch (PDFSecurityException ex) {
            System.out.println("Error encryption not supported " + ex);
        } catch (FileNotFoundException ex) {
            System.out.println("Error file not found " + ex);
        } catch (IOException ex) {
            System.out.println("Error handling PDF document " + ex);
        }
        try {
            // save page caputres to file.
            File file = new File("imageCapture.tif");
            ImageOutputStream ios = ImageIO.createImageOutputStream(file);
            ImageWriter writer = ImageIO.getImageWritersByFormatName("tiff").next();
            writer.setOutput(ios);
            // Paint each pages content to an image and write the image to file
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                final double targetDPI = PRINTER_RESOLUTION;
                float scale = 1.0f;
                float rotation = 0f;
                // Given no initial zooming, calculate our natural DPI when
                // printed to standard US Letter paper
                PDimension size = document.getPageDimension(i, rotation, scale);
                double dpi = Math.sqrt((size.getWidth()*size.getWidth()) +
                                       (size.getHeight()*size.getHeight()) ) /
                             Math.sqrt((8.5*8.5)+(11*11));
                // Calculate scale required to achieve at least our target DPI
                if (dpi < (targetDPI-0.1)) {
                    scale = (float) (targetDPI / dpi);
                    size = document.getPageDimension(i, rotation, scale);
                }
                int pageWidth = (int) size.getWidth();
                int pageHeight = (int) size.getHeight();
                int[] cmap = new int[] { 0xFF000000, 0xFFFFFFFF };
                IndexColorModel cm = new IndexColorModel(
                    1, cmap.length, cmap, 0, false, Transparency.BITMASK,
                    DataBuffer.TYPE_BYTE);
                BufferedImage image = new BufferedImage(
                    pageWidth, pageHeight, BufferedImage.TYPE_BYTE_BINARY, cm);
                Graphics g = image.createGraphics();
                document.paintPage(
                    i, g, GraphicsRenderingHints.PRINT, Page.BOUNDARY_CROPBOX,
                    rotation, scale);
                g.dispose();
                // capture the page image to file
                IIOImage img = new IIOImage(image, null, null);
                ImageWriteParam param = writer.getDefaultWriteParam();
                param.setCompressionMode(param.MODE_EXPLICIT);
                param.setCompressionType(COMPRESSION_TYPE_GROUP4FAX);
                if (i == 0) {
                    writer.write(null, img, param);
                }
                else {
                    writer.writeInsert(-1, img, param);
                }
                image.flush();
            }
            ios.flush();
            ios.close();
            writer.dispose();
           System.out.println("File coverted");
        }
        catch(IOException e) {
            System.out.println("Error saving file " + e);
            e.printStackTrace();
        }
        // clean up resources\
        document.dispose();
}


}
