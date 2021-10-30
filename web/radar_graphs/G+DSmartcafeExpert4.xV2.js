var w = document.getElementById('chart').offsetWidth,
    h = window.innerHeight -70;
var colorscale = d3.scale.category10();
var data = [
[
{axis:"SECURE RANDOM (256B)",value:0.099,title:"322.91 ms"},
{axis:"SHA-1 hash (256B)",value:0.514,title:"33.66 ms"},
{axis:"SHA2-256 hash (256B)",value:0.0,title:"NS"},
{axis:"AES128 encrypt (256B)",value:0.0,title:"NS"},
{axis:"AES256 encrypt (256B)",value:0.0,title:"NS"},
{axis:"3DES encrypt (256B)",value:0.818,title:"37.19 ms"},
{axis:"3DES setKey(192b)",value:0.928,title:"3.59 ms"},
{axis:"AES setKey(128b)",value:0.0,title:"NS"},
{axis:"AES setKey(256b)",value:0.0,title:"NS"},
{axis:"SWAES oneblock (16B)",value:0.816,title:"438.03 ms"},
{axis:" arrayCopy nonAtomic RAM2RAM (256B)",value:0.889,title:"0.71 ms"},
{axis:" arrayCopy nonAtomic EEPROM2EEPROM (256B)",value:0.952,title:"2.01 ms"},
{axis:"ECC 256b genKeyPair",value:0.0,title:"NS"},
{axis:"ECDSA 256b sign",value:0.0,title:"NS"},
{axis:"ECDSA 256b verify",value:0.0,title:"NS"},
{axis:"ECDH 256b",value:0.0,title:"NS"},
{axis:"RSA2048 CRT decrypt",value:0.760,title:"417.72 ms"},
{axis:"RSA2048 CRT encrypt",value:0.564,title:"35.83 ms"},
{axis:"RSA2048 CRT generate",value:0.816,title:"14076.7 ms"},
{axis:"RSA2048 decrypt",value:0.0,title:"NS"},
{axis:"RSA2048 encrypt",value:0.0,title:"NS"},
{axis:"RSA2048 generate",value:0.0,title:"NS"},
{axis:"RSA1024 CRT generate",value:0.671,title:"2210.1 ms"},
{axis:"RSA1024 CRT decrypt",value:0.711,title:"127.33 ms"},
{axis:"RSA1024 CRT encrypt",value:0.761,title:"9.59 ms"},
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