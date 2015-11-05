# ![logo](/resources/logos/TESTAR.png)
>Automated system testing of desktop, web and mobile applications at the GUI level

Test your system from the GUI:

* No more Capture/Replay script maintenance!
* No more boring testing tasks!
* Just concentrate on the important stuff and let the tool do the rest for you!
* Simple, automated, and open source: TESTAR!

**website**: http://webtestar.dsic.upv.es

**LICENSE**: TESTAR is distributed FREE of charge as an open source project under the [BSD-3 license](http://opensource.org/licenses/BSD-3-Clause)

<hr>

## Releases

**TESTAR v1.2**: upcoming
* Test protocol filtering addon
* SUT UI exploration curve
* Abstracted minimal graphs, and more:
http://webtestar.dsic.upv.es/upcoming/abstract_minimal_graph.svg
* ...

**TESTAR v1.1a**: https://github.com/STaQ-PROS-UPV/TESTAR/releases/tag/v1.1a
Requirements:
* Java JDK/JRE 1.8 x64 (protocol compilation will require JDK)
* tools/graphviz-2.38 (optional, dot.exe is used for .dot to .svg graphs conversion)

**First public release**: https://github.com/STaQ-PROS-UPV/TESTAR/releases/tag/v1.0
Requirements:
* Java JDK/JRE 1.7 x64 (protocol compilation will require JDK)

<hr>

## Building source code

Requirements: Windows 7, JDK 1.8, Apache Ant, Microsoft SDK 7.1

How to:
* build: run "ant" on /testar
* run: run "testar.bat" on /testar/target
* clean: run "ant cleanall" on /testar
