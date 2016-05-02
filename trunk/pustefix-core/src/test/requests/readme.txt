Mock pustefix requests to allow higher-level testing. If we start writing
tests based on this code, we should merge it into test/junit and dump 
this directory.

The classes in this directory were contributed by cristi.mocanu@schlund.ro,
here's the respective email they sent:


Hallo Jens !

We've been doing some unit testing to our pustefix classes and we've
created a PustefixTestCase class that can be used by any test case that
requires a pustefix environment for testing purposes. Feel free to throw
them away but if you think that this class could be useful to other
people we would be happy to share it with the pustefix community. Also
feel free to change anything.

Regards,
Cristi



