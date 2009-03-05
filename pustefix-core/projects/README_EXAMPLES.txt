### Basic examples for the Pustefix Framework ###

1)  After unpacking the tar to the projects folder move the src folder
    to skel/src.
    Type in "ant clean" and afterwards "ant all" or just "ant" (you'll 
    find the build file in the skel directory).
    Running ant is required in order to create all necessary files making the
    examples run.

2)  Restart tomcat

3)  Type http://simplepage.HOSTNAME.DOMAIN
	http://simplelink.HOSTNAME.DOMAIN or 
	http://simpleform.HOSTNAME.DOMAIN as an 
	url in your browser and you will see the chosen sample application

The tars content:
->  simplepage: It contains just one site in order to show the absolutly
    necessary files.

->  simplelink: How to create a simple link with pustefix? After examining this
    project, you will know much more about this exiting topic ;o)

->  simpleform: A basic task for dynamic web-applications is IMHO to process forms
    in an easy way. 
    Take a short look into this example if you want to know more about form-handling 
    with pustefix. This example describes also the main classes and interfaces to
    handle form-requests.
