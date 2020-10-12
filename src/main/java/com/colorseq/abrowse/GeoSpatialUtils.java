package com.colorseq.abrowse;

import com.mongodb.client.model.geojson.LineString;
import com.mongodb.client.model.geojson.Polygon;
import com.mongodb.client.model.geojson.Position;

import java.util.ArrayList;
import java.util.List;

public class GeoSpatialUtils {

    private static final double half_rectangle_height_latitude = 0.3;
    // 临时实现
    public static final double chrNum = 250;


    public static double getLongitude(int coordinate, int chrLen) {

        return (((double) coordinate * 180) / (double) chrLen) - 90;
    }

    public static LineString getLineString(int start, int end, int chrLen, int chrCode) {

        double longitudeStart = GeoSpatialUtils.getLongitude(start, chrLen);
        double longitudeEnd = GeoSpatialUtils.getLongitude(end, chrLen);

        //Position startP = new Position(longitudeStart, (double) chrCode);
        //Position endP = new Position(longitudeEnd, (double) chrCode);
        Position startP = new Position(longitudeStart, (((double) chrCode) * 180 / chrNum) - 90);
        Position endP = new Position(longitudeEnd, (((double) chrCode) * 180 / chrNum) - 90);
        List<Position> posList = new ArrayList<Position>();
        posList.add(startP);
        posList.add(endP);
        LineString location = new LineString(posList);

        return location;
    }

    public static Polygon getQueryRectangle(int start, int end, int chrLen, int chrCode) {

        double longitudeStart = GeoSpatialUtils.getLongitude(start, chrLen);
//        BigDecimal longitudeStartBig = new BigDecimal(longitudeStart).setScale(19);
//        longitudeStart = longitudeStartBig.doubleValue();

        double longitudeEnd = GeoSpatialUtils.getLongitude(end, chrLen);
//        BigDecimal longitudeEndBig = new BigDecimal(longitudeEnd).setScale(19);
//        longitudeEnd = longitudeEndBig.doubleValue();

        if(longitudeStart>180){
            System.out.println("start====="+start+"     len===="+chrLen);
            longitudeStart = 180;
        }
        if(longitudeEnd>180){
            System.out.println("end====="+end+"     len===="+chrLen);
            longitudeEnd = 180;
        }

        double latitudeTop = (chrCode * 180 / chrNum) - 90 - half_rectangle_height_latitude;
        double latitudeBottom = (chrCode * 180 / chrNum) - 90 + half_rectangle_height_latitude;



        Position topLeft = new Position(longitudeStart, latitudeTop);
        Position topRight = new Position(longitudeEnd, latitudeTop);
        Position bottomRight = new Position(longitudeEnd, latitudeBottom);
        Position bottomLeft = new Position(longitudeStart, latitudeBottom);

        List<Position> exterior = new ArrayList<>(5);
        exterior.add(topLeft);
        exterior.add(topRight);
        exterior.add(bottomRight);
        exterior.add(bottomLeft);
        exterior.add(topLeft);

        return new Polygon(exterior);
    }
}
