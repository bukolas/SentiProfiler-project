The JVM-Garbage To Begin with - Recently I was asked to asked to analyze the behavior of heap in java application (with lots of short lived objects and highly transactional processes).
Here is what I came to know :-
Java is a programming language, where object creation and destruction is handled by JVM itself and this indeed is handled by one of its process called GC (garbage collector process). Four algorithm is available (not specific to Java vendor), For the defined case, gencon is considered to be best.
For most of the cases, gencon policy will work fine
Heap (memory) is divided into two physical part: Nursery (NEW) and Tenured (OLD) Space, Nursery space is logically further divided into Allocated (Eden) and Survivor Space.
New (young) objects gets a place in Eden.

Once the eden is full (or partly filled depends on the setting (JVM option) ), the process called SCAVENGE is called (in gencon policy of garbage collection).

This process will clean up the eden space i.e, deallocates the memory space of the objects no longer in use, and copies the objects to 'survivor' space (or tenured space depending upon the age of object).
As soon as the scavenger is called, the initial eden space is now referred as survivor space (yes empty) and objects in previous survivor space (yes now eden/allocated space) gets their age increased by 1.

As stated previously, the objects can get copied logically to survivor or physically to Tenured area. The destination depends upon the "ThreshholdTenuredAge" parameter (in JVM setting or dynamically set by JVM in default case)

Scavenge process is light weight (wrt CPU utilization), frequent scavenge is called (as the application supposedly had lots of short lived objects)

The major overhead while cleaning garbage is clearing up Tenured Area, where the objects with high age has been allotted space. This is done by GLOBAL garbage collection, which is triggered when the space in Tenured in not enough to accommodate new object from nursery to tenured area (through scavenge; this is called Allocation Failure). Global GC clears up Tenured Area and frees the memory!

Global GC(and scavenge also) is "Stop the World" Phase where the application thread is paused and ONLY gc thread works.
Garbage collection process mainly has three phases (and all the phases work as single step (stop the world, hehe) )
1. Mark - That is marking all LIVE objects
2. Sweep - Cleaning/ freeing and updating free list.
3. Compact - (optional) reduce fragmentation (time taking)

Bad coding is natural gift to all the expert programmers (no offense) still this can get worse if the JavaHeap is not perfectly set, that is BAD JVM OPTION. 

For instance, The JVM OPTION -Xmx512m means set maximum heap size to 512MB and the java application regularly throws OOM Exception(OutOfMemory Exception), tuning is required.
Need not be happy, the above objective case is not always the situation.
The case can be really complicated as improving performance of an application based on Memory leaks or availability is a tough and subjective task.
Google for Java Vendor Specific JVM OPTIONs (depends on version too) and do dome permutation to tune in a good application. However, the theoretical aspect can by analyzed to reduce the set of eligible jvm settings.

In order to analyze the garbage collection, enable the verbose in JVM setting (as the name suggests, verbose (who speaks a lot) is a log which tells the whole story of heap in elaborated xml format. This complicated log can be analyzed on various freely available tool and hence the memory behavior can be studied. Later parameters like fixing nursery size, tenured size, total heap size, size ratio, Garbage collection policy, max tenured age etc can be modified.
Make sure while tuning, you degrade one feature to get another, settle as soon as end user says OK.

Happy tuning
(please google for various JVM options)