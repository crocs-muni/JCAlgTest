var w = document.getElementById('chart').offsetWidth,
    h = window.innerHeight -70;
var colorscale = d3.scale.category10();
var data = [
[
{axis:"SECURE RANDOM (256B)",value:0.970,title:"10.78 ms"},
{axis:"SHA-1 hash (256B)",value:0.943,title:"3.96 ms"},
{axis:"SHA2-256 hash (256B)",value:0.723,title:"35.13 ms"},
{axis:"AES128 encrypt (256B)",value:0.969,title:"3.18 ms"},
{axis:"AES256 encrypt (256B)",value:0.972,title:"3.63 ms"},
{axis:"3DES encrypt (256B)",value:0.980,title:"4.13 ms"},
{axis:"3DES setKey(192b)",value:0.988,title:"0.58 ms"},
{axis:"AES setKey(128b)",value:0.988,title:"0.57 ms"},
{axis:"AES setKey(256b)",value:0.987,title:"0.64 ms"},
{axis:"SWAES oneblock (16B)",value:0.0,title:"NS"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.0,title:"NS"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.0,title:"NS"},
{axis:"ECC 256b genKeyPair",value:0.0,title:"NS"},
{axis:"ECDSA 256b sign",value:0.0,title:"NS"},
{axis:"ECDSA 256b verify",value:0.0,title:"NS"},
{axis:"ECDH 256b",value:0.0,title:"NS"},
{axis:"RSA1024 CRT decrypt",value:0.943,title:"25.04 ms"},
{axis:"RSA1024 CRT encrypt",value:0.916,title:"3.38 ms"},
{axis:"RSA2048 CRT decrypt",value:0.916,title:"147.04 ms"},
{axis:"RSA2048 CRT encrypt",value:0.903,title:"7.98 ms"},
{axis:"RSA1024 decrypt",value:0.850,title:"71.65 ms"},
{axis:"RSA1024 encrypt",value:0.916,title:"3.37 ms"},
{axis:"RSA2048 decrypt",value:0.823,title:"573.78 ms"},
{axis:"RSA2048 encrypt",value:0.903,title:"7.98 ms"},
],
[
{axis:"SECURE RANDOM (256B)",value:0.996,title:"1.49 ms"},
{axis:"SHA-1 hash (256B)",value:0.882,title:"8.14 ms"},
{axis:"SHA2-256 hash (256B)",value:0.930,title:"8.86 ms"},
{axis:"AES128 encrypt (256B)",value:0.911,title:"9.03 ms"},
{axis:"AES256 encrypt (256B)",value:0.928,title:"9.35 ms"},
{axis:"3DES encrypt (256B)",value:0.958,title:"8.56 ms"},
{axis:"3DES setKey(192b)",value:0.0,title:"NS"},
{axis:"AES setKey(128b)",value:0.0,title:"NS"},
{axis:"AES setKey(256b)",value:0.0,title:"NS"},
{axis:"SWAES oneblock (16B)",value:0.0,title:"NS"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.0,title:"NS"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.0,title:"NS"},
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