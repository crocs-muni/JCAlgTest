var w = document.getElementById('chart').offsetWidth,
    h = window.innerHeight -70;
var colorscale = d3.scale.category10();
var data = [
[
{axis:"SECURE RANDOM (256B)",value:0.965,title:"12.6 ms"},
{axis:"SHA-1 hash (256B)",value:0.933,title:"4.62 ms"},
{axis:"SHA2-256 hash (256B)",value:0.650,title:"44.36 ms"},
{axis:"3DES encrypt (256B)",value:0.920,title:"5.43 ms"},
{axis:"AES128 encrypt (256B)",value:0.851,title:"4.31 ms"},
{axis:"AES256 encrypt (256B)",value:0.862,title:"4.82 ms"},
{axis:"3DES setKey(192b)",value:0.971,title:"1.0 ms"},
{axis:"AES setKey(128b)",value:0.959,title:"1.06 ms"},
{axis:"AES setKey(256b)",value:0.962,title:"1.06 ms"},
{axis:"SWAES oneblock (16B)",value:0.787,title:"505.13 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.912,title:"0.56 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.968,title:"1.36 ms"},
{axis:"RSA1024 CRT decrypt",value:0.0,title:"NS"},
{axis:"RSA1024 CRT encrypt",value:0.0,title:"NS"},
{axis:"RSA2048 CRT decrypt",value:0.0,title:"NS"},
{axis:"RSA2048 CRT encrypt",value:0.0,title:"NS"},
{axis:"RSA1024 CRT generate",value:0.639,title:"2424.2 ms"},
{axis:"RSA2048 CRT generate",value:0.844,title:"11937.1 ms"},
{axis:"RSA1024 decrypt",value:0.0,title:"NS"},
{axis:"RSA1024 encrypt",value:0.0,title:"NS"},
{axis:"RSA2048 decrypt",value:0.0,title:"NS"},
{axis:"RSA2048 encrypt",value:0.0,title:"NS"},
{axis:"RSA1024 generate",value:0.759,title:"1605.2 ms"},
{axis:"RSA2048 generate",value:0.715,title:"13985.7 ms"},
],
];

var config = { w: w-175,
 h: h-175,
 maxValue: 1.0,
 levels: 10,
 }

RadarChart.draw("#chart", data, config);