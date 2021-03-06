/**
 * 
 */
package icy.imagej;

import icy.common.listener.ProgressListener;
import icy.image.IcyBufferedImage;
import icy.math.ArrayMath;
import icy.roi.ROI;
import icy.roi.ROI2D;
import icy.roi.ROI2DArea;
import icy.roi.ROI2DEllipse;
import icy.roi.ROI2DLine;
import icy.roi.ROI2DPath;
import icy.roi.ROI2DPoint;
import icy.roi.ROI2DPolyLine;
import icy.roi.ROI2DPolygon;
import icy.roi.ROI2DRectangle;
import icy.roi.ROI2DShape;
import icy.sequence.Sequence;
import icy.type.DataType;
import icy.type.collection.array.Array2DUtil;
import icy.type.collection.array.ArrayUtil;
import ij.CompositeImage;
import ij.ImagePlus;
import ij.ImageStack;
import ij.LookUpTable;
import ij.gui.ImageRoi;
import ij.gui.Line;
import ij.gui.OvalRoi;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.measure.Calibration;
import ij.process.FloatPolygon;
import ij.process.ImageProcessor;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

/**
 * ImageJ utilities class.
 * 
 * @author Stephane
 */
public class ImageJUtil
{
    /**
     * Calibrate the specified Icy {@link Sequence} from the specified ImageJ {@link Calibration}
     * object.
     */
    private static void calibrateIcySequence(Sequence sequence, Calibration cal)
    {
        if (cal != null)
        {
            if (cal.scaled())
            {
                // TODO : apply unit conversion
                sequence.setPixelSizeX(cal.pixelWidth);
                sequence.setPixelSizeY(cal.pixelHeight);
                sequence.setPixelSizeZ(cal.pixelDepth);
            }

            // TODO : apply unit conversion
            sequence.setTimeInterval(cal.frameInterval);
        }
    }

    /**
     * Calibrate the specified ImageJ {@link ImagePlus} from the specified Icy {@link Sequence}.
     */
    private static void calibrateImageJImage(ImagePlus image, Sequence seq)
    {
        final Calibration cal = image.getCalibration();

        final double psx = seq.getPixelSizeX();
        final double psy = seq.getPixelSizeY();
        final double psz = seq.getPixelSizeZ();

        // different from defaults values ?
        if ((psx != 1d) || (psy != 1d) || (psz != 1d))
        {
            cal.pixelWidth = psx;
            cal.pixelHeight = psy;
            cal.pixelDepth = psz;
            // default unit size icy
            cal.setUnit("mm");
        }

        final double ti = seq.getTimeInterval();
        // different from default value
        if (ti != 1d)
        {
            cal.frameInterval = ti;
            cal.setTimeUnit("msec");
        }

        image.setDimensions(seq.getSizeC(), seq.getSizeZ(), seq.getSizeT());
        image.setOpenAsHyperStack(image.getNDimensions() > 3);

        // final ImageProcessor ip = image.getProcessor();
        // ip.setMinAndMax(seq.getChannelMin(0) displayMin, displayMax);
    }

    /**
     * Convert the specified ImageJ {@link ImagePlus} object to Icy {@link Sequence}.
     */
    public static Sequence convertToIcySequence(ImagePlus image, ProgressListener progressListener)
    {
        final Sequence result = new Sequence(image.getTitle());
        final int[] dim = image.getDimensions(true);

        final int sizeX = dim[0];
        final int sizeY = dim[1];
        final int sizeC = dim[2];
        final int sizeZ = dim[3];
        final int sizeT = dim[4];
        final int type = image.getType();
        // only integer signed type allowed in ImageJ is 16 bit signed
        final boolean signed16 = image.getLocalCalibration().isSigned16Bit();

        final int len = sizeZ * sizeT;
        int position = 0;

        result.beginUpdate();
        try
        {
            // convert image
            for (int t = 0; t < sizeT; t++)
            {
                for (int z = 0; z < sizeZ; z++)
                {
                    if (progressListener != null)
                        progressListener.notifyProgress(position, len);

                    image.setPosition(1, z + 1, t + 1);

                    // separate RGB channel
                    if ((sizeC == 1) && ((type == ImagePlus.COLOR_256) || (type == ImagePlus.COLOR_RGB)))
                        result.setImage(t, z, IcyBufferedImage.createFrom(image.getBufferedImage()));
                    else
                    {
                        final ImageProcessor ip = image.getProcessor();
                        final Object data = ip.getPixels();
                        final DataType dataType = ArrayUtil.getDataType(data);
                        final Object[] datas = Array2DUtil.createArray(dataType, sizeC);

                        // first channel data
                        datas[0] = data;
                        // special case of 16 bits signed data --> subtract 32768
                        if (signed16)
                            datas[0] = ArrayMath.subtract(datas[0], Double.valueOf(32768));

                        // others channels data
                        for (int c = 1; c < sizeC; c++)
                        {
                            image.setPosition(c + 1, z + 1, t + 1);
                            datas[c] = image.getProcessor().getPixels();
                            // special case of 16 bits signed data --> subtract 32768
                            if (signed16)
                                datas[c] = ArrayMath.subtract(datas, Double.valueOf(32768));
                        }

                        // create a single image from all channels
                        result.setImage(t, z, new IcyBufferedImage(sizeX, sizeY, datas, signed16));

                        position++;
                    }
                }
            }

            // convert ROI
            final Roi roi = image.getRoi();
            if (roi != null)
            {
                for (ROI r : convertToIcyRoi(roi))
                    result.addROI(r);
            }

            // calibrate
            calibrateIcySequence(result, image.getCalibration());
        }
        finally
        {
            result.endUpdate();
        }

        return result;
    }

    /**
     * Convert the specified Icy {@link Sequence} object to ImageJ {@link ImagePlus}.
     */
    public static ImagePlus convertToImageJImage(Sequence sequence, ProgressListener progressListener)
    {
        final int sizeX = sequence.getSizeX();
        final int sizeY = sequence.getSizeY();
        final int sizeC = sequence.getSizeC();
        final int sizeZ = sequence.getSizeZ();
        final int sizeT = sequence.getSizeT();

        final int len = sizeZ * sizeT * sizeC;
        int position = 0;

        final ImageStack stack = new ImageStack(sizeX, sizeY, LookUpTable.createGrayscaleColorModel(false));

        for (int t = 0; t < sizeT; t++)
        {
            for (int z = 0; z < sizeZ; z++)
            {
                for (int c = 0; c < sizeC; c++)
                {
                    if (progressListener != null)
                        progressListener.notifyProgress(position, len);

                    stack.addSlice(null, sequence.getDataXY(t, z, c));

                    position++;
                }
            }
        }

        // create the image
        final ImagePlus result = new ImagePlus(sequence.getName(), stack);

        // convert ROI
        final ArrayList<ROI2D> rois = sequence.getROI2Ds();
        if (rois.size() > 0)
            result.setRoi(convertToImageJRoi(rois.get(0)));
        // calibrate
        calibrateImageJImage(result, sequence);

        if (result.getNChannels() > 4)
            return new CompositeImage(result, CompositeImage.COLOR);
        else if (result.getNChannels() > 1)
            return new CompositeImage(result, CompositeImage.COMPOSITE);

        return result;
    }

    /**
     * Convert the specified ImageJ {@link Roi} object to Icy {@link ROI}.
     */
    public static ArrayList<ROI2D> convertToIcyRoi(Roi roi)
    {
        final ArrayList<ROI2D> result = new ArrayList<ROI2D>();
        final ArrayList<Point2D> pts = new ArrayList<Point2D>();
        final FloatPolygon fp;

        switch (roi.getType())
        {
            default:
                result.add(new ROI2DRectangle(roi.getFloatBounds()));
                break;

            case Roi.OVAL:
                result.add(new ROI2DEllipse(roi.getFloatBounds()));
                break;

            case Roi.LINE:
                final Rectangle2D rect = roi.getFloatBounds();
                final double x = rect.getX();
                final double y = rect.getY();
                result.add(new ROI2DLine(new Point2D.Double(x, y), new Point2D.Double(x + rect.getWidth(), y
                        + rect.getHeight())));
                break;

            case Roi.POLYGON:
            case Roi.TRACED_ROI:
            case Roi.FREEROI:
                fp = ((PolygonRoi) roi).getFloatPolygon();
                for (int p = 0; p < fp.npoints; p++)
                    pts.add(new Point2D.Float(fp.xpoints[p], fp.ypoints[p]));

                final ROI2DPolygon roiPolygon = new ROI2DPolygon();
                roiPolygon.setPoints(pts);

                result.add(roiPolygon);
                break;

            case Roi.FREELINE:
            case Roi.POLYLINE:
            case Roi.ANGLE:
                fp = ((PolygonRoi) roi).getFloatPolygon();
                for (int p = 0; p < fp.npoints; p++)
                    pts.add(new Point2D.Float(fp.xpoints[p], fp.ypoints[p]));

                final ROI2DPolyLine roiPolyline = new ROI2DPolyLine();
                roiPolyline.setPoints(pts);

                result.add(roiPolyline);
                break;

            case Roi.COMPOSITE:
                final ROI2DPath roiPath = new ROI2DPath(((ShapeRoi) roi).getShape());
                final Rectangle2D.Double roiBounds = roi.getFloatBounds();
                // we have to adjust position as Shape do not contains it
                roiPath.setPosition(new Point2D.Double(roiBounds.x, roiBounds.y));
                result.add(roiPath);
                break;

            case Roi.POINT:
                fp = ((PolygonRoi) roi).getFloatPolygon();
                for (int p = 0; p < fp.npoints; p++)
                    pts.add(new Point2D.Float(fp.xpoints[p], fp.ypoints[p]));

                for (Point2D pt : pts)
                    result.add(new ROI2DPoint(pt));
                break;
        }

        for (ROI2D r : result)
        {
            r.setC(roi.getCPosition() - 1);
            r.setZ(roi.getZPosition() - 1);
            r.setT(roi.getTPosition() - 1);
            r.setSelected(false, false);
        }

        return result;
    }

    /**
     * Convert the specified Icy {@link ROI} object to ImageJ {@link Roi}.
     */
    public static Roi convertToImageJRoi(ROI2D roi)
    {
        final Roi result;

        if (roi instanceof ROI2DShape)
        {
            final ArrayList<Point2D> pts = ((ROI2DShape) roi).getPoints();

            if (roi instanceof ROI2DPoint)
            {
                final Point2D p = pts.get(0);
                result = new PointRoi(p.getX(), p.getY());
            }
            else if (roi instanceof ROI2DLine)
            {
                final Point2D p1 = pts.get(0);
                final Point2D p2 = pts.get(1);
                result = new Line(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            }
            else if (roi instanceof ROI2DRectangle)
            {
                final Rectangle2D r = roi.getBounds2D();
                result = new Roi(r.getX(), r.getY(), r.getWidth(), r.getHeight(), 0);
            }
            else if (roi instanceof ROI2DEllipse)
            {
                final Rectangle2D r = roi.getBounds2D();
                result = new OvalRoi(r.getX(), r.getY(), r.getWidth(), r.getHeight());
            }
            else if ((roi instanceof ROI2DPolyLine) || (roi instanceof ROI2DPolygon))
            {
                final FloatPolygon fp = new FloatPolygon();
                for (Point2D p : pts)
                    fp.addPoint(p.getX(), p.getY());
                if (roi instanceof ROI2DPolyLine)
                    result = new PolygonRoi(fp, Roi.POLYLINE);
                else
                    result = new PolygonRoi(fp, Roi.POLYGON);
            }
            else
                // create compatible shape ROI
                result = new ShapeRoi(((ROI2DPath) roi).getShape());
        }
        else if (roi instanceof ROI2DArea)
        {
            final ROI2DArea roiArea = (ROI2DArea) roi;
            final Point p = roiArea.getPosition();
            result = new ImageRoi(p.x, p.y, roiArea.getImageMask());
            ((ImageRoi) result).setOpacity(roiArea.getPainter().getAlphaLevel());
        }
        else
        {
            // create standard ROI
            final Rectangle2D r = roi.getBounds2D();
            result = new Roi(r.getX(), r.getY(), r.getWidth(), r.getHeight());
        }

        result.setPosition(roi.getC() + 1, roi.getZ() + 1, roi.getT() + 1);
        result.setName(roi.getName());
        result.setStrokeColor(roi.getColor());

        return result;
    }
}
