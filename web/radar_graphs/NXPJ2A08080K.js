var w = document.getElementById('chart').offsetWidth,
    h = window.innerHeight -70;
var colorscale = d3.scale.category10();
var data = [
[
{axis:"SECURE RANDOM (256B)",value:0.945,title:"19.59 ms"},
{axis:"SHA-1 hash (256B)",value:0.551,title:"31.09 ms"},
{axis:"SHA2-256 hash (256B)",value:0.526,title:"60.16 ms"},
{axis:"3DES encrypt (256B)",value:0.886,title:"18.11 ms"},
{axis:"AES128 encrypt (256B)",value:0.755,title:"18.57 ms"},
{axis:"AES256 encrypt (256B)",value:0.781,title:"20.12 ms"},
{axis:"3DES setKey(192b)",value:0.648,title:"12.24 ms"},
{axis:"AES setKey(128b)",value:0.543,title:"11.91 ms"},
{axis:"AES setKey(256b)",value:0.553,title:"12.58 ms"},
{axis:"SWAES oneblock (16B)",value:0.814,title:"441.77 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.882,title:"0.75 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.924,title:"3.19 ms"},
{axis:"RSA1024 CRT decrypt",value:0.719,title:"123.75 ms"},
{axis:"RSA1024 CRT encrypt",value:0.686,title:"12.57 ms"},
{axis:"RSA2048 CRT decrypt",value:0.630,title:"644.05 ms"},
{axis:"RSA2048 CRT encrypt",value:0.627,title:"30.67 ms"},
{axis:"RSA1024 CRT generate",value:0.553,title:"3004.3 ms"},
{axis:"RSA2048 CRT generate",value:0.672,title:"25156.2 ms"},
{axis:"RSA1024 decrypt",value:0.0,title:"NS"},
{axis:"RSA1024 encrypt",value:0.0,title:"NS"},
{axis:"RSA2048 decrypt",value:0.0,title:"NS"},
{axis:"RSA2048 encrypt",value:0.0,title:"NS"},
{axis:"RSA1024 generate",value:0.0,title:"NS"},
{axis:"RSA2048 generate",value:0.0,title:"NS"},
],
];

var config = { w: w-175,
 h: h-175,
 maxValue: 1.0,
 levels: 10,
 }

RadarChart.draw("#chart", data, config);