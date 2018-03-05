AVR Programming on Mac

Huge thanks to Dennis Clark for guidance and setting me on this path in the first place!!!

Updated Feb 28, 2010 for Mac OS X 10.5 "Leopard" and OS X 10.6 "Snow Leopard"

Yes, you can program your AVR-based Orangutan with your Mac! I'm using a Pololu Orangutan LV-168 to control Pokey the robot and programming it with the Pololu USB programmer. Works great! I recently updated the AVR toolchain / programming environment on my Intel-based MacBook running OS X 10.5 "Leopard" and so I've updated this article. Here's the play by play.

Step 1.0: AVR Toolchain

The toolchain software needed to program your AVR includes the following:
avr-binutils
avr-gcc
avr-libc
avrdude
simulavr
I suggest obtaining the tools in the form of CrossPack for AVR from Objective Development. It will support the new Orangutan library (libpololu-avr) and the new ATmega328P-based controllers.  The latest version of CrossPack AVR as of this writing is 2010-01-15 and includes avr-gcc 4.3.3, avr-libc 1.6.7, avrdude 5.8, and simulavr 0.9. It's based on WinAVR 20100110. The old Mac OS X AVR is no longer available and MacPorts' avr toolchain is a couple years out of date.

The CrossPack AVR suite comes as a disk image with a Mac package installer.  It's a matter of opening the disk image then double-clicking the package and going through the prompts.  Tremendously simple, way more so than MacPorts ever was. Both avrdude and simulavr are also included. Neato!

Incidentally if you see the following error (compiling with the new Pololu library), it is most likely because you're running a version of avr-gcc that is too old (4.0.2 in my case):
main.o: In function `main':
main.c:(.text+0xa): undefined reference to `serial_set_mode'
main.c:(.text+0x16): undefined reference to `serial_set_baud_rate'
main.c:(.text+0x3c): undefined reference to `serial_send'
main.c:(.text+0x44): undefined reference to `delay_ms'
main.o: In function `serial_get_next_line':
main.c:(.text+0x50): undefined reference to `serial_get_received_bytes'
The new versions included in CrossPack AVR will make the above errors go bye-bye.

Step 1.1: Configuring Your Environment

CrossPack AVR installs in the directory /usr/local/CrossPack-AVR.You'll want to set up some environment variables to make sure everything works properly when you compile the libpololu-avr libraries.

First, edit your ~/.profile and add /usr/local/CrossPack-AVR/bin: to your PATH environment variable in front of /opt/local/bin or wherever your old avr-gcc was installed. Like this:
export PATH=/usr/local/CrossPack-AVR/bin:/opt/local/bin:/opt/local/sbin:/usr/local/bin:/usr/local/sbin:$PATH
While we're at it, let's set up the MANPATH too.
export MANPATH=/usr/local/CrossPack-AVR/man:/usr/local/man:/opt/local/man:/sw/man:/usr/share/man:$MANPATH
And, we'll want to set up the C include path.
export C_INCLUDE_PATH=/usr/local/CrossPack-AVR/include:$C_INCLUDE_PATH
We could try to set up the LIBRARY_PATH but the CrossPack AVR linker seems to ignore this environment variable.

Close your current shell and open a new shell window for these changes to take effect. 

Step 1.2 Enable ATmega328P Support

You can skip this step if you don't plan to program an ATmega328P MCU.  The version of avrdude that comes with CrossPack-AVR doesn't support the ATmega328P out of the box so you need to edit the config file and add a section defining it.  This link describes the process. Edit /usr/local/CrossPack-AVR/etc/avrdude.conf and add the text in this link at the end of the file.

Step 2.0: Pololu Software

You'll need to download the Orangutan USB programmer drivers as well as the new library for Orangutan controllers (libpololu-avr) from Pololu's website.
CP2102 drivers for Mac OS X (Orangutan USB Programmer driver)
libpololu-avr, the Pololu Orangutan Library
The drivers come as a .dmg disk image file.  Download the disk image and open it. After the disk image mounts, double-click the "Pololu Installer" icon and follow the directions from there. You'll need to restart your computer when the installation is done.

If you are using a different programmer, you'll have to follow the manufacturer's instructions for getting it ready to use for downloading code.

The Pololu library comes as a ZIP archive.  After you download and unzip libpololu-avr into some convenient directory, you'll need to edit its Makefile to specify where to install the includes and static libraries.  The section of the Makefile you're looking for talks about setting INCLUDE_POLOLU and LIB.  When you're done that section should look like this:
# You can override this behavior by inserting a line below that manually
# sets INCLUDE_POLOLU and LIB to a directory of your choice.
# For example, you could uncomment these lines:
#   LIB := /usr/lib/avr/lib
#   INCLUDE_POLOLU := /usr/lib/avr/include
LIB := /usr/local/CrossPack-AVR/lib
INCLUDE_POLOLU := /usr/local/CrossPack-AVR/include/pololu
Optionally, you can compile the Pololu library from scratch using the command  make clean to wipe everything and then make to recompile using CrossPack AVR's avr-gcc 4.3.3.

Next, install the libraries with the command sudo make install. Enter your password when prompted.

The compilation succeed with no errors.  (Note, version 4.0.2 gave me numerous errors and warnings compiling these libraries as mentioned above).

Step 3.0 Configuring AVR Makefiles

Let's try compiling one of the Pololu examples that come with libpololu-avr. Before we can do that, we have to edit the Makefile.  You'll need to do this with any Makefile you create.

First, we need to fix the linker's search path. As mentioned the linker ignores the LIBRARY_PATH environment variable, so edit the Makefile to add the CrossPack AVR directory to the library search path. Set (or add to) the LIBDIRS variable, which is used to list -L switches which add the specified path to the linker's search path.
LIBDIRS = -L/usr/local/CrossPack-AVR/lib
To include the MCU-specific library, you'd use, for example, -lpololu_atmega168 (or whatever MCU you have) when linking objects together.  Set (or add to) the LIBS variable: 
LIBS = -lpololu_$(MCU)
Find (or add) the MCU assignment to specify the device (atmega168, atmega328P, etc)
MCU = atmega168
Instead, if there's a DEVICE specified, make that atmega168 (etc).  The example I looked at uses an AVRDUDE_DEVICE set to m168.  Note that the CrossPack-AVR avrdude will recognize the device atmega168 so you can simplify your project makefiles by using a single variable (like MCU) for both the avr-gcc and avrdude command lines.

Now that you have all the preliminaries set up, time to ensure that the linking command includes the LIBS and LIBDIRS variables. Add $(LIBDIRS) and $(LIBS) to your link command in the correct order, LIBDIRS first, LIBS second, appearing after the list of objects, like this:
##Link
$(TARGET): $(OBJECTS)
         $(CC) $(LDFLAGS) $(OBJECTS) $(LINKONLYOBJECTS) $(LIBDIRS) $(LIBS) -o $(TARGET)
The order of command line flags is really important.  You must first list your object file, then list the library directory, then list your library, as above. If you list the library first and the search path second you'll probably see an error like this:

/usr/local/CrossPack-AVR-20100115/lib/gcc/avr/4.3.3/../../../../avr/bin/ld: cannot find -lpololu_atmega168
Make sure the avrdude command is correct (we'll revisit this momentarily), something like this:

    avrdude -q -p $(MCU) -P $(SERIAL) -c avrispv2 -e -U flash:w:$(PROJECT).hex

or like this:
$(AVRDUDE) -p $(AVRDUDE_DEVICE) -c avrisp2 -P $(PORT) -U flash:w:$(TARGET).hex
The variable SERIAL or PORT should be defined in the Makefile as /dev/tty.Pololu.

Also, be sure your Makefiles are using the correct avr-gcc.  Just specify avr-gcc as the compiler with no path:
CC=avr-gcc
Then make sure that the CrossPack AVR version of avr-gcc is in your path via the command which avr-gcc and if not, did you set up the path variable correctly from Step 1.1?  Did you open a new shell window?

One other thing. If you compile and link the static pololu library but you aren't using all the functions, then you're adding a lot of unnecessary code onto your AVR that is doing nothing but taking up space. To tell the linker to prune out everything except what's actually used, add the option -Wl,-gc-sections to your LDFLAGS like so:

LDFLAGS+=-Wl,-gc-sections
Here's what I got when I did all of the above then compiled the libpololu-avr's lcd1 test program:

$ pwd
/Users/mes/Documents/Robot/src/libpololu-avr/examples/atmega168/lcd1
$ ls
Makefile    lcd1.aps    test.c
$ make
avr-gcc -g -Wall -mcall-prologues -mmcu=atmega168 -Os   -c -o test.o test.c
avr-gcc -g -Wall -mcall-prologues -mmcu=atmega168 -Os test.o -Wl,-gc-sections -L/usr/local/CrossPack-AVR/lib -lpololu_atmega168 -Wl,-relax -o test.obj
avr-objcopy  -R .eeprom -O ihex test.obj test.hex
rm test.obj
Step 4.0: Programming the Orangutan AVR

You're ready to download some code to your Orangutan! Here's Pololu's user guide entry on using avrdude with their USB serial programmer (on Windows): http://www.pololu.com/docs/0J6?view_all=1#5.d

The correct command should already be set up in the Makefile for the Pololu library examples and the demo program. You should be able to simply issue the command make program. For what it's worth, the avrdude command should look something like this:

avrdude -p atmega168 -P /dev/tty.Pololu -c avrispv2 -e -U flash:w:test.hex

The argument following the -p is the part number and should be atmega168 (or m168), atmega48 (m48), etc.  Note that this version of avrdude doesn't support the atmega328P out of the box, see Step 1.2 after which you can specify atmega328p (m328p) as a part. The argument following the -P is the serial port; On Mac OS X, do an ls of /dev/tty* and you will see the Pololu serial device driver of your Orangutan USB programmer (it should be /dev/tty.Pololu). The programmer ID is specified using the -c option and should be avrispv2. The -e option results in a chip erase and the -U option is used for writing, reading, or verifying flash, EEPROM, fuses, or lock bits. In this example we are using -U to write test.hex to flash.

Running make program with the programmer plugged in but without plugging it into the Orangutan, or leaving the Orangutan off, you will see flashy red/green lights as the software attempts to find the AVR, then:
$ make program
avrdude -q -p atmega168 -P /dev/tty.Pololu -c avrispv2 -e -U flash:w:servo.hex

avrdude: stk500v2_command(): command failed
avrdude: stk500v2_command(): unknown status 0xc9
avrdude: stk500v2_program_enable(): cannot get connection status
avrdude: initialization failed, rc=-1
         Double check connections and try again, or use -F to override
         this check.


avrdude done.  Thank you.

make: *** [program] Error 1
If you plug in and turn on the Orangutan, the program should be written into the MCU's flash memory. Note that the avrdude command includes the -q option which suppresses the progress indicator hashmarks.  Use this switch when using Emacs.
$ make program
avrdude -q -p atmega168 -P /dev/tty.Pololu -c avrispv2 -e -U flash:w:servo.hex

avrdude: AVR device initialized and ready to accept instructions
avrdude: Device signature = 0x1e9406
avrdude: erasing chip
avrdude: reading input file "servo.hex"
avrdude: input file servo.hex auto detected as Intel Hex
avrdude: writing flash (7944 bytes):
avrdude: 7944 bytes of flash written
avrdude: verifying flash memory against servo.hex:
avrdude: load data flash data from input file servo.hex:
avrdude: input file servo.hex auto detected as Intel Hex
avrdude: input file servo.hex contains 7944 bytes
avrdude: reading on-chip flash data:
avrdude: verifying ...
avrdude: 7944 bytes of flash verified

avrdude: safemode: Fuses OK

avrdude done.  Thank you.


Step 5.0: Integrated Development Environment

Now that you can compile code and download compiled binaries, you need to be able to write code so you need an editor or IDE. But... which editor to choose?

Emacs
I have been an Emacs geek for 20 years (yikes, I'm old!) so needless to say it is my Integrated Development Environment (IDE) of choice. But it is all text-based. Yes, it is an IDE; you can make / compile code, step through errors, run software, and do anything else you can think of (since it is programmable), all within the editor. It is fast and very lightweight (thanks to Moore's Law).

It's old school, but if you learn it well it's quick to use (hey kids, you may not remember the days before menus and mouses, but there are some things that are much faster using keyboard shortcuts). Here's a reference card with emacs key combinations: http://www.geek-girl.com/emacs/refcard.html. 

Fortunately, you'll find emacs on your Mac but working with many files at once is challenging. Instead, may I suggest you try my preference, Aquamacs, a try: It's Emacs with an Aqua interface. It's quite decent and it puts each file (module, header, etc.) in a separate OS X window. I found this tool really improved my productivity -- and protected my sanity. 

Configuring Emacs is pretty simple. To make it as useful as possible, I set up the ability to compile programs within Emacs, as well as stepping through compile errors, by editing / creating the .emacs preferences file to include the following key bindings:

(global-set-key "\C-xc" 'compile)
(global-set-key "\C-xn" 'next-error)

Xcode
Another option is the Xcode editor. David Beck's Blog has some good information in setting up an Xcode project template with CrossPack-AVR. 

Eclipse
And still another option is to use Eclipse, a fantastic, popular, open source, java-based, GUI IDE. It's a pretty big application, takes a while to launch on slower systems, but it is extremely flexible, very nice to use, and powerful. They offer a version with C/C++ plugins that you'll probably want to use for AVR.

One of the folks in a local Colorado robotics club, Dennis Clark, has configured Eclipse to handle both the coding and downloading/programming for AVR. I have the plugins installed but would like to configure it to program the AVR at the press of a button as Dennis did. Still working on this.


That's All

So there you are. If you go through the above you should be good to go in programming your AVR, whether an Orangutan like I'm using, or whatever else. Happy AVR programming!

Big thanks to Mike Seeman's guide on AVR software for Mac. It's a good guide and you should have a peek. Since I needed to get my environment set up for the Orangutan anyway, I wanted to document the experience and provide an updated, simpler guide using MacPorts, in case it helps someone out there, someday.