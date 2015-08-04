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
                Title = "You are here!",
                NormalizedAnchorPoint = new Point() { X = 0.5, Y = 0.5 },
            };

            Map.MapElements.Add(pin);

            await Map.TrySetViewAsync(userLocation.Coordinate.Point, 17, 0, 0, MapAnimationKind.Bow);
        }
    }
}
