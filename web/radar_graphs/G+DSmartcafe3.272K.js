var w = document.getElementById('chart').offsetWidth,
    h = window.innerHeight -70;
var colorscale = d3.scale.category10();
var data = [
[
{axis:"SECURE RANDOM (256B)",value:0.884,title:"41.4 ms"},
{axis:"SHA-1 hash (256B)",value:0.099,title:"62.4 ms"},
{axis:"SHA2-256 hash (256B)",value:0.099,title:"114.26 ms"},
{axis:"3DES encrypt (256B)",value:0.629,title:"59.13 ms"},
{axis:"AES128 encrypt (256B)",value:0.777,title:"16.9 ms"},
{axis:"AES256 encrypt (256B)",value:0.814,title:"17.1 ms"},
{axis:"3DES setKey(192b)",value:0.560,title:"15.3 ms"},
{axis:"AES setKey(128b)",value:0.554,title:"11.62 ms"},
{axis:"AES setKey(256b)",value:0.424,title:"16.2 ms"},
{axis:"SWAES oneblock (16B)",value:0.718,title:"669.64 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.826,title:"1.11 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.904,title:"4.04 ms"},
{axis:"RSA1024 CRT decrypt",value:0.585,title:"182.74 ms"},
{axis:"RSA1024 CRT encrypt",value:0.525,title:"19.02 ms"},
{axis:"RSA2048 CRT decrypt",value:0.520,title:"836.66 ms"},
{axis:"RSA2048 CRT encrypt",value:0.653,title:"28.53 ms"},
{axis:"RSA1024 CRT generate",value:0.427,title:"3854.1 ms"},
{axis:"RSA2048 CRT generate",value:0.766,title:"17930.7 ms"},
{axis:"RSA1024 decrypt",value:0.145,title:"409.75 ms"},
{axis:"RSA1024 encrypt",value:0.527,title:"18.93 ms"},
{axis:"RSA2048 decrypt",value:0.181,title:"2659.69 ms"},
{axis:"RSA2048 encrypt",value:0.651,title:"28.68 ms"},
{axis:"RSA1024 generate",value:0.516,title:"3230.7 ms"},
{axis:"RSA2048 generate",value:0.704,title:"14507.7 ms"},
],
];

var config = { w: w-175,
 h: h-175,
 maxValue: 1.0,
 levels: 10,
 }

RadarChart.draw("#chart", data, config);