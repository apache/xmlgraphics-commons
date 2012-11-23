package org.apache.xmlgraphics.image.writer;

import java.util.Iterator;

import javax.imageio.ImageIO;

public class ImageIOCheckUtility {

    /**
     * Determines whether the JAI ImageIO library is present to run tests
     * @return Returns true if the library is present.
     */
    public static boolean isSunTIFFImageWriterAvailable() {
        Iterator<javax.imageio.ImageWriter> tiffWriters
            = ImageIO.getImageWritersByMIMEType("image/tiff");
        boolean found = false;
        while (tiffWriters.hasNext()) {
            javax.imageio.ImageWriter writer = tiffWriters.next();
            if ("com.sun.media.imageioimpl.plugins.tiff.TIFFImageWriter".equals(
                    writer.getClass().getName())) {
                //JAI ImageIO implementation present
                found = true;
                break;
            }
        }
        return found;
    }
}
