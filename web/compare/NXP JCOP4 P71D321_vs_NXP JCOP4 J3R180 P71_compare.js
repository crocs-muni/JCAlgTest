var w = document.getElementById('chart').offsetWidth,
    h = window.innerHeight -70;
var colorscale = d3.scale.category10();
var data = [
[
{axis:"SECURE RANDOM (256B)",value:0.996,title:"1.28 ms"},
{axis:"SHA-1 hash (256B)",value:0.917,title:"5.73 ms"},
{axis:"SHA2-256 hash (256B)",value:0.933,title:"8.54 ms"},
{axis:"AES128 encrypt (256B)",value:0.981,title:"1.95 ms"},
{axis:"AES256 encrypt (256B)",value:0.985,title:"2.01 ms"},
{axis:"3DES encrypt (256B)",value:0.990,title:"1.98 ms"},
{axis:"3DES setKey(192b)",value:0.929,title:"3.51 ms"},
{axis:"AES setKey(128b)",value:0.929,title:"3.48 ms"},
{axis:"AES setKey(256b)",value:0.929,title:"3.52 ms"},
{axis:"SWAES oneblock (16B)",value:0.927,title:"173.22 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.973,title:"0.17 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.861,title:"5.86 ms"},
{axis:"ECC 256b genKeyPair",value:0.959,title:"26.25 ms"},
{axis:"ECDSA 256b sign",value:0.790,title:"33.06 ms"},
{axis:"ECDSA 256b verify",value:0.866,title:"31.28 ms"},
{axis:"ECDH 256b",value:0.896,title:"21.58 ms"},
{axis:"RSA1024 CRT decrypt",value:0.963,title:"16.34 ms"},
{axis:"RSA1024 CRT encrypt",value:0.929,title:"2.83 ms"},
{axis:"RSA2048 CRT decrypt",value:0.969,title:"54.8 ms"},
{axis:"RSA2048 CRT encrypt",value:0.947,title:"4.33 ms"},
{axis:"RSA1024 decrypt",value:0.941,title:"28.1 ms"},
{axis:"RSA1024 encrypt",value:0.929,title:"2.84 ms"},
{axis:"RSA2048 decrypt",value:0.955,title:"146.53 ms"},
{axis:"RSA2048 encrypt",value:0.947,title:"4.32 ms"},
],
[
{axis:"SECURE RANDOM (256B)",value:0.997,title:"1.19 ms"},
{axis:"SHA-1 hash (256B)",value:0.923,title:"5.32 ms"},
{axis:"SHA2-256 hash (256B)",value:0.937,title:"7.94 ms"},
{axis:"AES128 encrypt (256B)",value:0.982,title:"1.83 ms"},
{axis:"AES256 encrypt (256B)",value:0.985,title:"1.92 ms"},
{axis:"3DES encrypt (256B)",value:0.991,title:"1.86 ms"},
{axis:"3DES setKey(192b)",value:0.935,title:"3.23 ms"},
{axis:"AES setKey(128b)",value:0.934,title:"3.21 ms"},
{axis:"AES setKey(256b)",value:0.935,title:"3.24 ms"},
{axis:"SWAES oneblock (16B)",value:0.927,title:"174.05 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.975,title:"0.16 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.933,title:"2.84 ms"},
{axis:"ECC 256b genKeyPair",value:0.961,title:"24.4 ms"},
{axis:"ECDSA 256b sign",value:0.797,title:"32.0 ms"},
{axis:"ECDSA 256b verify",value:0.871,title:"30.2 ms"},
{axis:"ECDH 256b",value:0.904,title:"20.0 ms"},
{axis:"RSA1024 CRT decrypt",value:0.966,title:"15.0 ms"},
{axis:"RSA1024 CRT encrypt",value:0.935,title:"2.61 ms"},
{axis:"RSA2048 CRT decrypt",value:0.969,title:"53.24 ms"},
{axis:"RSA2048 CRT encrypt",value:0.951,title:"4.0 ms"},
{axis:"RSA1024 decrypt",value:0.946,title:"25.95 ms"},
{axis:"RSA1024 encrypt",value:0.935,title:"2.61 ms"},
{axis:"RSA2048 decrypt",value:0.955,title:"147.78 ms"},
{axis:"RSA2048 encrypt",value:0.951,title:"3.99 ms"},
],
];

var config = { w: w-175,
 h: h-175,
 maxValue: 1.0,
 levels: 10,
 }

RadarChart.draw("#chart", data, config);