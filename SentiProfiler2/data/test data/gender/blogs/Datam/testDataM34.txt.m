Playing with Xcode and AppleScript

In case you noticed a post about PDFopener and were curious, I've been playing with Xcode, the Mac OS X development environment. And AppleScript. I guess either could be useful if I ever build a robot with a Mac brain. I wonder how hard it would be to use Xcode to develop for ATmega MCUs. At any rate, the reason for the PDFopener project was that I got sick of problems downloading USPS shipping labels so I dove in and fixed it.


set text item delimiters to ".LabelGenerationServlet"
set stripOther to first text item of fileName
if stripOther is not equal to fileName then

AppleScript is somewhat bizarre to me, an outgrowth of HyperCard's HyperTalk language (anyone remember that?), sort of a pseudo-English programming language. It's a bit mind-boggling for a dude used to C and Perl. I didn't spend any time really learning the language fundamentals but was able to slog my way to making a functional program, chipping away at it little by little with a lot of searching on macscripter.net. Trying to do too much at once and it would all fall apart. Like building a house of cards. That's not unlike my robotic coding experiences thus far, actually.
at 8:00 AM 0 comments  

Labels: mac os x

Saturday, January 30, 2010

Unicorn-1 Arm

Time to arm the robots! No, not for world takeover. I'm talking about robotic arms. Digging through the stack of old robot stuff I ran across a photocopy of this ancient, August, 1980 series from Radio-Electronics.

The first article offers a neat, simple design for a robotic arm that seems relatively easy to build with some basic tools and careful techniques. Interesting excerpts are reproduced here for educational purposes.
The elbow-forearm actuator is comprised of a large motor and threaded rod that pulls a on the midpoint of the forearm. The shoulder is actuated, I think, from a motor within the robot. Details on the design, dimensions, etc, are given below.
To save weight, I suppose one could change the design to use aluminum tube instead of steel rod for the side rods. They're available from home improvement stores, hardware stores, and online shops.

Some parts (right) need to be machined, unless one comes up with a simpler way to fabricate them. If so, I think all that's really needed is a drill press, a way to ensure drilling a hole through the center of a rod, a cutoff saw, some hand tools, and a tap or two.

An electronics article from the 80's would be good for a chuckle. But since motor and mechanical technology haven't changed all that much, I submit that these 30 year old plans are still relevant for the modern robot builder.