Pokey V2.0


There's talk of another local robotic firefighting competition some time in May or June!

Pokey's defeat in 2008 has nagged at me for the last couple years so time permitting Pokey and I are going to take another crack at it.

Pokey needs a refit and redesign. Here are some of the topic areas I'll be covering in the near future --

-- but first, what won't change?

No Dead Reckoning

You may recall that Pokey does not use dead reckoning. I want to continue with that design philosophy. Pokey relied on wall following and "events" to navigate -- the appearance/disappearance, while moving, of walls and floor markers.

Smooth Speed 

Pokey was always intended to be a fast robot. His name comes from the fact that I had to slow him down before the original competition to increase navigation reliability.  I don't want to slow him down further. If anything, I'm hoping to speed up the little fella. Also, Pokey was built to move smoothly and fluidly through the maze and I don't want to change that, either.

Budget

Pokey was intended to be somewhat low buck, with cheap, minimalist solutions preferred over fancier, more expensive ones where possible. I may have to admit defeat in a few areas and throw some more money at the problem, but I still want to come in under the cost of a Lego NXT when all is said and done.

Despite the things that won't change, clearly some changes are needed for Pokey to complete his mission and these things will be the subject of upcoming articles.

Navigation Problems


Thinking it through, most of the navigation problems boil down to poor wall following and failing to execute precise turns.

The wall following system was marginal. It could usually maintain a correct alignment but failed to correct even moderate misalignment. A single wall distance sensor was inadequate given short maze walls and a fast robot. A pair of wall distance sensors on each side should solve several problems at once.

While executing consistent, constant radius turns wasn't too tough, reliably turning to a precise heading was. The trigger to terminate the turn was the distance of the wall that the robot was turning to.  It just didn't work.

I suspect using either a rate gyro or wheel encoders -- just for turning, not dead reckoning! -- would provide more precise heading changes and fluid movement. If I can actually pull it off, be assured you'll hear about it here...

Some robots had success aligning to the regulation door threshold floor stripe. This approach alters the flow of robotic movement as it enters the room, but maybe I can live with it if the gyro and encoder options don't pan out.

Flame Detection Problems

Pokey failed to detect a flame the one time he encountered the candle in the contest.  I ran out of time to really dial in the calibration and software. The sensor itself works ok at closer ranges, poorly at long range.  It's big and heavy, limiting fire suppression system options and making Pokey less nimble.


Picture from superdroidrobots.com of Hamamatsu UVtron


Affording (or justifying the cost of) a UVtron or Eltec Pyroelectric flame sensor -- or a CMUcam or NXTcam vision sensor -- is tough. The AVRcam is more affordable and, apparently, just as capable as these other two vision systems. Or sticking with some form of IR detection is still a possibility.

I'm currently exploring some cheap DIY camera/video options. I really think that's the best way to go since the last contest winner was using an NXTcam and very easily and reliably detected the candle. Not to mention, I could reuse this type of sensor for many other purposes. More on vision in future articles. 

Telemetry

One of the biggest difficulties was that Pokey didn't collect data for later analysis. I never quite knew what the robot was doing from moment to moment. I'm working on using bluetooth-based communication for telemetry reporting and logging. More on this in an upcoming series of articles.

Collision Avoidance

Finally, it'd be nice if the robot could priority override all other functions to prevent head-on wall collisions...

Of course the biggest challenge is time... but at least I don't have to start totally from scratch.
at 7:00 AM 4 comments  

Labels: firefighting, Pokey

Friday, March 5, 2010

Heathkit and Robots

So I fixed up my Heathkit IO-12 oscilloscope and the Heathkit RG-8 frequency generator is next on the to-fix list.  I'm also still working on building the oscilloscope calibrator; it's based on a Heathkit design.

  


Heathkit sold robot kits, too, as you may know.  I remember in college every time I passed the ground floor ECE Building labs, looking in the big windows, being greatly envious of the guys in the lab on the first floor of the ECE Building who got to play with the HERO 1 (as above left).

I guess some of that envy shows in my goofy sketch of Edward Isaac Bot's head, what with the integrated hex programming keypad.  I think it's safe to say that feature would be ditched in a modern verison...

Apparently a Heathkit HE-Robot 2000 is available these days, a rebadged PC-Bot 914 from White Box Robotics. (The picture below is from Retro Thing; presumably they got the pic from the Heathkit site or a news article or something).