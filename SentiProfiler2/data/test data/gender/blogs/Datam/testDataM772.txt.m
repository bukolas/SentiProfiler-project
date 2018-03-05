I've been extremely busy with DealBase work, so have lagged on blogging, but have some interesting things coming up and hope to post more soon. Recently I added RSS feeds to all of the city/destination and hotel pages on DealBase . This is just one more way for you to keep up to date with all the deals we add to the system. Our other primary way, aside from browsing the site of course, is to sign up for personalized alert emails . This lets you follow one or more cities, and set criteria like class of hotel (i.e. you only want to see 4 and 5 star hotels), maximum price, and so on. You can set a schedule for how often you want the emails (once a week, twice a week, daily, etc.). We'll send you up to the best 10 deals that match your criteria. You can also simply subscribe by just supplying your email address and nothing else and we'll set your initial preferences for our top 10 destinations. We'll continue to enhance this alert service, and I'd love to hear any suggestions people have for additions, changes, etc. Development of the alert emails has been interesting. One of the most useful tools throughout has been Litmus ' email testing service. It's pretty slick. They cover quite an array of email clients, both web hosted (Gmail, Yahoo, Hotmail, etc.), as well as desktop (Outlook, Apple Mail, Notes, etc.). They also do spam testing. When you begin a test, they supply you an email address specific for that particular test run, and then you just email them the real email you want tested. You can also paste in an email, but I prefer to do the sending approach to as closely mimic the real thing as possible. I have my IRB and various other bits configured to make it a single command to fire off test cases to a Litmus supplied email address, and the whole process works great, and is truly worth Litmus' cost. I started off mentioning RSS, and back to that... The RSS does work differently than the email alerts (obviously), and to some degree the web page. It is like selecting the "Most Recently Posted" sort on a page (see below image). Deals are supplied to the RSS feed as they're posted, just like a blog would do. Just one more way to stay on top of deals. Oh, and don't forget we're on Twitter too. Uploaded with plasq 's Skitch ! [Update: I resolved the Git issue, and have now switched to using Hudson. The advantages of it's in-progress display, ability to more closely monitor and/or kill a build, and my impression of it being more stable, won the day.] After switching our CI server to Integrity, there were a few blips, one of which is that we were hitting swap (memory). Hitting swap is not surprising and I'm surprised it hasn't been a problem sooner, since memory is our #1 battle. I figured if I was going to up the memory on our CI server, I might also try Hudson, as that was the main reason for not trying it previously. The short story is that I tried it, and we're back to Integrity, but I learned some interesting things. The following is both some notes on installation, as well as some reasons why it didn't work out... Installing Hudson Our CI box is a slice at Slicehost, running Ubuntu 8.04 LTS, and I'd just upped it from 256MB RAM to 1GB. Also of note, I've built and install Git 1.6.4 for this system. The following are brief notes on getting Hudson going as it wasn't quite as simple as most folks made it out to be... First, I followed the directions on the Hudson site for installing Hudson from the Ubuntu package . This amounts to adding the package repository, updating apt-get, and then doing: sudo apt-get install hudson This installs all the Java 1.6 stuff you need (and mine seems fine with 64bit Java), and other dependencies. After the install, Hudson is running, on default port 8080. Next up I added a server block/configuration in Nginx for Hudson (and unlinked/removed Integrity). I then went to Hudson in my browser. What I got was an error message about the xstream library. Fixing that was easy, as it turned out: downloaded the latest (1.320 at the time) hudson.war from the Hudson site, and replaced /usr/share/hudson/hudson.war with it. Restarted Hudson, and voila, now it was up and running. Also, Hudson runs as the user "hudson", so I needed to add an SSH key for that user, and then add the public key to GitHub. And, setup a ~hudson/.gitconfig as needed. Finally, as I found later, do a git clone or an SSH to github so that you get past the whole SSH authenticity question when you first SSH to an unknown server. Note, the Hudson user is not an interactive user, i.e. you can't directly login as that user, so to gen the SSH key, you'll need do something like su to root and do, sudo -u hudson ssh-keygen -t dsa . Configuring Hudson Before adding a project, I needed to config some plugins. I went to the Manage page, clicked on Installed, and turned off the Subversion plugin and restarted. Next was going back in to manage plugins, and installing the Git, Github, and Rake plugins, and again restarting. Both restarts showed an Nginx bad gateway error, but simply refreshing got it back (probably just needed more time for Hudson to restart). Then to configure a build, from the main Hudson page after a fresh install, click the "create new jobs" link. In the ensuing form, enter a project name, and select "Build a free-style software project". Next: checked "Discard Old Builds" which then shows you options (so you can put in keep for X days, or X number of builds) Added URL for Github project, http://github.com/yourproject/yourproject Select Git as the SCM, and entered by git@github.com:yourproject/yourproject.git URL for the repository Turned on merge after build option. This will supposedly add tags for the build to your code base and then merge them back in. More on this in a bit. Next I configured the build steps for my project. All I really did here was take the same build steps I used for Integrity , and added them as individual shell and rake tasks. At this point, I fired off a build (truth be told, I started with just a single build step to vet it), and things worked, with the exception of the very last step, where I push the Git tags back to GitHub. This is what I saw: [workspace] $ /bin/sh -xe /tmp/hudson1444107192962944065.sh + /usr/bin/git push --tags XML error: syntax error error: Error: no DAV locking support on https://github.com/dealbase/dealbase/ error: failed to push some refs to 'https://github.com/dealbase/dealbase' I've now been searching for answers to this, and haven't yielded anything. I've tried the git push directly from a shell, with the same result. If I do this as a different user (e.g. under my user) it works fine. This git push is attempted both as part of a rake task (the ci_tag task), as well as I tried making it just a straight shell command in Hudson. After a lot of googling, and asking, and no resolution in sight, I've gone back to Integrity... Comparisons and Notes First off, I think Hudson looks pretty stellar. There are a TON of plugins for it, and it seems quite mature and polished. The essentially 100% configuration via the GUI is slick. Install, despite a few hoops above, was actually pretty painless. So, here's just a few notes/opinions: Hudson You will need some memory to run Hudson, more than with Integrity or CruiseControl.rb. From what I can tell, you probably want a system with 1GB or more. Various other folks I talked to all had 2GB or more systems, and their Hudson processes were taking 1.5GB or more. This is partly just a Java thing. It should be noted, the others were running more than one build with Hudson, and mine seemed to work fine on a 1GB total memory system (didn't seem to hit swap). Hudson allows you to kill a build while it's running (nice!). Hudson works with CCMenu/CCTray out of the box. The git integration has more options in terms of picking branches, doing merging, and various more involved operations, but doesn't have GitHub post-receive hook support out of the box (there are plugins up on GitHub for it, but not listed in the standard plugin list). Hudson runs as user "hudson", which is a user that has no shell. You could change this, although the idea is you shouldn't have to. However, this complicates setting up SSH keys and various things. And, of course, I had the issue with Git as mentioned above. I could probably change this to run as my user and so on, but part of all this for me is not having to change a lot of defaults and start messing with core configurations/designs of the system. In part, I just don't have time to do that, and to maintain it (these kinds of changes often cause problems when you upgrade versions, etc. - also known as you may forget to redo these changes if an upgrade undoes them :). The UI and web app itself is quite nice, understandable, well done in general for something like this. Also, thanks to the Tea-Driven blog for motivating me to try Hudson (and for some tips on Testjour - more on that in another post). Integrity I really only have one main complaint about Integrity, and that has to do with indication of a build in progress. It essentially doesn't indicate it - it says it hasn't been built yet, but a build may actually be running. The CCMenu/integritray plugin will show you that a build is running, so this mostly solves my problem, but this seems like a core failure. I may have to look at fixing this. And that is a win for Integrity in that it's Ruby, and thus I'll be more apt to go fix this (while I spent MANY years doing heavy Java work, I don't have interest in working in that code anymore for a task like this). CruiseControl.rb One thing that remains an advantage for CC.rb is that it has "build artifacts" - i.e. you can create files and such as part of your build and have those known to CC.rb, where it then links to them in the summary of your build. You may or may not need it, but it's also very handy for simply showing you the Git tag you created on a successful build. I just touch a file in the build artifacts dir with the name of the Git tag, and then I don't have to dig through the output of the build to find my Git tag. That's all the time I have for today, hopefully this is also helpful to others... Recently I switched the DealBase continuous integration server to use Integrity instead of CruiseControl.rb. This happened because I'd been having some sporadic failures under CC.rb that didn't seem explainable (no code would change, and tests would fail at random), and also due to some changes in Cucumber between versions, it all conspired to switch. It should be noted that, as you might guess, the "random" failures were not exactly random, but suffice it to say that the root cause re-inforced my notion to switch. My main beef with CC.rb has more to do with it being somewhat in bed with Rake and wanting to run your CI build via a rake task, and some of the issues (or impurities?) that come up with that. But I'm boring you... Anyway, Integrity... Setup is relatively easy, and is covered fairly well on their setup page/docs . But the following is what I did, which I'm documenting for myself and anyone else who may find it useful. I setup my server to use with Nginx and Passenger. I tried using Integrity with Nginx and Thin, but wasn't able to get Integrity to work right (similar to the results defunkt had when they tried it at GitHub ). We've standardized on Nginx+Passenger, so this was good anyway. I nuked the Nginx I had already and proceeded (all this being done on an Ubuntu Hardy VPS system at Slicehost): sudo gem install passenger sudo passenger-install-nginx-module I let it install Nginx, and picked the default location. I then re-added our HTTP Basic Authentication and a few other Nginx tweaks as I had in the prior Nginx configuration. Next up was to install some prerequisites and Integrity itself: sudo apt-get sqlite3 libsqlite3-dev sudo gem install integrity do_sqlite3 thin integrity install /home/ci/integrity --passenger cd integrity Next you'll want to tweak config.yml to customize the domain where you'll access your CI server. I left the rest the same. Then setup Integrity's database: integrity migrate_db /home/ci/integrity/config.yml I also added the "integritray" plugin, so that I could continue to use CCMenu to monitor my builds. See the integritray GitHub page for simple install. Now point Nginx/Passenger at your install, by adding the appropriate server block for a Passenger Rails app, such as: server { listen 80; server_name your.ciserver.com; root /home/ci/integrity/public; passenger_enabled on; auth_basic "Restricted"; auth_basic_user_file /opt/nginx/conf/htpasswd; } Finally, fire up Nginx, and surf to your CI server domain/URL. You should see something like this: Uploaded with plasq 's Skitch ! Click the "create your first project" link, and enter details about your app. The "Git repository" field should get your GitHub or other git server URL (e.g. your clone URL). For a build script, the following is what I used: rake log:clear && RAILS_ENV=test rake db:reset && spec --options spec/spec.opts spec/--/-_spec.rb && RAILS_ENV=cucumber rake db:reset && cucumber --strict -q --format pretty features && rake ci_tag This is one nice thing about Integrity - it's pretty much any command you can give it. Sure, you might want to wrap that up in a Rake task, or a shell script or however you want to do it. I just entered that raw so it's overly obvious exactly what it's doing. I also found this to work better than doing it as a Rake task, as somehow I wasn't getting the environment to switch properly under Rake. The "ci_tag" rake task is my task for tagging/labeling succesful builds in Git, etc. Next, you'll want to setup a post-commit service hook on GitHub. You can get your Push URL by clicking the edit link for your project in Integiry: Uploaded with plasq 's Skitch ! Note, that works even with HTTP Basic Auth, just add your user credentials to the Push URL before pasting it in on GitHub. Finally, fire off a build. And note, one downside of Integrity is that it doesn't indicate (in the web UI) that a build is underway. It just says it hasn't been built yet. The integritray item and thus CCMenu will show you that it's building though. Lastly... some have asked why not XYZ CI server? A few notes on this: Hudson: this looks awesome, but also somewhat overkill for our needs. I have one project to build, and I'm doing it on a very inexpensive slice that has only 256MB of RAM - I doubt I could even start Hudson in that little RAM, being it's a Java web app, etc. Secondarily, I'd prefer to have the app be Ruby so I can hack on it (I've made at least very minor tweaks to every CI server I've used to date). cruisecontrol.rb - this is what we were using, and it worked well, with minor exception to some random failures and the Rake-oriented build process. I'd really say this is minor though, and would suggest folks try it out. You can of course refer to my previous writeup on setting up CruiseControl.rb :) CI Joe/cijoe - this is actually what I started with when I looked at exploring alternatives. I had problems getting the build working properly, which seems odd. In hindsight that may have wound up being due to some problems during our switch to a newer version of Cucumber. But, one thing I didn't like is that there is no state maintained, so if you stop and start cijoe, it loses track of all its previous builds. This may or may not matter much to you, but I didn't like that. I also didn't want to spend time writing notifiers/CCMenu integration type stuff for our needs. I will ay that cijoe setup/install is pretty cool. Run Code Run doesn't have a viable plan for us yet, plus I've heard having it run custom rake tasks and such doesn't work (wrong?). Furthermore, I didn't really want our private code on their servers and didn't see a need to outsource this. others... either hadn't heard of them, they didn't work well with Rails, or whatever - upshot, Integrity worked, got it up and running fairly fast, and didn't need to spend any more time on this. A little bit ago, I went back to using multiple computers again. For a while I'd just done everything with a MacBook Pro, which was nice - only a single machine to maintain, always had everything in one spot. But alas, twas time for more performance, so I got a Mac Pro, which is now my primary work machine
 with the laptop relegated to basically iCal and Things for the bulk of the day (as my 3rd monitor), and then any portable needs, or working on the couch or deck, etc. Anyway, my main thing against multiple machines, after having spent many years with anywhere between 2 and 5 machines (mostly while working for Adobe), was the maintenance and synchronization of those machines. So, now that I'm back to 2, I am back to wanting/needing my data on whatever computer I'm using. These days this also really extends to my iPhone to some degree as well. It's my truly portable computer. So, with all the "cloud" computing/services these days, this is to some degree easier. I'm not that big a fan of Google having all my data, but I will use the services that are the best in class, or best for my needs, and no doubt some of that is Google, but there are many others. I have not gotten all my data truly available on all the computers I want it on, or as well as I'd like yet, but I'm getting closer. Also, I agree with Al3x in that if the data is important, only having it on a free cloud service is not wise. I haven't fully followed that, but that is part of my plan. Here's some notes on what I've done so far... Email This one is easy. I use Gmail for all my email accounts (about 15 if you count across various domains and so on). Right off, this is one case where this is the only place my data/email lives, so I'm only solving the accessible from anywhere issue. But, I'm also of the mind that I could pretty much lose all my email and be fine. If there are truly important things in it, that data tends to wind up getting put in somewhere else (EverNote, etc., see below) as appropriate. Gmail is so much better than any other Email. I even use it for some non-traditional things, like all our recipes are stored in a Gmail account. Makes it easy to trade recipes amongst friends and family, great for searching, and again, I can get to it from anywhere I can access the web. Further, I use MailPlane as my actual mail client. It is a very rare day that I use a web browser to view Gmail. On the iPhone I just use the regular iPhone mail client, which has some drawbacks, but for my needs is ok. Notes and Misc Data This is the place that recently changed and has become an outstanding solution. I used to use 37signals Backpack , but now use EverNote . EverNote is really quite amazing. I hadn't noticed the Notebooks before, and that's been a great feature for me to do high level organization. Then, the searching is quite good (and of course Backpack essentially didn't even have searching). But, what really swung this over was their desktop client, web storage, and then iPhone client. This gives me great desktop performance, but then access from any web browser, as well as iPhone, AND I get the data stored locally and off-site (i.e. on the web). Furthermore, it sync's across computers. So, I feel like the data I have here is very safe (since it's in at least 3 places). Even better, this allowed me to stop paying for Backpack (my use of EverNote is nowhere close to their paid account). I don't mind paying for software at all, but I had felt that I wasn't getting the solution I wanted from Backpack, so didn't want to pay for that any longer. Code GitHub and Git. Do I need to say more? Bookmarks Two things here: .Mac and Delicious. I use Safari as my primary browser, and leverage it's Bookmark Bar for common stuff, so sync that across machines and iPhone with .Mac. Delicious gets all the other bookmarks. RSS/News Feeds Another recent change was to stop using NetNewsWire which I've used forever, and switch to Google Reader. Specifically what made this work well was creating a Fluid app with the Helvetireader styling. NNW had sync via NewsGator, but it really just didn't work well. It didn't seem to keep the list of feeds in sync, and then it's sync during feed download just didn't seem to cut it. So now, with the fluid app, and then just plain Reader (via browser) on the iPhone seems to solve this all nicely. Documents So far this is a mix of using DropBox and Google Docs. DropBox is awesome for keeping files in sync across machines, and then also available on the web. I've been ramping up my use of DropBox. Things that Aren't sufficiently solved yet Calendar and Address Book Update: I'm now using .Mac to sync Address Book, seems to work great. I'm using iCal and Address Book. Primarily this is due to the super easy sync with the iPhone. I can sync two computers with BusySync or some other things, but haven't seen a great solution yet, as well as haven't investigated that much. Even with .mac I think you still have to have a primary calendar and you're publishing to the other, etc. I've used Google Calendar in the past, but didn't like it that much, and part of all this is ensuring sync is brain dead easy and solid. My wife and I sync calendars with BusySync, but that has been spotty. The various Google/iCal sync things I've tried have been iffy in the past. And, the real key is, how well will it sync Google cal to the iPhone calendar? An area I need to learn more about. Address Book, not sure of my options here either. Sync's nicely with iPhone, and .Mac so far. Music and Photos I've yet to see an iTunes sync thing that works really well. We tried TuneRanger but it was not good when dealing with two existing libraries. Also, we have a lot of music, so keeping it backed up on S3 or similar actually starts to cost real money. Of course these days, the primary need for this is to sync to my iPhone. Most of the time at home I'm listening to Pandora or some similar thing. I use Pandora a lot on the iPhone, but I also listen to various podcasts, and then there are times when Pandora isn't feasible (crappy/no signal, or I'm needing to use another app on the phone while listening to music). These same issues hold true for photos/Lightroom libraries. With Lightroom so far, for each calendar year, I have a Lightroom catalog, and then at the end of those years, I burn DVD's with the photos. Of course, the DVD's are just sitting in our house right now, not in a safe deposit box, etc. (we used to do that, but haven't since we moved to Oregon - lazy). But my bigger concern here is that I'd like my Lightroom library to stay in sync across my two machines, so I can use the burly Mac Pro when working at home, but then have my MBP when on the road. I guess that's it for now. What solutions do you like? What are people doing for large data (GB's or TB's of music, photos, video, etc.)? At DealBase , we've been testing Nginx with Passenger , and have mostly had good results. There are two issues that have come up, hopefully only one of which may broadly affect others. The first issue, which likely affects anyone using this, is that it appears that if you combine and cache CSS or JavaScript via tags like this in Rails: &lt;%= stylesheet_link_tag :standard, :cache => 'standard' %&gt; &lt;%= javascript_include_tag :jquery, :cache => 'jquery_all' %&gt; The ":standard" and ":jquery" symbols are expansion symbols for multiple CSS/JavaScript files defined in a Rails initializer. On the first request Rails gets, it's supposed to combine all the files as per the expansion symbol definition, and then produce a "cache" file, so you have a single file that is included in your HTML. This worked fine for us under Mongrel, but it didn't seem to regenerate under Passenger after doing a deploy, even with a restart to Passenger. We had to do a second restart of Passenger, and then hit the server at least twice, if not more to see it get picked up. Thanks to a tip from Engine Yard, one solution can be found on the overstimulate blog , where they detail how you can add a rake task that you call during deploy to regenerate those cache files. This is really even a nicer solution under Mongrel and others, as it will mean it doesn't occur during your first request. The second problem seems confined to my MacBook Pro laptop (no problem on my MacPro tower). That is, I simply cannot get Nginx+Passenger to work. It installs fine, Nginx runs, but I get some odd permissions problem from Passenger: 2009/05/12 11:57:52 [alert] 19611#0: could not create /var/folders/7m/7m7ezMSTHdiBHz5bzVyNDE+++TI/-Tmp-//passenger.19596/control_process.pid (13: Permission denied) 2009/05/12 12:01:19 [crit] 19611#0: -1 connect() to unix:/var/folders/7m/7m7ezMSTHdiBHz5bzVyNDE+++TI/-Tmp-//passenger.19596/master/helper_server.sock failed (13: Permission denied) while connecting to upstream, client: 127.0.0.1, server: dealbase.dev, request: "GET / HTTP/1.1", upstream: "unix:/var/folders/7m/7m7ezMSTHdiBHz5bzVyNDE+++TI/-Tmp-//passenger.19596/master/helper_server.sock:", host: "dealbase.dev" The first line of those two lines occurs when I start Nginx. The second happens when I try to surf to a page from my app in my browser (which makes sense given the first error :) If anyone has any suggestions on this one, let me know! At DealBase, we have an opening for a part-time front end Rails developer at DealBase.com . The opening is for US residents only, and for individuals, no agencies or recruiters please. Most likely you'd be working remotely/telecommuting. The job posting, which is posted in several places, such as Rubynow , Working With Rails , and Rubyjobs.in , covers all the details, but I'll relist it here for ease: DealBase.com , a startup hotel deals site, is looking for a stellar front end web developer who will adapt our current look/feel to new features, leverage JavaScript for useful and fun features, and is eager to apply their skills to enhance the user experience of our site. We're looking for you to share your knowledge and make an impact, be passionate about your work, and up-to-date on the latest technologies. If this is you, and you enjoy working with a small, distributed, agile team, then we'd love to talk with you. Requirements for this position: Deep knowledge of XHTML and CSS Familiarity with browser capabilities and restrictions for all major browsers Solid JavaScript skills Experience with/demonstrated use of Git You use and demand MacOS X as your primary development environment Comfortable at the command line Basic skills for image editing and optimization for the web Exposure to and basic knowledge of Ruby on Rails Great communication skills Attention to detail Ability to work both independently and on a team Eagerness to share ideas and problem-solve creatively Experience working on consumer oriented web applications/consumer focus Quick learner, and good at digging in to problems Agile development practices You are based in the US. Individuals only (no multi-person firms, agencies, etc.) Nice to have: jQuery experience GitHub experience MySQL experience Use of test frameworks, TDD, and BDD experience with Linux If you'd like to work with us at DealBase.com and think you're a good fit for this position, send us a resume and sample work, or let us know where we can see your resume and work/code, by emailing jobs@dealbase.com. Please note, we are only considering candidates based in the US. I'm excited to find a great developer to work with. DealBase has been an awesome company and app to work on, and we're already experiencing great success. We have some pretty cool features planned, and it'd be ideal to get some real CSS and JavaScript ninja skills making those features even better. So, if this is you, please do get in touch, making sure to send email to the right email address as outlined in the job description. There's been a couple cool writeups/solutions to deploying your crontab files when you deploy with Capistrano, which I think is great. I can't find the first one I saw (mention in comments and I'll update), but on GitHub, javan has the whenever gem that is really more about allowing you to define crontabs with Ruby/Rails' time methods so you don't have to remember the crontab file syntax which none of us ever seems to be able to remember. You can of course integrate this with Capistrano (and that's covered in his Readme). The point of all this: no more having to remember to go put in or uncomment a crontab entry once you deploy a certain build, and keeping your crontabs under version control. However, for us, none of the solutions out there worked quite right, and I just use what I find to be a simpler setup. First, we have multiple servers with different crontabs per server. Also, we have some environment variables that get defined within the crontab so that they work properly on our Engine Yard slices. I just found that, while yes, I sometimes don't remember all the crontab syntax perfectly, I also don't do this often enough for that to be an issue, and would rather just have the real deal right there, so I knew exactly what I was going to install on my server. Lastly, I didn't really want to have yet another gem dependency for something pretty straight forward like this (IMHO). So, get on with it you say, what's the solution? Two pieces. First, I create a crontab directory within my Rails app's config directory. In that I store crontab files named by the hostname of the server - the same thing you'd get by doing a hostname on the server. You could add an extension or whatever you want, but the hostname is what makes this work easily, so you want that somewhere in your file naming convention. We only have a couple servers and I know them well, so I just went with pure hostname for now. The contents of each file are exactly what you'd see in the crontab file on the server, for the user you set it up under. Second, a simple Capistrano task to affect the given crontab file on the server, with an after hook to run it: task :write_crontab, :roles => :app do puts "Installing server-specific crontab." run("cd #{deploy_to}/current/config/crontab; crontab `hostname`") end after "deploy:restart", "write_crontab" That's it. You can obviously tweek this for your own setup, for example, maybe you need to run it on all roles, or different roles, or what not. Your run command might need to be more robust (or run a shell script or rake task) for example if not all servers have crontabs or you have something more dynamic. But, as you can tell, setting this kind of thing up is pretty straight forward, and it's great to keep your crontab setups in version control. Thanks to you guys that stimulated the idea in the first place. Recently we ran into an interesting performance issue with MySQL. We have an automated process we run at night a few nights a week that does data harvesting for hotel rates and such. This data is versioned so that we can look at historical values. However, this script had begun to really crawl. Originally it took a couple hours to run. But it had gotten to the point where it could take almost a day. I tracked this down to being a SQL MAX call used by acts_as_versioned to determine the next version for one of these records. The problem is that it had to sift through nearly 10 million records. In testing this on my local machine, just one of these SQL queries could take 45 seconds! Think about doing this across oh say 100,000 hotels, ya, not good. The good folks at GitHub ran into this same thing (with a table of 36M records) on nearly the same day. Their approach is similar to the approach I'll be taking on another table (which isn't currently affecting us this way, but will have different benefits), which was to split it into two tables, one with older data. I could have done this, and would have, but the reality was that we simply didn't need to keep these versions, as we weren't using the data. So, luckily, I was able to just no longer version this particular model, and throw out that table. After doing that, I ran the script and it took just over an hour. Yea! So, this is something to note if you use acts_as_versioned with models that have frequent changes and a decent number of those models to begin with (think multipliers). One of the things I'll be looking into in the future is whether that MAX needs to get done, or whether acts_as_versioned can be smarter about how it does it. On first glance you'd think you could just use the version number on the original model itself, but that number isn't guaranteed to be the latest number, since you can rollback versions and so on.