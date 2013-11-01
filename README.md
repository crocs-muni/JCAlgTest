AlgTest
=======

Automated testing tool for algorithms supported by particular smart card with JavaCard platform

Usage:

1. Download prepared version (AlgTest_JavaCard/AlgTest_***.cap) or compile your own modification of AlgTest applet. In case of use of provided *.cap file, use version that is supported by your card or simply start with version converted with highest version of converter (e.g., AlgTest_v1.1_jc2.2.2.cap) and then use lower version if card refuses to accept this file.    
2. If you are unable to upload the package to card or install it, then see Caveats, use lower version of converter, comment out unsupported classes or compile your own limited version of AlgTest applet.
3. Upload AlgTest package to your smart card and install it - Use uploader supplied by your card vendor (e.g., GPShell, Gemplus RADIII, IBM JCOP or Cyberflex Access Toolkit). Package AID: 6D 79 70 61 63 6B 61 67 31, Applet AID: 6D 79 70 61 63 30 30 30 31. No special installation parameters are given.
4. Run application AlgTestJClient.jar - Choose the target reader for card with uploaded AlgTest applet and let it run. CSV file with values separated by the semicolon is created (AlgTest_ATR.csv).
5. Please consider to send me (petr@svenda.com) your results in case your card is not yet in the database at http://www.fi.muni.cz/~xsvenda/jcsupport.html

See more details at http://www.fi.muni.cz/~xsvenda/jcsupport.html
