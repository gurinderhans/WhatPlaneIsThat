using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Windows.Devices.Geolocation;

namespace WhatPlaneIsThat
{
    class GeoLocation
    {
        private GeoLocation()
        {
            // empty constructor
        }

        private static double DegreeToRadian(double angle)
        {
            return Math.PI * angle / 180.0;
        }

        private static double RadianToDegree(double angle)
        {
            return angle * (180.0 / Math.PI);
        }

        /**
         * Calculates LatLng of some point at a distance from given latitude, longitude at an angle
         * @param location - given location
         * @param bearing  - give bearing / angle (in degrees)
         * @param distance - distance in Km
         * @return - new LatLng that is distance away from current point at some angle
        */
        public static Geopoint boundingBox(Geocoordinate location, double bearing, double distance)
        {

            float radius = 6378.1f;
            double latitude = location.Latitude;
            double longitude = location.Longitude;

            // new latitude
            double nLat = RadianToDegree(Math.Asin(Math.Sin(DegreeToRadian(latitude)) * Math.Cos(distance / radius) + Math.Cos(DegreeToRadian(latitude)) * Math.Sin(distance / radius) * Math.Cos(DegreeToRadian(bearing))));
            double nLng = RadianToDegree(DegreeToRadian(longitude) + Math.Atan2(Math.Sin(DegreeToRadian(bearing)) * Math.Sin(distance / radius) * Math.Cos(DegreeToRadian(latitude)), Math.Cos(distance / radius) - Math.Sin(DegreeToRadian(latitude)) * Math.Sin(DegreeToRadian(nLat))));

            return new Geopoint(new BasicGeoposition() { Latitude = nLat, Longitude = nLng });
        }

    }
}
