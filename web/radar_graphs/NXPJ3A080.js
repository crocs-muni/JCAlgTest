var w = document.getElementById('chart').offsetWidth,
    h = window.innerHeight -70;
var colorscale = d3.scale.category10();
var data = [
[
{axis:"SECURE RANDOM (256B)",value:0.940,title:"21.64 ms"},
{axis:"SHA-1 hash (256B)",value:0.483,title:"35.78 ms"},
{axis:"SHA2-256 hash (256B)",value:0.453,title:"69.32 ms"},
{axis:"3DES encrypt (256B)",value:0.869,title:"20.92 ms"},
{axis:"AES128 encrypt (256B)",value:0.718,title:"21.41 ms"},
{axis:"AES256 encrypt (256B)",value:0.747,title:"23.2 ms"},
{axis:"3DES setKey(192b)",value:0.555,title:"15.48 ms"},
{axis:"AES setKey(128b)",value:0.529,title:"12.28 ms"},
{axis:"AES setKey(256b)",value:0.435,title:"15.88 ms"},
{axis:"SWAES oneblock (16B)",value:0.786,title:"507.6 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.868,title:"0.84 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.914,title:"3.62 ms"},
{axis:"RSA1024 CRT decrypt",value:0.712,title:"126.93 ms"},
{axis:"RSA1024 CRT encrypt",value:0.666,title:"13.37 ms"},
{axis:"RSA2048 CRT decrypt",value:0.629,title:"645.58 ms"},
{axis:"RSA2048 CRT encrypt",value:0.617,title:"31.5 ms"},
{axis:"RSA1024 CRT generate",value:0.577,title:"2843.8 ms"},
{axis:"RSA2048 CRT generate",value:0.627,title:"28592.1 ms"},
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