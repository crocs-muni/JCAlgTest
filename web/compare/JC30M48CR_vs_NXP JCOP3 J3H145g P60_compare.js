var w = document.getElementById('chart').offsetWidth,
    h = window.innerHeight -70;
var colorscale = d3.scale.category10();
var data = [
[
{axis:"SECURE RANDOM (256B)",value:0.970,title:"10.92 ms"},
{axis:"SHA-1 hash (256B)",value:0.843,title:"10.84 ms"},
{axis:"SHA2-256 hash (256B)",value:0.787,title:"27.0 ms"},
{axis:"AES128 encrypt (256B)",value:0.969,title:"3.18 ms"},
{axis:"AES256 encrypt (256B)",value:0.972,title:"3.6 ms"},
{axis:"3DES encrypt (256B)",value:0.978,title:"4.42 ms"},
{axis:"3DES setKey(192b)",value:0.783,title:"10.79 ms"},
{axis:"AES setKey(128b)",value:0.989,title:"0.56 ms"},
{axis:"AES setKey(256b)",value:0.988,title:"0.58 ms"},
{axis:"SWAES oneblock (16B)",value:0.905,title:"226.06 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.947,title:"0.34 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.981,title:"0.78 ms"},
{axis:"ECC 256b genKeyPair",value:0.0,title:"NS"},
{axis:"ECDSA 256b sign",value:0.0,title:"NS"},
{axis:"ECDSA 256b verify",value:0.0,title:"NS"},
{axis:"ECDH 256b",value:0.0,title:"NS"},
{axis:"RSA1024 CRT decrypt",value:0.933,title:"29.33 ms"},
{axis:"RSA1024 CRT encrypt",value:0.908,title:"3.68 ms"},
{axis:"RSA2048 CRT decrypt",value:0.911,title:"155.55 ms"},
{axis:"RSA2048 CRT encrypt",value:0.897,title:"8.49 ms"},
{axis:"RSA1024 decrypt",value:0.843,title:"75.08 ms"},
{axis:"RSA1024 encrypt",value:0.909,title:"3.66 ms"},
{axis:"RSA2048 decrypt",value:0.821,title:"581.88 ms"},
{axis:"RSA2048 encrypt",value:0.897,title:"8.43 ms"},
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