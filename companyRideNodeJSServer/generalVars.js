exports.port = (process.env.NODE_SERVER_PORT || 3000);
exports.host = ('0.0.0.0' || process.env.NODE_SERVER_HOST || 'localhost');

'use strict';

var os = require('os');
var ifaces = os.networkInterfaces();
//var ipAddress = "192.168.2.108"
var ipAddress = "ec2-54-194-91-81.eu-west-1.compute.amazonaws.com";

Object.keys(ifaces).forEach(function (ifname)
{
    var alias = 0;

    ifaces[ifname].forEach(function (iface)
    {
        if ('IPv4' !== iface.family || iface.internal !== false)
        {
            // skip over internal (i.e. 127.0.0.1) and non-ipv4 addresses
            return;
        }

        if (alias >= 1)
        {
          // this single interface has multiple ipv4 addresses
          console.log(ifname + ':' + alias, iface.address);
        }
        else
        {
          // this interface has only one ipv4 adress
          console.log(ifname, iface.address);
    	  // this one for debug purposes only for WIFI networks
    	  if (ifname.indexOf("Wi-Fi") > -1)
          {
              ipAddress = iface.address;
              console.log("IP Address assigned = '" + exports.serverIp + "'");
    	  }
        // if (ifname.indexOf("eth0") > -1){
        //   ipAddress = iface.address;
        //   console.log("IP Address assigned = '" + exports.serverIp + "'");
        // }
        }
    });
});

//exports.serverIp = "52.4.64.206:3000";
exports.serverIp = ipAddress + ":3000";
exports.profilePicturePath = "http://"+ ipAddress + ":3000" + "/profilePictures/" ;
console.log("IP Address assigned = '" + exports.serverIp + "'");
exports.gcmServerFullPath = "https://android.googleapis.com/gcm/send";
exports.gcmAPIKey = "AIzaSyDb5eMIrHOw42_MPqdObBvnHW6JyfhPRPY";
