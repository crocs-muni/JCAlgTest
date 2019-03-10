var w = document.getElementById('chart').offsetWidth,
    h = window.innerHeight -70;
var colorscale = d3.scale.category10();
var data = [
[
{axis:"SECURE RANDOM (256B)",value:0.978,title:"7.95 ms"},
{axis:"SHA-1 hash (256B)",value:0.805,title:"13.48 ms"},
{axis:"SHA2-256 hash (256B)",value:0.817,title:"23.25 ms"},
{axis:"3DES encrypt (256B)",value:0.099,title:"143.56 ms"},
{axis:"AES128 encrypt (256B)",value:0.099,title:"68.39 ms"},
{axis:"AES256 encrypt (256B)",value:0.099,title:"82.77 ms"},
{axis:"3DES setKey(192b)",value:0.132,title:"30.2 ms"},
{axis:"AES setKey(128b)",value:0.208,title:"20.65 ms"},
{axis:"AES setKey(256b)",value:0.114,title:"24.9 ms"},
{axis:"SWAES oneblock (16B)",value:0.894,title:"252.23 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.812,title:"-1.2 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.960,title:"1.68 ms"},
{axis:"RSA1024 CRT decrypt",value:0.731,title:"118.52 ms"},
{axis:"RSA1024 CRT encrypt",value:0.0,title:"NS"},
{axis:"RSA2048 CRT decrypt",value:0.657,title:"598.26 ms"},
{axis:"RSA2048 CRT encrypt",value:0.0,title:"NS"},
{axis:"RSA1024 CRT generate",value:0.635,title:"2455.5 ms"},
{axis:"RSA2048 CRT generate",value:0.703,title:"22815.2 ms"},
{axis:"RSA1024 decrypt",value:0.382,title:"295.88 ms"},
{axis:"RSA1024 encrypt",value:0.767,title:"9.34 ms"},
{axis:"RSA2048 decrypt",value:0.379,title:"2017.3 ms"},
{axis:"RSA2048 encrypt",value:0.760,title:"19.72 ms"},
{axis:"RSA1024 generate",value:0.443,title:"3717.3 ms"},
{axis:"RSA2048 generate",value:0.638,title:"17738.6 ms"},
],
];

var config = { w: w-175,
 h: h-175,
 maxValue: 1.0,
 levels: 10,
 }

RadarChart.draw("#chart", data, config);