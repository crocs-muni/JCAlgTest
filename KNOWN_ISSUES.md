# Known issues with testing 
The collection of well known issues which may harm your card, limit its functionality or outright brick it

IMPORTANT: Have you experienced any issues with cards you have? Please let us know (open issue or directly PR for this file)

### Issues sorted by cards 
  * ACS ACOSJ 40K 
    * will fail to upload additional applet after many (FIXME) unsuccesfull uploads (FIXME example from jcaidscan package scanning)
    * will fail to upload additional applet if applet upload is interrupted
  * NXP J3D081 
    * the eeprom memory is not properly freed after upload of certain (FIXME) cap file, potentially rendering card almost useless (very low amount of available EEPROM)
  * NXP JCOP3 J3H145G
    * setting and using incorrect private exponent for RSA may brick the whole card (FIXME example from JCMathLib)
  * Feitian A20, A40 and similar cards 
    * failing repeated performance tests in KeyBuilder part with SystemException.NO_TRANSIENT_SPACE
    * The issue is likely garbage collector not freeing unused objects properly even after JCSystem.requestObjectDeletion() is called
    
  
