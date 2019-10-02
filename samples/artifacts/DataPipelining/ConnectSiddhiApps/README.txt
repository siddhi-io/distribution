Purpose:
    This demonstrates how to connect multiple Siddhi Apps deployed under the same Siddhi Manager. The application "ConnectSiddhiAppsSink" publishes events and "ConnectSiddhiAppsSource" application consumes the published events and log the events in OutputStream to the output console.

Prerequisites:
    1) Save the samples

Executing the Sample:
    1) Start the Siddhi applications by clicking on 'Run'.
    2) If the Siddhi application starts successfully, the following messages would be shown on the console.
        * ConnectSiddhiAppsSink.siddhi - Started Successfully!
	    * ConnectSiddhiAppsSource.siddhi - Started Successfully!

Testing the Sample:
    You may send events via event simulator
        a) Open the event simulator by clicking on the second icon or pressing Ctrl+Shift+I.
        b) In the Single Simulation tab of the panel, specify the values as follows:
            * Siddhi App Name  : ConnectSiddhiAppsSink
            * Stream Name      : SweetProductionStream
        c) In the name and amount fields, enter the following and then click Send to send the event.
            name: chocolate cake
            amount: 50.50
        d) Send some more events.

Viewing the Results:
    INFO {io.siddhi.core.stream.output.sink.LogSink} - ConnectSiddhiAppsSource : OutputStream : Event{timestamp=1569997627918, data=[chocolate cake, 50.5], isExpired=false}

Note:
    Both "ConnectSiddhiAppsSink" app and "ConnectSiddhiAppsSource" app should be running at the same time in order to view the results on the console.
