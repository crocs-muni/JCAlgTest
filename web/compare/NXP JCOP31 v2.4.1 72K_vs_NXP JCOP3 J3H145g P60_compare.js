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
{axis:"SECURE RANDOM (256B)",value:0.996,title:"1.45 ms"},
{axis:"SHA-1 hash (256B)",value:0.886,title:"7.93 ms"},
{axis:"SHA2-256 hash (256B)",value:0.932,title:"8.63 ms"},
{axis:"AES128 encrypt (256B)",value:0.912,title:"9.01 ms"},
{axis:"AES256 encrypt (256B)",value:0.928,title:"9.33 ms"},
{axis:"3DES encrypt (256B)",value:0.958,title:"8.54 ms"},
{axis:"3DES setKey(192b)",value:0.834,title:"8.25 ms"},
{axis:"AES setKey(128b)",value:0.839,title:"7.89 ms"},
{axis:"AES setKey(256b)",value:0.831,title:"8.38 ms"},
{axis:"SWAES oneblock (16B)",value:0.870,title:"309.07 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.928,title:"0.46 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.961,title:"1.63 ms"},
{axis:"ECC 256b genKeyPair",value:0.753,title:"156.3 ms"},
{axis:"ECDSA 256b sign",value:0.643,title:"56.29 ms"},
{axis:"ECDSA 256b verify",value:0.785,title:"50.32 ms"},
{axis:"ECDH 256b",value:0.735,title:"55.08 ms"},
{axis:"RSA1024 CRT decrypt",value:0.870,title:"57.25 ms"},
{axis:"RSA1024 CRT encrypt",value:0.854,title:"5.87 ms"},
{axis:"RSA2048 CRT decrypt",value:0.0,title:"NS"},
{axis:"RSA2048 CRT encrypt",value:0.0,title:"NS"},
{axis:"RSA1024 decrypt",value:0.811,title:"90.63 ms"},
{axis:"RSA1024 encrypt",value:0.854,title:"5.86 ms"},
{axis:"RSA2048 decrypt",value:0.842,title:"514.0 ms"},
{axis:"RSA2048 encrypt",value:0.864,title:"11.18 ms"},
],
];

var config = { w: w-175,
 h: h-175,
 maxValue: 1.0,
 levels: 10,
 }

RadarChart.draw("#chart", data, config);