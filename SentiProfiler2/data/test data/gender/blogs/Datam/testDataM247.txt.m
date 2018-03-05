Every March, users all around the world flock to Yahoo! Fantasy Sports to play our NCAA Tournament bracket game, “Tourney Pick’Em.” It’s one of our most popular games.

In many ways, it’s also one of our simplest. Just fill out your bracket by selecting the teams you expect will win. Sounds easy, right? Well there’s a catch — there are 9,223,372,036,854,780,000 different possible ways you can fill your bracket out.

That absurdly huge number presents a challenge for us as well. How do you make something with 9 quintillion possible combinations so easy-to-use that a user can fill out their brackets in just a couple of minutes? The answer involves a fair amount of JavaScript beneath the hood, which we call our “bracket engine”.

The bracket engine we had been using since 2004 had served us well, but after six NCAA tournaments it was beginning to show it’s age. The YUI Library didn’t even exist when it was first written (for that matter, neither did JSON or Firebug). So for this year’s game, we decided that instead of the incremental improvements we normally make, we’d start from scratch. Rebuilding from the ground up gave us the opportunity to completely modernize our bracket engine, so it’s only fitting that we went as modern as we could by using YUI 3. This would be one of the first projects Yahoo! Fantasy Sports has built with YUI 3; but, at this point, we knew that the strengths of the library were too great to pass up. Happily, the new bracket engine turned out even better than we had hoped for.