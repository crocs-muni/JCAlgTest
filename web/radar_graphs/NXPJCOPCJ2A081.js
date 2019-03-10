var w = document.getElementById('chart').offsetWidth,
    h = window.innerHeight -70;
var colorscale = d3.scale.category10();
var data = [
[
{axis:"SECURE RANDOM (256B)",value:0.961,title:"14.14 ms"},
{axis:"SHA-1 hash (256B)",value:0.828,title:"11.9 ms"},
{axis:"SHA2-256 hash (256B)",value:0.823,title:"22.46 ms"},
{axis:"3DES encrypt (256B)",value:0.917,title:"13.3 ms"},
{axis:"AES128 encrypt (256B)",value:0.858,title:"10.78 ms"},
{axis:"AES256 encrypt (256B)",value:0.871,title:"11.81 ms"},
{axis:"3DES setKey(192b)",value:0.845,title:"5.39 ms"},
{axis:"AES setKey(128b)",value:0.800,title:"5.22 ms"},
{axis:"AES setKey(256b)",value:0.802,title:"5.56 ms"},
{axis:"SWAES oneblock (16B)",value:0.857,title:"339.14 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.919,title:"0.52 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.955,title:"1.9 ms"},
{axis:"RSA1024 CRT decrypt",value:0.813,title:"82.4 ms"},
{axis:"RSA1024 CRT encrypt",value:0.791,title:"8.37 ms"},
{axis:"RSA2048 CRT decrypt",value:0.754,title:"428.54 ms"},
{axis:"RSA2048 CRT encrypt",value:0.756,title:"20.04 ms"},
{axis:"RSA1024 CRT generate",value:0.729,title:"1822.9 ms"},
{axis:"RSA2048 CRT generate",value:0.708,title:"22373.3 ms"},
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