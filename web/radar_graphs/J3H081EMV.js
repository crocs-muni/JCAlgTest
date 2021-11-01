var w = document.getElementById('chart').offsetWidth,
    h = window.innerHeight -70;
var colorscale = d3.scale.category10();
var data = [
[
{axis:"SECURE RANDOM (256B)",value:0.994,title:"2.1 ms"},
{axis:"SHA-1 hash (256B)",value:0.904,title:"6.66 ms"},
{axis:"SHA2-256 hash (256B)",value:0.946,title:"6.83 ms"},
{axis:"AES128 encrypt (256B)",value:0.906,title:"9.58 ms"},
{axis:"AES256 encrypt (256B)",value:0.921,title:"10.23 ms"},
{axis:"3DES encrypt (256B)",value:0.954,title:"9.39 ms"},
{axis:"3DES setKey(192b)",value:0.455,title:"27.05 ms"},
{axis:"AES setKey(128b)",value:0.507,title:"24.13 ms"},
{axis:"AES setKey(256b)",value:0.454,title:"27.05 ms"},
{axis:"SWAES oneblock (16B)",value:0.890,title:"261.3 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.950,title:"0.32 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.986,title:"0.6 ms"},
{axis:"ECC 256b genKeyPair",value:0.0,title:"NS"},
{axis:"ECDSA 256b sign",value:0.0,title:"NS"},
{axis:"ECDSA 256b verify",value:0.0,title:"NS"},
{axis:"ECDH 256b",value:0.0,title:"NS"},
{axis:"RSA2048 CRT decrypt",value:0.0,title:"NS"},
{axis:"RSA2048 CRT encrypt",value:0.0,title:"NS"},
{axis:"RSA2048 CRT generate",value:0.0,title:"NS"},
{axis:"RSA2048 decrypt",value:0.0,title:"NS"},
{axis:"RSA2048 encrypt",value:0.0,title:"NS"},
{axis:"RSA2048 generate",value:0.0,title:"NS"},
{axis:"RSA1024 CRT generate",value:0.0,title:"NS"},
{axis:"RSA1024 CRT decrypt",value:0.0,title:"NS"},
{axis:"RSA1024 CRT encrypt",value:0.0,title:"NS"},
{axis:"RSA1024 generate",value:0.0,title:"NS"},
{axis:"RSA1024 decrypt",value:0.0,title:"NS"},
{axis:"RSA1024 encrypt",value:0.0,title:"NS"},
],
];

var config = { w: w-175,
 h: h-175,
 maxValue: 1.0,
 levels: 10,
 }

RadarChart.draw("#chart", data, config);