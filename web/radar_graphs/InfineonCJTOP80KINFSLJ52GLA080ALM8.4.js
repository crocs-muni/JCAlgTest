var w = document.getElementById('chart').offsetWidth,
    h = window.innerHeight -70;
var colorscale = d3.scale.category10();
var data = [
[
{axis:"SECURE RANDOM (256B)",value:0.931,title:"24.9 ms"},
{axis:"SHA-1 hash (256B)",value:0.748,title:"17.42 ms"},
{axis:"SHA2-256 hash (256B)",value:0.719,title:"35.58 ms"},
{axis:"3DES encrypt (256B)",value:0.614,title:"61.49 ms"},
{axis:"AES128 encrypt (256B)",value:0.664,title:"25.53 ms"},
{axis:"AES256 encrypt (256B)",value:0.661,title:"31.18 ms"},
{axis:"3DES setKey(192b)",value:0.810,title:"6.61 ms"},
{axis:"AES setKey(128b)",value:0.767,title:"6.08 ms"},
{axis:"AES setKey(256b)",value:0.763,title:"6.65 ms"},
{axis:"SWAES oneblock (16B)",value:0.429,title:"1356.0 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.666,title:"2.13 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.946,title:"2.27 ms"},
{axis:"RSA1024 CRT decrypt",value:0.768,title:"101.92 ms"},
{axis:"RSA1024 CRT encrypt",value:0.724,title:"11.06 ms"},
{axis:"RSA2048 CRT decrypt",value:0.861,title:"242.91 ms"},
{axis:"RSA2048 CRT encrypt",value:0.791,title:"17.16 ms"},
{axis:"RSA1024 CRT generate",value:0.770,title:"1546.3 ms"},
{axis:"RSA2048 CRT generate",value:0.847,title:"11741.8 ms"},
{axis:"RSA1024 decrypt",value:0.750,title:"119.97 ms"},
{axis:"RSA1024 encrypt",value:0.725,title:"11.02 ms"},
{axis:"RSA2048 decrypt",value:0.788,title:"689.56 ms"},
{axis:"RSA2048 encrypt",value:0.792,title:"17.13 ms"},
{axis:"RSA1024 generate",value:0.738,title:"1749.9 ms"},
{axis:"RSA2048 generate",value:0.672,title:"16088.9 ms"},
],
];

var config = { w: w-175,
 h: h-175,
 maxValue: 1.0,
 levels: 10,
 }

RadarChart.draw("#chart", data, config);