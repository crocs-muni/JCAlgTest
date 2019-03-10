var w = document.getElementById('chart').offsetWidth,
    h = window.innerHeight -70;
var colorscale = d3.scale.category10();
var data = [
[
{axis:"SECURE RANDOM (256B)",value:0.950,title:"18.03 ms"},
{axis:"SHA-1 hash (256B)",value:0.846,title:"10.67 ms"},
{axis:"SHA2-256 hash (256B)",value:0.692,title:"39.07 ms"},
{axis:"3DES encrypt (256B)",value:0.952,title:"7.67 ms"},
{axis:"AES128 encrypt (256B)",value:0.714,title:"21.7 ms"},
{axis:"AES256 encrypt (256B)",value:0.756,title:"22.42 ms"},
{axis:"3DES setKey(192b)",value:0.690,title:"10.8 ms"},
{axis:"AES setKey(128b)",value:0.723,title:"7.21 ms"},
{axis:"AES setKey(256b)",value:0.592,title:"11.46 ms"},
{axis:"SWAES oneblock (16B)",value:0.0,title:"NS"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.0,title:"NS"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.0,title:"NS"},
{axis:"RSA1024 CRT decrypt",value:0.842,title:"69.55 ms"},
{axis:"RSA1024 CRT encrypt",value:0.824,title:"7.04 ms"},
{axis:"RSA2048 CRT decrypt",value:0.731,title:"469.21 ms"},
{axis:"RSA2048 CRT encrypt",value:0.841,title:"13.08 ms"},
{axis:"RSA1024 CRT generate",value:0.697,title:"2038.8 ms"},
{axis:"RSA2048 CRT generate",value:0.818,title:"13945.6 ms"},
{axis:"RSA1024 decrypt",value:0.519,title:"230.24 ms"},
{axis:"RSA1024 encrypt",value:0.825,title:"7.02 ms"},
{axis:"RSA2048 decrypt",value:0.492,title:"1651.24 ms"},
{axis:"RSA2048 encrypt",value:0.840,title:"13.18 ms"},
{axis:"RSA1024 generate",value:0.728,title:"1817.8 ms"},
{axis:"RSA2048 generate",value:0.723,title:"13577.3 ms"},
],
];

var config = { w: w-175,
 h: h-175,
 maxValue: 1.0,
 levels: 10,
 }

RadarChart.draw("#chart", data, config);