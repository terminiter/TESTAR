# ![logo](/resources/logos/TESTAR.png)
>AUTOMATED ROBUSTNESS TESTING AT THE UI LEVEL:

* START TESTING INMEDIATELY: With TESTAR, you can start testing immediately!! TESTAR automatically generates and executes test sequences based on a structure that is automatically derived from the UI through the accessibility API. TESTAR can detect the violation of general-purpose system requirements through implicit oracles.

* BUILD REQUIREMENTS INCREMENTALLY: While your application is automatically being tested for stability, crashes and undesired outputs, you can start adding more and more oracles that test more specific requirements of our application. This way we incrementally create the requirements, this is something that turns out to be very helpful when dealing with legacy systems.

* NO TEST SCRIPTS: No test scripts are recorded!, so no test script maintenance! Tests are generated and executed on the fly! When your UI changes, so do your tests!

**website**: http://webtestar.dsic.upv.es

**LICENSE**: TESTAR is distributed FREE of charge as an open source project under the [BSD-3 license](http://opensource.org/licenses/BSD-3-Clause)

<hr>

## Releases

**TESTAR v1.2**: https://github.com/STaQ-PROS-UPV/TESTAR/releases/tag/1.2

Requirements:
* Java JDK/JRE 1.8 x64 (protocol compilation will require JDK)
* tools/graphviz-2.38 (optional, dot.exe is used for .dot to .svg graphs conversion)

What is new?:
* Test protocol filtering addon
* SUT UI exploration curve
* Abstracted minimal graphs, and more:
http://webtestar.dsic.upv.es/upcoming/abstract_minimal_graph.svg
* and more

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
* build:

run "ant" on /testar

* run:

run "testar.bat" on /testar/target

* clean:

run "ant cleanall" on /testar

## TESTAR API:
* core:

http://htmlpreview.github.io/?https://github.com/STaQ-PROS-UPV/TESTAR/blob/master/core/doc/index.html

* windows:

http://htmlpreview.github.io/?https://github.com/STaQ-PROS-UPV/TESTAR/blob/master/windows/doc/index.html

* testar:

http://htmlpreview.github.io/?https://github.com/STaQ-PROS-UPV/TESTAR/blob/master/testar/doc/index.html
