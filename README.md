# android-bind-service-implementation
Developed two android applications as part of mobile development course at Grad school for implementation of Bind services in Android

### FedCash
This app acts as a client and provides the following three functionalities to fetch data from the treasury.io website:
1. Takes a particular year as input from 2006 to 2016 (both inclusive) and returns a list of values denoting the cash the US government had at the opening of each month
2. Takes a particular year, month, day and a number denoting the number of working days and fetches the values denoting cash the US government has at the opening of the specified day and the subsequent days denoted by the fourth parameter
3. Takes a particular year as input from 2006 to 2016 (both inclusive) and fetches the avg cash in hand value for the specified year

### TreasuryServ 
This apps defines a bind service that the FedCash app connects to and this app is provides the API for the FedCash application to call methods call appropriate methods in order to fetch the output for the above functionalities

Here, I have used AIDL to define the common interface that TreasuryServ implements and FedCash uses in order to make the calls to the bind service and fetch data from the treasury.io website
