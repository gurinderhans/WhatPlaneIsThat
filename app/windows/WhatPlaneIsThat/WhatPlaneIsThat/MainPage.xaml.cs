using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices.WindowsRuntime;
using Windows.Foundation;
using Windows.Foundation.Collections;
using Windows.UI.Xaml;
using Windows.UI.Xaml.Controls;
using Windows.UI.Xaml.Controls.Primitives;
using Windows.UI.Xaml.Data;
using Windows.UI.Xaml.Input;
using Windows.UI.Xaml.Media;
using Windows.UI.Xaml.Navigation;
using Windows.UI.Xaml.Controls.Maps;
using Windows.Devices.Geolocation;
using System.Net;
using System.Diagnostics;
using Windows.Data.Json;
using System.Net.Http;
using System.Threading.Tasks;


// The Blank Page item template is documented at http://go.microsoft.com/fwlink/?LinkId=402352&clcid=0x409

namespace WhatPlaneIsThat
{
    /// <summary>
    /// An empty page that can be used on its own or navigated to within a Frame.
    /// </summary>
    public sealed partial class MainPage : Page
    {
        public MainPage()
        {
            this.InitializeComponent();
        }

        private async void Map_Loaded(object sender, RoutedEventArgs e)
        {
            // get user location
            var locator = new Geolocator() { DesiredAccuracy = PositionAccuracy.High };
            var userLocation = await locator.GetGeopositionAsync();

            var pin = new MapIcon()
            {
                Location = userLocation.Coordinate.Point,
                Title = "You",
                NormalizedAnchorPoint = new Point() { X = 0.5, Y = 0.5 },
            };

            Map.MapElements.Add(pin);

            var nw = GeoLocation.boundingBox(userLocation.Coordinate, 315, 100).Position;
            var se = GeoLocation.boundingBox(userLocation.Coordinate, 135, 100).Position;

            // request plane data
            var BASE_URL = "http://lhr.data.fr24.com/zones/fcgi/feed.js";
            var OPTIONS_FORMAT = String.Format("?bounds={0},{1},{2},{3}&faa=1&mlat=1&flarm=1&adsb=1&gnd=1&air=1&vehicles=1&estimated=1&maxage=900&gliders=1&stats=1&", 
                nw.Latitude, se.Latitude, nw.Longitude, se.Longitude);

            JsonObject data = await GetAsync(BASE_URL + OPTIONS_FORMAT);
            plotPlaneData(data);

            // zoom map to user location
            await Map.TrySetViewAsync(userLocation.Coordinate.Point, 12, 0, 0, MapAnimationKind.Bow);

        }

        public async Task<JsonObject> GetAsync(string uri)
        {
            var httpClient = new HttpClient();
            var content = await httpClient.GetStringAsync(uri);
            return await Task.Run(()=> JsonObject.Parse(content));
        }

        public void plotPlaneData(JsonObject jsonObj)
        {
            try
            {

                foreach (string key in jsonObj.Keys)
                {
                    try
                    {
                        // data type array is plane data type
                        JsonArray plane = jsonObj[key].GetArray();

                        // plot the plane on map
                        var latitude = plane[1].GetNumber();
                        var longitude = plane[2].GetNumber();

                        var planeGeoPoint = new Geopoint(new BasicGeoposition() { Latitude = latitude, Longitude = longitude });

                        Debug.WriteLine(planeGeoPoint.Position.Latitude + " " + planeGeoPoint.Position.Longitude);

                        var pin = new MapIcon()
                        {
                            Location = planeGeoPoint,
                            Title = "Plane",
                        };
                        Map.MapElements.Add(pin);

                    }
                    catch (Exception e)
                    {
                        // Exception can be ignored as we do not care about data that we can't conver to type 
                    }
                }

            }
            catch (WebException e)
            {
                Debug.WriteLine("No plane data recieved.");
                return;
            }
        }
    }
}
