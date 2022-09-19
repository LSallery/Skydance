# Skydance
Skydance lighting control driver in Kotlin.
This program allows you to turn a Skydance light in a specific zone, on and off. I have written this code in Kotlin for performance though the actual network payload is quite small.
## Installation
Follow the steps below if you're using Gradle, if not you may find this link helpful [^1].

#### Step 1: Add the JitPack repository to your build file.
Add it in your root build.gradle at the end of repositories.

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
    
#### Step 2: Add the JitPack repository to your build file.

    dependencies {
	        implementation 'com.github.lsallery:skydance:0.0.1'
	}
    
## Usage

When running the program, specify the zone (as an Int), whether you want the light on (1) or off (0) and an IP address (otherwise the program will default to a hardcoded IP address - you can easily change this).

Example program arguments: **1 0 192.173.1.135** - _This example will turn the Skydance light connected to the IP address 192.173.1.135 in zone 1 off._

This library was based on the python library by tomasbedrich [^2].

[^1]: https://jitpack.io/#lsallery/skydance/0.0.1
[^2]: https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax#footnotes
