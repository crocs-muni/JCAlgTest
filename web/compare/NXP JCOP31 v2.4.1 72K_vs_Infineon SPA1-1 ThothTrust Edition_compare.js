var w = document.getElementById('chart').offsetWidth,
    h = window.innerHeight -70;
var colorscale = d3.scale.category10();
var data = [
[
{axis:"SECURE RANDOM (256B)",value:0.941,title:"20.97 ms"},
{axis:"SHA-1 hash (256B)",value:0.508,title:"34.1 ms"},
{axis:"SHA2-256 hash (256B)",value:0.479,title:"66.02 ms"},
{axis:"AES128 encrypt (256B)",value:0.800,title:"20.44 ms"},
{axis:"AES256 encrypt (256B)",value:0.829,title:"22.24 ms"},
{axis:"3DES encrypt (256B)",value:0.903,title:"19.95 ms"},
{axis:"3DES setKey(192b)",value:0.865,title:"6.7 ms"},
{axis:"AES setKey(128b)",value:0.870,title:"6.38 ms"},
{axis:"AES setKey(256b)",value:0.858,title:"7.04 ms"},
{axis:"SWAES oneblock (16B)",value:0.798,title:"479.83 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.875,title:"0.8 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.918,title:"3.44 ms"},
{axis:"ECC 256b genKeyPair",value:0.0,title:"NS"},
{axis:"ECDSA 256b sign",value:0.0,title:"NS"},
{axis:"ECDSA 256b verify",value:0.0,title:"NS"},
{axis:"ECDH 256b",value:0.0,title:"NS"},
{axis:"RSA1024 CRT decrypt",value:0.715,title:"125.48 ms"},
{axis:"RSA1024 CRT encrypt",value:0.676,title:"12.98 ms"},
{axis:"RSA2048 CRT decrypt",value:0.631,title:"642.86 ms"},
{axis:"RSA2048 CRT encrypt",value:0.622,title:"31.12 ms"},
{axis:"RSA1024 decrypt",value:0.0,title:"NS"},
{axis:"RSA1024 encrypt",value:0.0,title:"NS"},
{axis:"RSA2048 decrypt",value:0.0,title:"NS"},
{axis:"RSA2048 encrypt",value:0.0,title:"NS"},
],
[
{axis:"SECURE RANDOM (256B)",value:0.981,title:"6.91 ms"},
{axis:"SHA-1 hash (256B)",value:0.843,title:"10.84 ms"},
{axis:"SHA2-256 hash (256B)",value:0.881,title:"15.05 ms"},
{axis:"AES128 encrypt (256B)",value:0.989,title:"1.14 ms"},
{axis:"AES256 encrypt (256B)",value:0.995,title:"0.66 ms"},
{axis:"3DES encrypt (256B)",value:0.996,title:"0.79 ms"},
{axis:"3DES setKey(192b)",value:0.545,title:"22.58 ms"},
{axis:"AES setKey(128b)",value:0.557,title:"21.68 ms"},
{axis:"AES setKey(256b)",value:0.526,title:"23.45 ms"},
{axis:"SWAES oneblock (16B)",value:0.878,title:"289.41 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.966,title:"0.22 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.993,title:"0.31 ms"},
{axis:"ECC 256b genKeyPair",value:1.650,title:"-412.0 ms"},
{axis:"ECDSA 256b sign",value:0.752,title:"39.19 ms"},
{axis:"ECDSA 256b verify",value:0.827,title:"40.35 ms"},
{axis:"ECDH 256b",value:1.065,title:"-13.44 ms"},
{axis:"RSA1024 CRT decrypt",value:0.939,title:"26.96 ms"},
{axis:"RSA1024 CRT encrypt",value:1.362,title:"-14.49 ms"},
{axis:"RSA2048 CRT decrypt",value:0.963,title:"64.82 ms"},
{axis:"RSA2048 CRT encrypt",value:1.161,title:"-13.22 ms"},
{axis:"RSA1024 decrypt",value:0.921,title:"37.62 ms"},
{axis:"RSA1024 encrypt",value:1.184,title:"-7.37 ms"},
{axis:"RSA2048 decrypt",value:0.933,title:"217.29 ms"},
{axis:"RSA2048 encrypt",value:1.074,title:"-6.1 ms"},
],
];

var config = { w: w-175,
 h: h-175,
 maxValue: 1.0,
 levels: 10,
 }

RadarChart.draw("#chart", data, config);