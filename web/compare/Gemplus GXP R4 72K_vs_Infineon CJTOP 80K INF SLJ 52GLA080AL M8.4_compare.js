var w = document.getElementById('chart').offsetWidth,
    h = window.innerHeight -70;
var colorscale = d3.scale.category10();
var data = [
[
{axis:"SECURE RANDOM (256B)",value:0.993,title:"2.45 ms"},
{axis:"SHA-1 hash (256B)",value:0.947,title:"3.69 ms"},
{axis:"SHA2-256 hash (256B)",value:0.0,title:"NS"},
{axis:"AES128 encrypt (256B)",value:0.745,title:"26.05 ms"},
{axis:"AES256 encrypt (256B)",value:0.757,title:"31.52 ms"},
{axis:"3DES encrypt (256B)",value:0.738,title:"53.71 ms"},
{axis:"3DES setKey(192b)",value:0.811,title:"9.4 ms"},
{axis:"AES setKey(128b)",value:0.810,title:"9.28 ms"},
{axis:"AES setKey(256b)",value:0.807,title:"9.54 ms"},
{axis:"SWAES oneblock (16B)",value:0.953,title:"112.38 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.928,title:"0.46 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.779,title:"9.32 ms"},
{axis:"ECC 256b genKeyPair",value:0.0,title:"NS"},
{axis:"ECDSA 256b sign",value:0.0,title:"NS"},
{axis:"ECDSA 256b verify",value:0.0,title:"NS"},
{axis:"ECDH 256b",value:0.0,title:"NS"},
{axis:"RSA1024 CRT decrypt",value:0.099,title:"396.48 ms"},
{axis:"RSA1024 CRT encrypt",value:0.546,title:"18.21 ms"},
{axis:"RSA2048 CRT decrypt",value:0.099,title:"1569.15 ms"},
{axis:"RSA2048 CRT encrypt",value:0.460,title:"44.42 ms"},
{axis:"RSA1024 decrypt",value:0.163,title:"401.04 ms"},
{axis:"RSA1024 encrypt",value:0.547,title:"18.12 ms"},
{axis:"RSA2048 decrypt",value:0.632,title:"1196.79 ms"},
{axis:"RSA2048 encrypt",value:0.464,title:"44.07 ms"},
],
[
{axis:"SECURE RANDOM (256B)",value:0.931,title:"24.9 ms"},
{axis:"SHA-1 hash (256B)",value:0.748,title:"17.42 ms"},
{axis:"SHA2-256 hash (256B)",value:0.719,title:"35.58 ms"},
{axis:"AES128 encrypt (256B)",value:0.750,title:"25.53 ms"},
{axis:"AES256 encrypt (256B)",value:0.760,title:"31.18 ms"},
{axis:"3DES encrypt (256B)",value:0.700,title:"61.49 ms"},
{axis:"3DES setKey(192b)",value:0.867,title:"6.61 ms"},
{axis:"AES setKey(128b)",value:0.876,title:"6.08 ms"},
{axis:"AES setKey(256b)",value:0.866,title:"6.65 ms"},
{axis:"SWAES oneblock (16B)",value:0.429,title:"1356.0 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.666,title:"2.13 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.946,title:"2.27 ms"},
{axis:"ECC 256b genKeyPair",value:0.670,title:"208.9 ms"},
{axis:"ECDSA 256b sign",value:0.0,title:"NS"},
{axis:"ECDSA 256b verify",value:0.0,title:"NS"},
{axis:"ECDH 256b",value:0.0,title:"NS"},
{axis:"RSA1024 CRT decrypt",value:0.768,title:"101.92 ms"},
{axis:"RSA1024 CRT encrypt",value:0.724,title:"11.06 ms"},
{axis:"RSA2048 CRT decrypt",value:0.861,title:"242.91 ms"},
{axis:"RSA2048 CRT encrypt",value:0.791,title:"17.16 ms"},
{axis:"RSA1024 decrypt",value:0.750,title:"119.97 ms"},
{axis:"RSA1024 encrypt",value:0.725,title:"11.02 ms"},
{axis:"RSA2048 decrypt",value:0.788,title:"689.56 ms"},
{axis:"RSA2048 encrypt",value:0.792,title:"17.13 ms"},
],
];

var config = { w: w-175,
 h: h-175,
 maxValue: 1.0,
 levels: 10,
 }

RadarChart.draw("#chart", data, config);