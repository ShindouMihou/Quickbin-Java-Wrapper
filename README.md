[![](https://jitpack.io/v/Quickbin/Quickbin-Java-Wrapper.svg)](https://jitpack.io/#Quickbin/Quickbin-Java-Wrapper)
# Quickbin Java Wrapper
This is the official simple asynchronous implementation of the entire Quickbin Public Application Interface which is documented on [https://docs.quickbin.pw](https://docs.quickbin.pw) which
available for any projcets above or equal to JDK 1.8.

#### Requirements
* JDK 1.8
* An email address (to retrieve the token and custom user-agent)

#### Installation
We will be using Jitpack to install the wrapper.

###### Maven

1. Add the JitPack repository to your build file 
```xml
<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

2. Add the dependency
```xml
	<dependency>
	    <groupId>com.github.Quickbin</groupId>
	    <artifactId>Quickbin-Java-Wrapper</artifactId>
	    <version>v1.0.0</version>
	</dependency>
```

###### Gradle 

1.Add it in your root build.gradle at the end of repositories:
```gradle
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

2. Add the dependency
```gradle
  dependencies {
	        implementation 'com.github.Quickbin:Quickbin-Java-Wrapper:v1.0.0'
	}
```

#### Usage

###### Extra notes before moving forward.
* Always use `exceptionally` to retrieve the exceptions, so you can know when you are hitting the 1,000 requests a day limit (or 5 requests per minute for requests without tokens).


###### Requesting a token
```java
String email = ...;

// This will send a request telling the Public API to send an mail towards your email
// containing your token and custom user-agent to use when accessing the site, please keep
// them in a secure place.
TokenRequest token = new TokenRequestBuilder().setEmail(email).build().request()
  .thenAccept(unused -> System.out.println("Email received!"))
  .exceptionally(throwable -> { System.out.println("Error received: " + throwable.getMessage()); return null; });
```


###### Notes before we move towards creating, editing and deleting a bin.
* Please use only one instance of TokenRequest and QuickBin since a single instance can manage all the threads by itself.
* You can only request one token per email, so make sure that token and custom user-agent is saved somewhere safe.


###### Creating a bin
```java
// The token retrieved from your email.
String token = ...;

// The agent retrieved from your email.
String agent = ...;

// Store this as a public static somewhere if possible, don't make multiple instances since it would be a waste.
static Quickbin quickBin = new QuickBinBuilder().setToken(token).setAgent(agent).build();

// The content of the bin (e.g. System.out.println("Hello World");)
String content = ...;

// The programming language of the content (e.g. java)
String language = ...;

quickBin.createBin(content, language)
.thenAccept(bin -> {
  System.out.println("Bin created at: " + bin.getFullUrl());
  System.out.println("Bin ID: " + bin.getBinKey());
  System.out.println("Bin language: " + bin.getLanguage());
}).exceptionally(throwable -> { System.out.println("Error received: " + throwable.getMessage()); return null; });
```

###### Deleting a bin
```java
// We will be reusing the created quickBin variable from Creating a Bin.

// The bin identifier (https://quickbin.pw/view/{identifier}).
String bin = ...;

quickBin.deleteBin(bin).thenAccept(unused -> System.out.println("The bin was deleted successfully")))
.exceptionally(throwable -> { System.out.println("Error received: " + throwable.getMessage()); return null; });
```

###### Deleting a bin
```java
// We will be reusing the created quickBin variable from Creating a Bin.

// The bin identifier (https://quickbin.pw/view/{identifier}).
String bin = ...;

// The new (or old) content of the bin.
String content = ...;

// The new (or old) language of the bin.
String language = ...;

quickBin.editBin(bin, content, language)
  .thenAccept(unused -> System.out.println("The bin was edited successfully"))
  .exceptionally(throwable -> { System.out.println("Error received: " + throwable.getMessage()); return null; });
```

## Future Plans of the API
* Optional content and language for `editBin` but requires either one to exist. (not implemented in Public API yet).
