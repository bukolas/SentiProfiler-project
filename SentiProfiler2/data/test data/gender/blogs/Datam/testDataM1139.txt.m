Hello I'm back.

My current weapon of choice is Java though.

The renderer supports:
median split BVH,
thin lens and pinhole camera models,
sphere and Triangle primitives,
naive path tracing with lambertian surfaces only,
rendering in a progressive manner.
Performance is not that bad. I designed it to take full advantage of the new feature of java called Escape Analysis, so I'm not reusing objects (what leads to ugly code btw.) but creating them like crazy.

What sucks though is that there is no 1.6.0_14 java for Mac OS X yet, but I've tested in on windows and it works very well.

Some first, naive path tracing, renderings: