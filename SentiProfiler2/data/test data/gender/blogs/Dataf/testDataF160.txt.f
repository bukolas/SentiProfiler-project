The Google Maps API provides a sophisticated way for developers and webmasters to add custom interactive maps to their websites. Version 3 of the API, released in May of 2009, represents a complete overhaul of the API in response to several years worth of user feedback on the previous version.

In this tutorial we’ll cover a few of the API’s simpler features by showing you how to add a map with a set of location markers to a website. Each marker will have an associated info bubble with the name and address of the location. What’s more, we’ll be loading the location data via Ajax, so this can be used as the first step towards developing a more sophisticated map-based application.

For example, if your site’s contact page shows the position of all your retail locations on a map, you could filter them dynamically (say, based on which ones offered certain features or were open on a given day) by sending those parameters to the server and displaying markers on the map based on the returned XML.

Before we start, you should have at least a basic understanding of jQuery. To learn more about any of the classes and methods we’ll be using, you can consult the Google Maps API reference. Before we start writing code, it’s best to examine the format of the XML data we’ll be using to load our location data.

The coordinates and information for each marker we want to place on our map will be contained in an XML file. This makes it easy to change it, or have it generated automatically by a server-side script pulling the information from a database. The XML is formatted as follows: The root element is markers, and it contains a series of marker  elements, each containing a text address, latitude, and longitude.

Before we can load this XML and use it to place markers on our map, we first need to include the Google Maps JavaScript and the jQuery library in our HTML page.
jQuery and the Maps API

The two libraries we’ll be relying on for our functionality are, unsurprisingly, jQuery and the Google Maps API library itself. As far as jQuery is concerned, you can simply download the latest version from the jQuery home page and include it in your HTML page as follows:We’re packaging all our map functionality inside a JavaScript object called MYMAP, which will help to avoid potential conflicts with other scripts on the page. The object contains two variables and two functions. The map variable will store a reference to the Google Map object we’ll create, and the bounds variable will store a bounding box that contains all our markers. This will be useful after we’ve added all the markers, when we want to zoom the map in such a way that they’re all visible at the same time.

Now for the methods: init will find an element on the page and initialize it as a new Google map with a given center and zoom level. placeMarkers, meanwhile, takes the name of an XML file and will load in coordinate data from that file to place a series of markers on the map.