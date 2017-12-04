var rideModel = require('./rideModel');
var generics = require('./genericFunctions');
var messages  = require('./messages');
var userProfileModel = require("./userProfileModel");

exports.thankDriver = function(req, res)
{
    var rideId = req.params.rideId;
    var userId = req.params.userId;

    rideModel.findById(rideId).select('driverProfileId').exec(
    function callback(err, doc)
    {
        if (err)
        {
            console.log("Problem fetching ride " + rideId);
            return res.status(500).send(generics.wrappedResponse("error", 'Problem fetching ride: ' + err.message));
        }
        else if (!doc)
        {
            console.log("No document " + rideId + " was found");
            return res.status(400).send(generics.wrappedResponse("error", "No document " + rideId + " was found"));
        }
        else
        {
            messages.removeMessageFromUserProfile(userId, rideId, "thanks",
            function(err, hitcherName)
            {
                if(err) return res.status(500).send(generics.wrappedResponse("error", 'Problem removing message from user profile ' + userId));
                else
                {
                    userProfileModel.findByIdAndUpdate(doc.driverProfileId, {$inc:{rating:10}},
                    function(err, updatedDoc)
                    {
                        if(err || !updatedDoc) return res.status(500).send(generics.wrappedResponse("error", 'Problem adding rating to  ' + doc.driverProfileId));
                        else {
                          // send notification to driver
                          generics.sendNotificationToUserWithMessage(doc.driverProfileId, rideId, hitcherName + " thanks you for the ride!", "event",
                          function(err)
                          {
                            if(err)
                              console.log('Failed send thanks notification to driver: ' + doc.driverProfileId);
                            return res.status(200).send(generics.wrappedResponse("success", "'Thanks' has successfully added rating to driver", doc));
                          });
                        }

                    });
                }
            });
         }
     });

}
