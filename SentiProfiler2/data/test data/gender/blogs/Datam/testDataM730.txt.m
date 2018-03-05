This article, written by Jonathan Hartley, was originally published in the Python Magazine. Acceptance Testing .NET Applications using IronPython Unit tests demonstrate to developers that individual functions and classes work as expected. Acceptance tests are an orthogonal complement to this. They verify to everybody, including managers and clients, that features they understand and care about are completed and working correctly. They also prove that the system as a whole is correctly integrated and that no regressions have occurred. Resolver Systems is developing a .NET desktop spreadsheet application, Resolver One, for which we have accumulated an acceptance testing framework. This framework uses Python’s standard unittest module, and is executed using IronPython. While Resolver One is written in IronPython, this technique allows IronPython tests to interact with product code written in any .NET language. This article describes the principles of this IronPython acceptance testing framework, and demonstrates them by creating an acceptance test for a small sample C# GUI application. March 9th (that's tomorrow at the time of typing) IronPython in Action is the Manning deal of the day. This is a one day offer with a special discount. You can get the discount by buying IronPython in Action from the Manning website and using the discount code dotd0310tw. It isn't only IronPython in Action that is on offer, you can also get Quick Python by Vern Ceder. &nbsp;In 2009 I made 312 posts on IronPython-URLs, a bit short of an average of one a day that it looked like I might manage earlier in the year. According to google analytics there were more than 112 thousand page views to a total of 2042 pages on the blog. An average of over three hundred page views a day. Feedburner estimates that this blog has around eight hundred readers. Many of those will read the articles via RSS or Planet Python and rarely visit the website. It's interesting to note that of the most popular articles in 2009 (most page views) several of them are to do with choice of IDE or using IronPython with Visual Studio. The question of which IDE to use with IronPython is still alive and well: New Sample: Writing a DLR Language in C# or IronPython &nbsp; (May 2009, 1892 page views) IronPython in SharpDevelop 3.1 and Converting C# to IronPython &nbsp; (May 2009, 1412 page views) C# Becomes a Dynamic Language... &nbsp; (December 2008, 1164 page views) Python is the easiest language in which to do interesting things &nbsp; (February 2009, 1150 page views) Python Support in Visual Studio &nbsp; (June 2009, 1119 page views) Another IDE for IronPython? Eclipse and PyDev &nbsp; (July 2009, 1077 page views) Davy's IronPython Editor &nbsp; (January 2009, 1032 page views) Jeff Hardy: Django, Zlib and easy_install on IronPython (January 2009, 949 page views) Writing an IronPython Debugger &nbsp; (March 2009, 816 page views) Running Python from C# &nbsp; (September 2008, 789 page views) My recent post Does Microsoft Take Dynamic Languages Seriously looks a bit at how much work it would be to get IronPython support in Visual Studio, and there have been a couple of interesting posts on this topic since. IDEs and IronPython is a topic that is likely to continue to be of interest in 2010 as well. Another collection of IronPython and DLR related articles from around the web. A fine way to end 2009. SLog: Silverlight Logging A nascent project to Port Log(4|5)J from Java to C# with the goal of usefulness in Silverlight, especially for IronPython. A WPF Picture Viewer NotifyIcon to use from IronPython &nbsp;Two Japanese blog entries, both by sasakima-nao. As with previous entries the code examples are very readable. The first is a simple WPF picture viewer (nice penguins) and the second shows how to create a NotifyIcon and ContextMenu in the taskbar (with Windows Forms classes). Python-Ruby (and a little bit of soap) This blog entry is in Russian, but I think there are enough code examples for it to be useful for those of us who don't speak Russian. As I've mentioned before the promise of the Dynamic Language Runtime is that dynamic languages can interoperate and share libraries. This is exactly what this blog entry shows: using the Ruby soap/wsdlDriver from Python. The cool thing is that this is done with a helper / wrapper library, that looks like it could be used to expose virtually any Ruby module / class that can be accessed through IronRuby to IronPython. Using his ruby module, the code looks like this: from ruby import _import_, get_class _import_('soap/wsdlDriver') Soap = get_class('SOAP::WSDLDriverFactory') client = Soap('http://localhost1/Service1.asmx?WSDL').create_rpc_driver() client.HelloWorld(None) I've Been Bitten By... IronPython Marcus McElhaney has discovered IronPython and likes it. The reasons he gives are: 1. I can very easy use everything I know about the .Net Framework, VB.Net, and&nbsp; C# 2. I can run IronPython in Visual Studio 3. I can reference ESRI's&nbsp; ArcObjects libraries in&nbsp; Visual Studio and IronPython What's even more gratifying is that he has been exploring IronPython through IronPython in Action and likes that too. A slight misspelling gives rise to my favourite quote about IronPython in Action: IronPython In Action is probably the best Python book I have ever pickled up because it also explains a lot of not just Python but also about .Net. If any book deserved to be pickled up, this is it... Have a great 2010. My belief is that the answer to the question in the title of this entry is an emphatic yes. Microsoft have poured a lot of money into IronPython, IronRuby and the Dynamic Language Runtime and have demonstrated a consistent commitment since the inception of IronPython. What they haven't done is build full support into their premier development tool, Visual Studio. The reason for this is that it is a very difficult problem. Visual Studio is built around statically typed languages. Features like intellisense, refactoring and code navigation all rely on having type information which is absent in languages like Python and Ruby. (The way they are implemented in Visual Studio requires that information I mean.) What Microsoft have done is provide example integration in the form of IronPython Studio, which frankly sucks . Many important features are fragile, broken or missing altogether. Good IDEs like PyDev and Wing do provide these features, so it is definitely possible - it just requires a lot of work from scratch. Microsoft have however added intellisense for Javascript , particularly for jQuery, into Visual Studio. Javascript is of course a dynamic language, but without having tried the integration I can't tell you how well it compares to using a good Python IDE. I believe that Microsoft would like to add support for dynamic languages to Visual Studio and that it will come eventually. Really they have a lot to lose by not doing it. Now they have a good platform for dynamic developers to use, which they give away free, but the tools they charge for don't really support them. This puts them in a position of either bringing developers to their platform but not being able to make any money, or having their existing developers stay with their platform but losing dependence on the toolset. Neither option looks much good for Microsoft. ( NOTE: for those who can't wait for official Microsoft support and have Visual Studio 2010, try Jeff Hardy's IronPython for Visual Studio Extensions .) Anyway, K. Scott Allen sees things a little differently and I understand his frustration: Is Microsoft taking Dynamic Languages Seriously? Consider this … IronPython got underway in July of 2004. Five years later it appears IronPython is still not a candidate to be a first class language in the .NET framework and tools. You can vote on this issue . Microsoft first released IronRuby at Mix in 2007. Nearly three years later it appears IronRuby is still not a candidate to be a first class language in the .NET framework and tools. You can vote on this issue . A first class language is deployed when the full .NET framework is installed. It’s as easy to find as csc.exe . It’s not a language you have to ask the IT department to install separately. It’s not a language that requires you to jump out of Visual Studio to edit or run. Most of all, a first class language doesn’t require justification to higher powers. A first class language is pre-certified and stamped with a seal of approval. It’s as easy to use in the locked-down big corporate setting as the company paper shredder. Of course here we are talking about Microsoft as if it was a single entity with a single intention. The reality of course is that Microsoft is a huge company with many divisions and even more individuals working there. In all likelihood the vast majority of Microsoft employees have never heard of IronPython. The relevant division is the programming languages group, which includes Visual Studio development, and where the majority of employees who are developers probably have heard of IronPython if not actually used it... Adding official dynamic languages support to Visual Studio would require substantial investment of time and effort, so even if everyone in this department was determined to add support it would still be dependent on forces from other parts of the company who have other needs and priorities... Ironclad is a compatibility layer that allows you to use Python C extensions with IronPython. Ironclad is open source and development has been funded by Resolver Systems and it is integrated into Resolver One to allow you to use Numpy within Resolver One spreadsheets . Ironclad works by implementing the Python C API in a combination of C#, C and Python. Although Ironclad only works on 32 bit Windows at the moment the implementation has been done in such a way that porting it to run on other platforms (with Mono) and 64 bit would be relatively easy. Patches welcomed! Recent development has changed the implementation to use gcc-xml to access and transform the Python C source code. By reusing as much of the original implementation as possible it minimizes the amount that needs to be 'hand-coded'. It leaves only a (moderately) small core that would need to be reimplemented if Jython, PyPy (or other implementations) wanted to reuse Ironclad. The C# would need to be re-coded in Java or RPython, using the platform's native FFI instead of PInvoke on .NET. The advantage of reusing Ironclad is that difficult problems, like handling the Global Interpreter Lock and matching reference counting to different garbage collection strategies, are already solved (well, for some value of solved...). Anyway, that is all by way of introduction. William Reade, core developer of Ironclad, has just announced Ironclad 2.6 RC. This is a release candidate of Ironclad targeting IronPython 2.6. Ironclad 2.6.0rc1 released I'm very happy to announce the latest release (candidate) of Ironclad, the 120-proof home-brewed CPython compatibility layer, now available for IronPython 2.6! No longer need .NET pythonistas toil thanklessly without the benefits of bz2 , csv , numpy and scipy : with a simple ' import ironclad ', (most parts of) the above packages -- and many more -- will transparently Just Work. Get the package from: &nbsp;&nbsp;&nbsp; http://code.google.com/p/ironclad/ ...and get support from: &nbsp;&nbsp;&nbsp; http://groups.google.com/group/c-extensions-for-ironpython ...or just ask me directly. I'm very keen to hear your experiences, both positive and negative; I haven't been able to test it on as many machines as I have in the past, so your feedback is especially important this time round-. Cheers William - I'd be especially grateful if someone with a newish multicore machine would run the numpy and scipy test scripts (included in the source distrbution) a few times to check for consistent results and absence of weird crashes; if someone volunteers, I'll help however I can. William has recently started blogging. I recommend browsing the few entries he has already posted, particularly this rant on static typing and this post on .NET marshalling , but his latest is of particular interest: Spare batteries for IronPython As we all know, Python comes with batteries included in the form of a rich standard library; and, on top of this, there are many awesome and liberally-licensed packages just an easy_install away. IronPython, of course, includes -most- of the CPython standard library, but if you're a heavy user you might have noticed a few minor holes: in the course of my work on Ironclad, I certainly have. Happily for you I can vaguely remember what I did in the course of bodging them closed with cow manure and chewing gum; here then, for your edification and delectation, is my personal recipe for a delicious reduced-hassle IronPython install, with access to the best and brightest offered by CPython, on win32. More IronPython and DLR related projects, articles and news from around the web. Embedded IronRuby and IronPython in Silverlight with Multiple Source Files A&nbsp; nice example of embedding both IronPython and IronRuby in a single C# project. As an added bonus the project is a Silverlight project so you can add both Python and Ruby scripting to applications that run in the browser. slimtune: A free profiling and performance tuning tool for .NET applications IronPython 2.6 has useful new hooks for profiling and debugging IronPython code. Unfortunately most 'standard' .NET tools don't know how to use these, and if you attempt to profile IronPython code (particularly in an embedded environment) you have to work hard to get useful information about performance of your Python code. It's nice to see a new (and open source) tool that is designed to work with IronPython: SlimTune is a free profiler and performance analysis/tuning tool for .NET based applications, including C#, VB.NET, IronPython, and more. It provides many powerful features, such as remote profiling, real time results, multiple plugin-based visualizations, and much more. The source code is available under the terms of the MIT License. SlimTune is currently in the prototyping phase of development, but a preview release is available for testing and feedback is welcome. Both x86 and x64 targets are supported, but only sampling based profiling is available in the release. Future of Managed JScript When IronPython 2 and the Dynamic Language Runtime were announced one of the three Microsoft developed languages that ran on the DLR was JScript. Managed JScript was an implementation of ECMAScript (otherwise known as Javascript) and touted as a useful bridge for porting 'traditional-ajax' applications to run on Silverlight. Unfortunately as the DLR evolved JScript languished and there was no official word on its fate. In this post on the DLR Codeplex forum Bill Chiles (DLR Program Manager) gives the bad news: The DLR JScript was experimental for informing the design of the DLR (expression trees, interop, callsites, hosting, etc.).&nbsp; The JS we released with asp futures and the Silverlight dynamic sdk became very old and unserviceable as the DLR continued evolving for release in CLR 4.0.&nbsp; Unfortunately, there are no plans at this time to develop and release a DLR-hostable JScript. IronPython SHGetFileInfoW is ctypes A Japanese blog entry showing example code using the new implementation of ctypes in IronPython 2.6. ctypes is the Python FFI and in IronPython it is built on top of PInvoke , the .NET FFI. ctypes is used for calling into native code, like the Win32 API. Calls SHGetFileInfoW , which " Retrieves information about an object in the file system, such as a file, folder, directory, or drive root ." IronPython on deviantART No idea what this has to do with the implementation of Python for .NET, but IronPython is the username of someone on the alternative community art site deviantART. Some pretty nice computer generated art, and who knows - maybe they were created from Python... IronPython and the Dynamic Language Runtime make it almost ridiculously easy to add scripting to .NET applications. In recent weeks several examples of using IronPython to add scripting or interactive shells to .NET applications have been posted by the .NET community: Command-line scripting of IronPython code in 
utoCAD This post was heavily inspired by the code presented by my old friend Albert Szilvasy during his excellent AU class on using .NET 4.0 with AutoCAD . ... In this post we’ll take Albert’s technique and implement a command-line interface for querying and executing IronPython script. This approach could also be adapted to work with other DLR languages such as IronRuby, of course. Here’s the updated C# code which now not only implements PYLOAD functionality, but also a PYEXEC command. &nbsp; Scriptability via the DLR and PostSharp &nbsp;Making an application scriptable (particularly in a static language) has historically been difficult. With the advent of the DLR (Dynamic Language Runtime) on the .NET platform it becomes almost trivial to add scripting support to any application. For a recent project I needed the ability to add scripting hooks throughout the application and coupling the DLR with PostSharp AOP attributes made this effort pretty straightforward. Here’s how it was done. Revit Python Shell Introducing RevitPythonShell According to wikipedia : " Autodesk Revit is Building Information Modeling software for Microsoft Windows, currently developed by Autodesk, which allows the user to design with parametric modeling and drafting elements.&nbsp; Building Information Modeling is a Computer Aided Design (CAD) paradigm that allows for intelligent, 3D and parametric object-based design. " Jeremy Tavik blogs about working with the Revit API, and in this post he discusses a Python Shell for Revit that created by Daren Thomas with IronPython: Daren Thomas of the Professur für Gebäudetechnik, Institut für Hochbautechnik at the technical university ETH Zürich has published a Python Shell for Revit. It was implemented using IronPython and is used to automate the running of daily tests of a building energy analysis package. Hosting IronPython via a Revit plug-in is a now solved problem. Daren's intention is to continue publishing samples of what you can do with it pretty regularly in the future. The source code is available under the GNU General Public License v3 from a Subversion repository on code.google.com. It is documented, though there is currently little other stand-alone documentation. One would probably want to look for the IExternalApplication entry point and go from there. What the application does is: Add a button to the Revit ribbon to start the python shell. Execute a python script entered in a text box, with output going to a separate window. Daren describes one of the main scripts like this: it " will open up an interactive interpreter loop known as a REPL that will let you explore the Revit API. And by explore, I mean type a statement, hit enter, see the results, carry on. It doesn't get easier as this! " Scripting ADAM with IronPython IronPython is an implementation of the Python language for the .NET framework, using the new Dynamic Language Runtime (DLR) as language services layer on top of the Common Language Runtime (CLR). The DLR's reusable hosting model makes it extremely suitable to add scripting capabilities to existing products. In this post we'll create an ADAM command line utility that will allow us to run IronPython scripts against an ADAM application. ... Our command line tool will expose a single command, RunScript, that will take in ADAM connection information and a path to a Python file to execute: