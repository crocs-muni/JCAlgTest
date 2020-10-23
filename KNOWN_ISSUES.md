# Known issues with testing 
The collection of well known issues which may hard your card, limit its functionality or outright brick it

### Issues sorted by cards 
  * ACS ACOSJ 40K 
    * will fail to upload additional applet after many (FIXME) unsuccesfull uploads (FIXME example from jcaidscan package scanning)
    * will fail to upload additional applet if applet upload is interrupted
  * NXP J3D081 
    * the eeprom memory is not properly freed after upload of certain (FIXME) cap file, potentially rendering card almost useless (very low amount of available EEPROM)
  * NXP JCOP3 J3H145G
    * setting and using incorrect private exponent for RSA may brick the whole card (FIXME example from JCMathLib)
    
  
