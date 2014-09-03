JFLAGS = -g 
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	algtestjclient/CardMngr.java \
	algtestjclient/AlgTestJClient.java 

default: classes 

classes: $(CLASSES:.java=.class) 

pack:
	jar cfm algtestjclient.jar Manifest.txt algtestjclient/*.class 

clean:
	$(RM) algtestjclient/*.class
	$(RM) algtestjclient.jar