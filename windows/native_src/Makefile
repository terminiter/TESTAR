LIBS= ole32.lib user32.lib comsuppw.lib oleaut32.lib comctl32.lib gdi32.lib Msimg32.lib GDIplus.lib kernel32.lib psapi.lib

INCLUDES= /I "$(JAVA_HOME)\include"\
		  /I "$(JAVA_HOME)\include\win32"\
          /I "C:\Program Files\Microsoft SDKs\Windows\v7.1\Include"
          
DLLFILE="..\bin\org\fruit\alayer\windows\windows.dll"

JAVA_CLASSFILE = "..\bin\org\fruit\alayer\windows\Windows.class"

JAVA_CLASS = org.fruit.alayer.windows.Windows

OBJFILES= main.obj\

HPPFILES= windows.h\

CPPFLAGS= /EHsc /O2 /MD


all: windows.h $(DLLFILE)

windows.h: $(JAVA_CLASSFILE)
	javah -jni -force -o windows.h -classpath ..\bin $(JAVA_CLASS)

$(OBJFILES): windows.h

.cpp.obj:
	cl $(CPPFLAGS) /c $*.cpp $(INCLUDES)

$(DLLFILE): $(OBJFILES)
	cl $(OBJFILES) /link $(LIBS) /DLL /OUT:$(DLLFILE)

clean:
	del windows.h *.obj *.lib *.exp