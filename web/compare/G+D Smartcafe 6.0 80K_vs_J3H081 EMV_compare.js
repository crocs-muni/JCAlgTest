var w = document.getElementById('chart').offsetWidth,
    h = window.innerHeight -70;
var colorscale = d3.scale.category10();
var data = [
[
{axis:"SECURE RANDOM (256B)",value:0.950,title:"18.03 ms"},
{axis:"SHA-1 hash (256B)",value:0.846,title:"10.67 ms"},
{axis:"SHA2-256 hash (256B)",value:0.692,title:"39.07 ms"},
{axis:"AES128 encrypt (256B)",value:0.787,title:"21.7 ms"},
{axis:"AES256 encrypt (256B)",value:0.827,title:"22.42 ms"},
{axis:"3DES encrypt (256B)",value:0.963,title:"7.67 ms"},
{axis:"3DES setKey(192b)",value:0.783,title:"10.8 ms"},
{axis:"AES setKey(128b)",value:0.853,title:"7.21 ms"},
{axis:"AES setKey(256b)",value:0.769,title:"11.46 ms"},
{axis:"SWAES oneblock (16B)",value:0.0,title:"NS"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.0,title:"NS"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.0,title:"NS"},
{axis:"ECC 256b genKeyPair",value:0.0,title:"NS"},
{axis:"ECDSA 256b sign",value:0.0,title:"NS"},
{axis:"ECDSA 256b verify",value:0.0,title:"NS"},
{axis:"ECDH 256b",value:0.0,title:"NS"},
{axis:"RSA1024 CRT decrypt",value:0.842,title:"69.55 ms"},
{axis:"RSA1024 CRT encrypt",value:0.824,title:"7.04 ms"},
{axis:"RSA2048 CRT decrypt",value:0.731,title:"469.21 ms"},
{axis:"RSA2048 CRT encrypt",value:0.841,title:"13.08 ms"},
{axis:"RSA1024 decrypt",value:0.519,title:"230.24 ms"},
{axis:"RSA1024 encrypt",value:0.825,title:"7.02 ms"},
{axis:"RSA2048 decrypt",value:0.492,title:"1651.24 ms"},
{axis:"RSA2048 encrypt",value:0.840,title:"13.18 ms"},
],
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
{axis:"RSA1024 CRT decrypt",value:0.0,title:"NS"},
{axis:"RSA1024 CRT encrypt",value:0.0,title:"NS"},
{axis:"RSA2048 CRT decrypt",value:0.0,title:"NS"},
{axis:"RSA2048 CRT encrypt",value:0.0,title:"NS"},
{axis:"RSA1024 decrypt",value:0.0,title:"NS"},
{axis:"RSA1024 encrypt",value:0.0,title:"NS"},
{axis:"RSA2048 decrypt",value:0.0,title:"NS"},
{axis:"RSA2048 encrypt",value:0.0,title:"NS"},
],
];

var config = { w: w-175,
 h: h-175,
 maxValue: 1.0,
 levels: 10,
 }

RadarChart.draw("#chart", data, config);