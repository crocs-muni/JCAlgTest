var w = document.getElementById('chart').offsetWidth,
    h = window.innerHeight -70;
var colorscale = d3.scale.category10();
var data = [
[
{axis:"SECURE RANDOM (256B)",value:0.966,title:"12.18 ms"},
{axis:"SHA-1 hash (256B)",value:0.934,title:"4.54 ms"},
{axis:"SHA2-256 hash (256B)",value:0.687,title:"39.76 ms"},
{axis:"AES128 encrypt (256B)",value:0.965,title:"3.61 ms"},
{axis:"AES256 encrypt (256B)",value:0.969,title:"4.07 ms"},
{axis:"3DES encrypt (256B)",value:0.976,title:"4.9 ms"},
{axis:"3DES setKey(192b)",value:0.983,title:"0.83 ms"},
{axis:"AES setKey(128b)",value:0.983,title:"0.82 ms"},
{axis:"AES setKey(256b)",value:0.982,title:"0.9 ms"},
{axis:"SWAES oneblock (16B)",value:0.0,title:"NS"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.0,title:"NS"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.0,title:"NS"},
{axis:"ECC 256b genKeyPair",value:0.0,title:"NS"},
{axis:"ECDSA 256b sign",value:0.0,title:"NS"},
{axis:"ECDSA 256b verify",value:0.0,title:"NS"},
{axis:"ECDH 256b",value:0.0,title:"NS"},
{axis:"RSA1024 CRT decrypt",value:0.933,title:"29.44 ms"},
{axis:"RSA1024 CRT encrypt",value:0.897,title:"4.12 ms"},
{axis:"RSA2048 CRT decrypt",value:0.911,title:"154.49 ms"},
{axis:"RSA2048 CRT encrypt",value:0.892,title:"8.88 ms"},
{axis:"RSA1024 decrypt",value:0.844,title:"74.85 ms"},
{axis:"RSA1024 encrypt",value:0.897,title:"4.12 ms"},
{axis:"RSA2048 decrypt",value:0.815,title:"600.26 ms"},
{axis:"RSA2048 encrypt",value:0.892,title:"8.87 ms"},
],
[
{axis:"SECURE RANDOM (256B)",value:0.980,title:"7.25 ms"},
{axis:"SHA-1 hash (256B)",value:0.942,title:"4.0 ms"},
{axis:"SHA2-256 hash (256B)",value:0.784,title:"27.37 ms"},
{axis:"AES128 encrypt (256B)",value:0.968,title:"3.26 ms"},
{axis:"AES256 encrypt (256B)",value:0.972,title:"3.7 ms"},
{axis:"3DES encrypt (256B)",value:0.978,title:"4.5 ms"},
{axis:"3DES setKey(192b)",value:0.988,title:"0.62 ms"},
{axis:"AES setKey(128b)",value:0.986,title:"0.69 ms"},
{axis:"AES setKey(256b)",value:0.986,title:"0.68 ms"},
{axis:"SWAES oneblock (16B)",value:0.862,title:"328.54 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.944,title:"0.36 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.982,title:"0.75 ms"},
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