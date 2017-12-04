var rideModel = require('./rideModel');
var generics = require('./genericFunctions');
var genVars  = require('./generalVars');
var rideRequestModel = require("./rideRequestModel");
var userProfileModel = require("./userProfileModel");
var ObjectId = require('mongoose').Types.ObjectId;
var messages  = require('./messages');
var rideRequestSplitter = require("./rideRequestSplitter");
var mongo = require('mongodb');
var enums = require('./enums');


exports.getRideById = function(req, res)
{
    var id = req.params.id;
    console.log("Fetching ride " + id);

    exports.getRideFromDB(id, function callback(err, doc)
    {
        if (err)
        {
            console.log("Problem fetching ride " + id);
            return res.status(500).send(generics.wrappedResponse("error", 'Problem fetching ride: ' + err.message));
        }
        else if (!doc)
        {
            console.log("No document " + id + " was found");
            return res.status(400).send(generics.wrappedResponse("error", "No document " + id + " was found"));
        }
        else
        {
            return res.status(200).send(generics.wrappedResponse("success", "Successfully fetched a ride", doc));
        }
     });
}


exports.getRideFromDB = function(id, callback)
{
    rideModel.findById(id).select("-__v").exec(
    function (err, doc)
    {
          if (err)
          {
              console.log("Problem fetching ride " + id);
              callback('Problem fetching ride: ' + err.message);
          }
          else if (!doc)
          {
              console.log("No ride document " + id + " was found");
              callback();
          }
          else
          {
            callback(null, doc)
          }
    });
}


exports.removeRide = function(req,res)
{
    var id = req.params.id;
    console.log("Driver removes ride " + id);
}

exports.stopParticipateInRide = function(req,res)
{
    var rideId = req.params.rideId;
    var hitcherId = req.params.hitcherId; //hitcher user profile Id
    console.log("Hitcher exiting ride " + id);

    checkIfDetailsAreFullIfNotReturn(req, res);

    rideModel.findById(rideId).select('status driverRideReqId driverProfileId hitchers').exec(function callback(err, doc)
    {
        if (err)
        {
            console.log("Problem fetching ride" + rideId);
            return res.status(500).send(generics.wrappedResponse("error", "Problems in db connection"));
        }
        else if(!doc)
        {
            console.log("No document " + rideId + " was found");
            return res.status(400).send(generics.wrappedResponse("error", "No document " + rideId + " was found"));
        }
        else
        {
            hitcherDoc = retrieveHitcherFromHitcherArray(doc.hitchers, hitcherId);

            if (hitcherDoc === null)
              return res.status(400).send(generics.wrappedResponse("error", "Hitcher id " + hitcherId + " wasn't found in ride with id " + rideId));

            else if (doc.hitchers.length === 1 && doc.status === "inDriverApprove")
            {
                  return res.status(400).send(generics.wrappedResponse("error", "Driver should first look at hitcher"));
            }

            //else if ((doc.hitchers.length === 1 && doc.status === "inHitcherAprove") || (doc.hitchers.length > 1))
        }
    });
}



//checked for just matched partners
//if ride is new in status "inDriverApprove" and
//driver says "no" ride is removed and
//ride requests of both driver and hitcher returnes to be unmatched
//if ride isn't new, hitcher is removed and its ride request returnes to be unmatched
//Either way, driverId is entered to hitcher ride request inconvenient array
exports.driverDisaproveHitcher = function(req, res)
{
    var rideId = req.params.rideId;
    var hitcherId = req.params.hitcherId; //hitcher user profile Id

    checkIfDetailsAreFullIfNotReturn(req, res);

    rideModel.findById(rideId).select('status driverRideReqId driverProfileId hitchers').exec(function callback(err, doc)
    {
        if (err)
        {
            console.log("Problem fetching ride" + rideId);
            return res.status(500).send(generics.wrappedResponse("error", "Problems in db connection"));
        }
        else if(!doc)
        {
            console.log("No document " + rideId + " was found");
            return res.status(400).send(generics.wrappedResponse("error", "No document " + rideId + " was found"));
        }
        else
        {
            console.log("Looking for hitcher: " + hitcherId + " in ride: " + rideId);
            hitcherDoc = retrieveHitcherFromHitcherArray(doc.hitchers, hitcherId);

            if (hitcherDoc === null)
                return res.status(400).send(generics.wrappedResponse("error", "Can't disapprove hitcher that wasn't matched"));

            else if (doc.hitchers.length === 1 && doc.status === "inHitcherAprove")
          	{
                return res.status(400).send(generics.wrappedResponse("error", "Can't disapprove already approved hitcher"));
            }


            else if ((doc.hitchers.length === 1 && doc.status === "inDriverApprove") || (doc.hitchers.length > 1))
			{
                messages.removeMessageFromUserProfile(doc.driverProfileId, rideId, "newRide",
                function(err)
                {
                    if (err) return res.status(500).send(generics.wrappedResponse("error", "Can't remove driver message"));
                    else
                    {
                        if (doc.hitchers.length === 1 && doc.status === "inDriverApprove")
                            deleteMatchedRideAsNeverHappendAndExit(req, res, rideId, doc.driverRideReqId, hitcherDoc.hitcherRideReqId, doc.driverProfileId);
                        else
                        {
                            pushProfileIdToInconvenientArray(doc.driverProfileId,hitcherDoc.hitcherRideReqId,
                            function(err)
            				{
                                if (err) return res.status(500).send(generics.wrappedResponse("error", "Can't set companions as inconvenient"));
                                else
                                {
                                    removeHitcherFromDocument(hitcherId, rideId,
                                    function(err)
                                    {
                                        if (err) return res.status(500).send(generics.wrappedResponse("error", "Can't set companions as inconvenient"));
                                        else
                                        {
                                            rideRequestModel.findOneAndUpdate({ _id: hitcherDoc.hitcherRideReqId},{$set: { "status": "unmatched" }},
                                            function(err, updatedDoc)
                                            {
                                                if (err || !updatedDoc)
                                                {
                                                  console.log('Error updating ride request: ' + hitcherDoc.hitcherRideReqId);
                                                  res.status(500).send(generics.wrappedResponse('error', 'Failed to update hitcher ride request document: ' + hitcherDoc.hitcherRideReqId));
                                                }
                                                else
                                                {
                                                    return res.status(200).send(generics.wrappedResponse("success", "Hitcher was successfully removed"));
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }

            else return res.status(422).send(generics.wrappedResponse("error", "Invalid state in document " + rideId));
        }
    });
}

//checked
//retrieves hitcher document from hitcher array
function retrieveHitcherFromHitcherArray(hitchersArray, hitcherId)
{
    var id = new ObjectId(hitcherId);

    for (var i = 0; i < hitchersArray.length; i++)
    {
        if (hitchersArray[i].userProfileId.equals(id))
            return hitchersArray[i];
    }
    return null;
}

exports.setHitcherDisapprovement = function(req, res)
{
    var rideId = req.params.rideId;
    var hitcherId = req.params.hitcherId; //hitcher user profile Id

    checkIfDetailsAreFullIfNotReturn(req, res);

    rideModel.findById(rideId).select('status driverRideReqId driverProfileId hitchers').exec(function callback(err, doc)
    {
        if (err)
        {
            console.log("Problem fetching ride" + rideId);
            return res.status(500).send(generics.wrappedResponse("error", "Problems in db connection"));
        }
        else if(!doc)
        {
            console.log("No document " + rideId + " was found");
            return res.status(400).send(generics.wrappedResponse("error", "No document " + rideId + " was found"));
        }
        else
        {
            hitcherDoc = retrieveHitcherFromHitcherArray(doc.hitchers, hitcherId);

            if (hitcherDoc === null)
              return res.status(400).send(generics.wrappedResponse("error", "Can't disapprove ride that wasn't matched"));

            else if (doc.hitchers.length === 1 && doc.status === "inDriverApprove")
          	{
                  return res.status(400).send(generics.wrappedResponse("error", "Driver should first look at hitcher"));
            }

            else if ((doc.hitchers.length === 1 && doc.status === "inHitcherAprove") || (doc.hitchers.length > 1))
			{
                messages.removeMessageFromUserProfile(hitcherId, rideId, "newRide",
                function(err)
                {
                    if (err)  return res.status(500).send(generics.wrappedResponse("error", "Failed to remove message from hitcher " + hitcherId));
                    else
                    {
                        pushProfileIdToInconvenientArray(doc.driverProfileId,hitcherDoc.hitcherRideReqId,
                        function callback(err)
        				{
                            if (err) return res.status(500).send(generics.wrappedResponse("error", "Failed to push driver to inconvenient users "));
                            else
                            {
                                if (doc.hitchers.length > 1)
                                {
                                    removeHitcherFromDocument(hitcherId, rideId,
                                    function (err)
                                    {
                                        rideRequestModel.findOneAndUpdate({ _id: hitcherDoc.hitcherRideReqId},
                                        {$set: { "status": "unmatched" }},
                                        function(err, updatedDoc)
                                        {
                                            if (err)
                                            {
                                              console.log('Error updating ride request: ' + hitcherDoc.hitcherRideReqId);
                                              res.status(422).send(generics.wrappedResponse('error', 'Failed to update hitcher ride request document: ' + hitcherDoc.hitcherRideReqId));
                                            }
                                            else
                                            {
                                                return res.status(200).send(generics.wrappedResponse("success", "Hitcher was successfully removed"));
                                            }
                                        });
                                    });
                                }
                                else
                                {
                                    deleteMatchedRideAsNeverHappendAndExit(req, res, rideId, doc.driverRideReqId, hitcherDoc.hitcherRideReqId,doc.driverProfileId);
                                }
                             }
                        });
                    }
                });
            }

            else return res.status(422).send(generics.wrappedResponse("error", "Invalid state in document " + rideId));
        }
    });
}

exports.setHitcherApprovement = function(req, res)
{
    var rideId = req.params.rideId;
    var hitcherId = req.params.hitcherId; //hitcher user profile Id

    checkIfDetailsAreFullIfNotReturn(req, res);

    console.log('Updating ride: ' + rideId + ' with approvement of hitcher ' + hitcherId);

    rideModel.findById(rideId).select('driverProfileId driverRideReqId weekday hitchers maxNumOfHitchers minPickUpHour status maxPickUpHour').exec(function callback(err, doc)
    {
        if (err)
        {
            console.log("Problem fetching ride" + rideId);
            return res.status(500).send(generics.wrappedResponse("error", "Problems in db connection"));
        }
        if (!doc)
        {
            console.log("No document " + rideId + " was found");
            return res.status(400).send(generics.wrappedResponse("error", "No document " + rideId + " was found"));
        }
        else
        {
            var oldRideStatus = doc.status;
            console.log(doc.status);
            var hitcherDoc = retrieveHitcherFromHitcherArray(doc.hitchers, hitcherId);

            if (hitcherDoc === null)
                return res.status(400).send(generics.wrappedResponse("error", "You Can't approve ride that that wasn't matched for you"));

            if (hitcherDoc.status === "waitingForDriverApprovement")
          	{
                return res.status(400).send(generics.wrappedResponse("error", "Driver should approve first"));
            }

            //new vars
            var rideStatus = (doc.hitchers.length === doc.maxNumOfHitchers) ? "activeFull": "activeNotFull";

            rideModel.update({ _id: rideId, hitchers: { $elemMatch:{userProfileId: hitcherId}}},
            { $set:{
                'status': rideStatus,
                'hitchers.$.status': "approved"
                }},
            function(err, updatedDoc)
            {
                if (err || !updatedDoc)
                {
                    console.log('Error updating ride: ' + rideId);
                    res.status(500).send(generics.wrappedResponse('error','Error updating ride: ' + rideId));
                }
                else
                {
                    messages.removeMessageFromUserProfile(hitcherId, rideId, "newRide",
                    function(err)
                    {
                        if (err) res.status(500).send(generics.wrappedResponse('error',"Failed to remove message from hitcher profile " + hitcherId));
                        var message =
                        {
                            "type" : "newRide",
                            "message" :"Hitcher approved a ride. Get started and enjoy!",
                            "rideId" : new ObjectId(rideId)
                        }
                        messages.sendMessageToUserProfile(message, doc.driverProfileId,
                        function(err)
                        {
                            if (err) res.status(500).send(generics.wrappedResponse('error','Error updating driver ' + doc.driverProfileId +  'about a ride : ' + rideId));
                            else
                            {
                                console.log('Driver ' + doc.driverProfileId + " was successfully updated");

                                splitRideRequest(rideId, hitcherDoc.hitcherRideReqId, doc.weekday,
                                function (err)
                                {
                                    if (err) res.status(500).send(generics.wrappedResponse('error','Error splitting ride request date range of hitcher'));
                                    else
                                    {
                                      console.log("Old ride status" + oldRideStatus);
                                        if (oldRideStatus === "inHitcherAprove")
                                        {
                                            splitRideRequest(rideId, doc.driverRideReqId,doc.weekday,
                                            function (err)
                                            {
                                                if (err) res.status(500).send(generics.wrappedResponse('error','Error splitting ride request date range of driver'));
                                                else
                                                {
                                                    updateMinimumMaximumPickUpTime(rideId);
                                                    generics.sendNotificationToUserWithMessage(doc.driverProfileId, rideId, "Your ride was approved by hitcher", "message",
                                                    function(err)
                                                    {
                                                      if(err)
                                                        console.log('Failed send notification to driver: ' + doc.driverProfileId);

                                                     return res.status(200).send(generics.wrappedResponse("success", "Ride was successfully approved"));
                                                    });
                                                }
                                            });
                                        }
                                        else return res.status(200).send(generics.wrappedResponse("success", "Ride was successfully approved"));
                                    }
                                });
                            }
                        });
                    });
                }
            });
        }
    });
}

function updateMinimumMaximumPickUpTime(rideId)
{
    var rideId = mongo.ObjectID(rideId);

    rideModel.aggregate(
    [
        { "$match" : {"_id" : rideId}},
        { "$unwind": "$hitchers" },
        { "$group":
            {
                '_id':'$_id' ,
                'min': {'$min': "$hitchers.pickUp.time"} ,
                'max': {'$max': "$hitchers.pickUp.time" }
            }
        }
    ],
   function(err,result)
   {
       if (err)
       {
           console.log(JSON.stringify(err));
        }

       else
       {
           rideModel.update({ _id: rideId},
           { $set : {'maxPickUpHour': result[0].max, 'minPickUpHour': result[0].min}},
               function(err, updatedDoc)
               {
                   if (err || !updatedDoc)
                   {
                      console.log('Failed to update min max document ' + rideId);
                   }
                   else
                   {
                        console.log('Successfully updated min max in document ' + rideId);
                   }
               });
       }
       console.log(JSON.stringify(result));
   });
}


exports.setHitcherPickUpDropDetailsApprove = function(req,res)
{
    var rideId = req.params.rideId;
    var hitcherId = req.params.hitcherId; //hitcher user profile Id

    checkIfDetailsAreFullIfNotReturn(req, res);

    console.log('Updating ride: ' + rideId + ' with pickup drop details approvement of hitcher ' + hitcherId);

    rideModel.findById(rideId).select('driverProfileId driverRideReqId hitchers maxNumOfHitchers minPickUpHour status maxPickUpHour').exec(function callback(err, doc)
    {
        if (err)
        {
            console.log("Problem fetching ride" + rideId);
            return res.status(500).send(generics.wrappedResponse("error", "Problems in db connection"));
        }
        if (!doc)
        {
            console.log("No document " + rideId + " was found");
            return res.status(400).send(generics.wrappedResponse("error", "No document " + rideId + " was found"));
        }
        else
        {
            var hitcherDoc = retrieveHitcherFromHitcherArray(doc.hitchers, hitcherId);

            if (hitcherDoc === null)
                return res.status(400).send(generics.wrappedResponse("error", "You Can't approve ride that that wasn't matched for you"));

            if (hitcherDoc.status != "inPickUpDropDetailsApprove")
            {
                return res.status(400).send(generics.wrappedResponse("error", "Status of hitcher is not inPickUpDropDetailsApprove"));
            }

            rideModel.update({ _id: rideId, hitchers: { $elemMatch:{userProfileId: hitcherId}}},
            { $set:{
                'hitchers.$.status': "approved"
                }},
            function(err, updatedDoc)
            {
                if (err || !updatedDoc)
                {
                    console.log('Error updating ride: ' + rideId);
                    res.status(500).send(generics.wrappedResponse('error','Error updating ride: ' + rideId));
                }
                else
                {
                    messages.removeMessageFromUserProfile(hitcherId, rideId, "newRide",
                    function(err)
                    {
                        if (err) res.status(500).send(generics.wrappedResponse('error',"Failed to remove message from hitcher profile " + hitcherId));
                        else
                        {
                            updateMinimumMaximumPickUpTime(rideId);
                            generics.sendNotificationToUserWithMessage(doc.driverProfileId, rideId, "Change of pick up/drop details was approved by hitcher", "message",
                            function(err)
                            {
                              if(err)
                                console.log('Failed send notification to driver: ' + doc.driverProfileId);

                              return res.status(200).send(generics.wrappedResponse("success", "Ride  pick up and drop details was successfully approved"));
                            });
                        }
                    });
                }
            });
        }
    });
}


function splitRideRequest(rideId, rideReqId,weekday, callback)
{
    rideRequestSplitter.splitRideRequestAfterRideCreation(rideId, rideReqId, weekday,
    function (err)
    {
        if (err) callback(err);
        else callback(null);
    });
}

//checked
function deleteMatchedRideAsNeverHappendAndExit(req, res, rideId, driverRideReqId, hitcherRideReqId, driverProfileId)
{
    rideRequestModel.findOneAndUpdate({ _id: hitcherRideReqId }, { $set: {"status": "unmatched"},$push: {"inconvenientUsers": driverProfileId}},
    function(err, updatedDoc)
    {
        if (err || !updatedDoc)
        {
            console.log('Error updating ride request: ' + hitcherRideReqId);
            return res.status(500).send(generics.wrappedResponse('error', 'Failed to update hitcher ride request document: ' + hitcherRideReqId));
        }
        else
        {
            rideRequestModel.findOneAndUpdate({ _id: driverRideReqId}, { $set: {"status": "unmatched"}},
            function(err, updatedDoc)
            {
                if (err || !updatedDoc)
    			{
                    console.log('Error updating ride request: ' + driverRideReqId);
                    return res.status(500).send(generics.wrappedResponse('error', 'Failed to update driver ride request document: ' + driverRideReqId));
                }
    			else
    			{
                  	rideModel.findOneAndRemove({_id: rideId},
                    function(err, updatedDoc)
                    {
                         if (err || !updatedDoc)
                         {
                            console.log('Failed to remove ride document: ' + rideId);
                            return res.status(500).send(generics.wrappedResponse('error', 'Failed to remove ride document: ' + rideId));
                         }
                         else
                         {
                              return res.status(200).send(generics.wrappedResponse("success", "Ride was successfully removed"));
                         }
                    });
                }
            });
        }
    });
}

//checked
function pushProfileIdToInconvenientArray(inconvenientUserProfileId, rideReqIdToUpdate, callback)
{
    rideRequestModel.findByIdAndUpdate( rideReqIdToUpdate,
                                        { $push: {"inconvenientUsers": new ObjectId(inconvenientUserProfileId) }},
                                        { safe: true, new: true},
        function(err, doc)
        {
            if (err)
            {
                console.log("failed to push inconvenient user " + err);
                callback(err, null);
            }
            else
            {
                console.log("inconvenient user profile " + inconvenientUserProfileId + "was added to " + rideReqIdToUpdate);
                callback(null, doc);
            }
        }
    );
}


function checkIfDetailsAreFullIfNotReturn(req, res)
{
    var rideId = req.params.rideId;
    var hitcherId = req.params.hitcherId;
    if (!rideId)
    {
        console.log('setPickUpDetails: Invalid rideId:  ' + rideId);
        return res.status(400).send(generics.wrappedResponse("error", 'Invalid rideId:  ' + rideId));
    }
    if (!hitcherId)
    {
        console.log('setPickUpDetails: Invalid hitcherNumber:  ' + hitcherId);
        return res.status(400).send(generics.wrappedResponse("error", 'Invalid hitcherNumber:  ' + hitcherNumber));
    }
}

//driver approves hitcher by setting precise time and location of pick up
exports.setPickUpDropDetails = function(req, res)
{
    var rideId = req.params.rideId;
    var hitcherId = req.params.hitcherId;

    var pickUpDetails = req.body;
    console.log (pickUpDetails);

    checkIfDetailsAreFullIfNotReturn(req, res);

    if (!pickUpDetails || !pickUpDetails.pickUp  || !pickUpDetails.drop )
    {
        console.log('setPickUpDetails: Invalid request body:  ' + pickUpDetails);
        return res.status(400).send(generics.wrappedResponse("error", "Details of pick up should be provided for hitcher"));
    }

    console.log('Updating ride: ' + rideId + ' with pick up details of hitcher ' + hitcherId);

    rideModel.findById(rideId).select('minPickUpHour maxPickUpHour status driverProfileId driverRideReqId hitchers ').exec(function callback(err, doc)
    {
        if (err)
        {
            console.log("Problem fetching ride" + rideId);
            return res.status(500).send(generics.wrappedResponse("error", "Problems in db connection"));
        }
        if (!doc)
        {
            console.log("No document " + rideId + " was found");
            return res.status(400).send(generics.wrappedResponse("error", "No document " + rideId + " was found"));
        }
        else
	    {
            hitcherDoc = retrieveHitcherFromHitcherArray(doc.hitchers, hitcherId);

            if (hitcherDoc === null)
            return res.status(400).send(generics.wrappedResponse("error", "Can't approve hitcher that wasn't matched"));

            var docStatus = (doc.status === ("inDriverApprove")) ? "inHitcherAprove" : doc.status;
            var hitcherStatus = (hitcherDoc.status === "approved" || hitcherDoc.status === "inPickUpDropDetailsApprove") ?  "inPickUpDropDetailsApprove" : "waitingForHitcherApprovement";


            var pickUp = (pickUpDetails.pickUp) ? pickUpDetails.pickUp : hitcherDoc.pickUp;
            var drop = (pickUpDetails.drop) ? pickUpDetails.drop : hitcherDoc.drop;

            rideModel.update({ _id: rideId, hitchers: { $elemMatch:{userProfileId: hitcherId}}},
            { $set:{
                'status': docStatus,
                'hitchers.$.pickUp': pickUp,
                'hitchers.$.drop': drop,
                'hitchers.$.status': hitcherStatus
                        }},
            { safe: true, new: true},
            function(err, updatedDoc)
            {
                if (err || !updatedDoc)
                {
                    console.log('Error updating ride: ' + rideId + ". Error: " + err);
                    res.status(500).send(generics.wrappedResponse('error','Error updating ride: ' + rideId));
                }
                else
                {
                    messages.removeMessageFromUserProfile(doc.driverProfileId, rideId, "newRide",
                    function(err)
                    {
                        if (err)
                        {
                            res.status(500).send(generics.wrappedResponse('error','Error deleting message in driver profile'));
                        }
                        else
                        {
                            var messageTxt =  (hitcherDoc.status === ("waitingForDriverApprovement")) ?
                            "You have a new ride matched. Please approve driver and pick up details" :
                            "Driver changed your pick up details. Please approve";


                            var message = {
                                "type" : "newRide",
                                "message" :messageTxt,
                                "rideId" : new ObjectId(rideId)
                            }

                            var responseMessage = (hitcherDoc.status === ("waitingForDriverApprovement")) ?
                            "Ride was successfully approved" :
                            "Details of pick up were successfully updated";

                            messages.sendMessageToUserProfile(message, hitcherId,
                            function(err, doc)
                            {
                                if (err) res.status(500).send(generics.wrappedResponse('error','Error updating hitcher ' + hitcherId +  'about a ride : ' + rideId));
                                else
                                {
                                    console.log('Ride ' + rideId + " was successfully updated");
                                    updateMinimumMaximumPickUpTime(rideId);
                                    generics.sendNotificationToUserWithMessage(hitcherId, rideId, messageTxt, "message",
                                    function(err)
                                    {
                                      if(err)
                                        console.log('Failed send notification to hitcher: ' + hitcherId);

                                      return res.status(200).send(generics.wrappedResponse("success", responseMessage));
                                    });

                                }
                            });
                        }
                    });
                }
            });
        }
    });
}


function removeHitcherFromDocument(hitcherProfileId, rideId, callback)
{
    rideModel.findOneAndUpdate({"_id" : rideId},
        {$pull: {'hitchers' : {"userProfileId": hitcherProfileId }}},
        { safe: true, new: true},
        function(err, doc)
        {
            if(err || !doc) callback ("error");
            else callback(null);
        }
    );
}


function filterMatchedRides(matchedRides, month, year, rideType)
{
		var filteredDocs = [];
		matchedRides.forEach(function(doc)
		{
			var startDate = doc.startDate;
            var endDate   = doc.stopDate;
			// if the requested month and year is in between the start nd stop dates
			// then add it to the result list

			if (generics.checkMonthWithinTimeRange(startDate,endDate, year, month)) //problem here - works for the same year
					{
						filteredDocs.push({
							startDate : doc.startDate,
							stopDate  : doc.stopDate,
							eventType	: doc.eventType,
							rideType  : rideType,
							weekday 	: doc.weekday,
							rideTimeFrom  : doc.minPickUpHour,
							rideTimeTo    : doc.maxPickUpHour,
              timeOffset: doc.timeOffset,
							_id				    : doc._id
						});
					}
		});
		return filteredDocs;
};

exports.getRidesForMonth = function(req, res)
{
	var userId = req.params.userid;
	var month  = req.params.month;

	var year = parseInt(month.substring(2,6));
	month = parseInt(month.substring(0,2));

	console.log("Get ride for user: ", userId, " month: ", month, " year: ", year);

	// first look for the rider id
	rideModel.find({ driverProfileId : userId })
	         .select('startDate stopDate eventType weekday minPickUpHour maxPickUpHour timeOffset')
					 .exec(function callback(err, docs)
	{
		if(err)
		{
			console.log("Problem fetching rides for user as a driver: ", userId);
			return res.status(500).send(generics.wrappedResponse('error','Problem fetching rides for user as a driver: ' + err.message));
		}
        if(!docs)
        {
            console.log("Problem fetching rides for user as a driver: ", userId);
			return res.status(500).send(generics.wrappedResponse('error','Problem fetching rides for user as a driver'));
        }
		else
		{
			var relevantDocs = [];
			console.log("Found: " + docs.length + " documents for driver, will try to filter");
			if (docs.length > 0)
			{
				relevantDocs = filterMatchedRides(docs, month, year, "driver");
			}
			// second look for the hitcher id
			rideModel.find({ 'hitchers.userProfileId' : userId })
							 .select('startDate stopDate eventType weekday minPickUpHour maxPickUpHour timeOffset')
							 .exec(function callback(err, docs)
			{
				if(err || !docs)
				{
					console.log("Problem fetching rides for user as a hitcher. ", userId);
                    if (err) console.log(err);
					return res.status(500).send(generics.wrappedResponse('error','Problem fetching rides for user as a hitcher'));
				}
				else
				{
					console.log("Found: " + docs.length + " documents for hitcher, will try to filter");
					relevantDocs = relevantDocs.concat(filterMatchedRides(docs, month, year, "hitcher"));
					return res.status(200).send(generics.wrappedResponse("success", "Found: " + relevantDocs.length + " documents for user ", relevantDocs));
				}
			});
		}
	});
}

exports.addMessageToHitcherReceivedMessages = function(req,res)
{
    checkRideCanReceiveTheMessageIfNotReturn(req, res,
    function(driverId)
    {
        var rideId = req.params.rideId;
        var message = req.body.message;
        var hitcherId = req.params.hitcherId;

        console.log('Inserting message' + message + " to received messages of ride " + rideId + " hitcher " + hitcherId + " from driver: " + driverId);

        rideModel.update({ _id: rideId, hitchers: { $elemMatch: {userProfileId: hitcherId}}},
        { $push : {'hitchers.$.messages.received' : message}},
            function(err, updatedDoc)
            {
                if (err || !updatedDoc)
                {
                   console.log('Failed to push message to document ' + rideId);
                   return res.status(500).send(generics.wrappedResponse('error', 'Failed to push message to document ' + rideId));
                }
                else
                {
                     generics.sendNotificationToUserWithMessage(hitcherId, rideId, message,  "message",
                     function(err) {
                       if(err)
                       {
                         return res.status(500).send(generics.wrappedResponse('error', 'Failed send notification to user: ' + hitcherId));
                       }
                       else
                       {
                          return res.status(200).send(generics.wrappedResponse("success", "Message: ''" + message + "'' was successfully added"));
                       }
                     });
                }
            });
    });
}

exports.addMessageToHitcherSentMessages = function(req,res)
{
    checkRideCanReceiveTheMessageIfNotReturn(req, res,
    function(driverId)
    {
        var rideId = req.params.rideId;
        var message = req.body.message;
        var hitcherId = req.params.hitcherId;

        console.log('Inserting message' + message + " to sent messages of ride " + rideId + " hitcher " + hitcherId + " to driver: " + driverId);

        rideModel.update({ _id: rideId, hitchers: { $elemMatch: {userProfileId: hitcherId}}},
        { $push : {'hitchers.$.messages.sent' : message}},
            function(err, updatedDoc)
            {
                if (err || !updatedDoc)
                {
                   console.log('Failed to push message to document ' + rideId);
                   return res.status(500).send(generics.wrappedResponse('error', 'Failed to push message to document ' + rideId));
                }
                else
                {
                  generics.sendNotificationToUserWithMessage(driverId, rideId, message,  "message",
                  function(err) {
                    if(err)
                    {
                      return res.status(500).send(generics.wrappedResponse('error', 'Failed send notification to driver: ' + driverId));
                    }
                    else
                    {
                       return res.status(200).send(generics.wrappedResponse("success", "Message: ''" + message + "'' was successfully added"));
                    }
                  });
                }
            });
    });
}



function checkRideCanReceiveTheMessageIfNotReturn(req, res, callback)
{
    var rideId = req.params.rideId;
    var hitcherId = req.params.hitcherId;

    var message = req.body;

    if (!message)
        return res.status(400).send(generics.wrappedResponse('error','Message should be suplied'));
    console.log("Checking ride: " + rideId);
    rideModel.findById(rideId).select('status maxPickUpHour weekday stopDate startDate minPickUpHour hitchers timeOffset driverProfileId').exec(
    function(err, doc)
    {
        console.log("Found doc: " + doc);
        if (err)
        {
            console.log("Problem fetching ride" + rideId);
            return res.status(500).send(generics.wrappedResponse("error", "Problems in db connection"));
        }
        else if(!doc)
        {
            console.log("No document " + rideId + " was found");
            return res.status(400).send(generics.wrappedResponse("error", "No document " + rideId + " was found"));
        }
        else
        {
            hitcherDoc = retrieveHitcherFromHitcherArray(doc.hitchers, hitcherId);

            if (hitcherDoc === null)
                return res.status(400).send(generics.wrappedResponse("error", "Hitcher isn't connected to a ride"));

            if (doc.status !== "activeFull" && doc.status !== "activeNotFull")
                return res.status(400).send(generics.wrappedResponse("error", "Can't add messages to not active ride"));

            console.log("new Date() = " + new Date());
            console.log("new Date().getTime() = " + new Date().getTime());
            console.log("doc.stopDate = " + doc.stopDate);
            console.log("doc.stopDate.getTime() = " + doc.stopDate.getTime());
            console.log("utc date" + generics.getUTCNewDate());
            console.log("utc date getTime()" + generics.getUTCNewDate().getTime());

          /*  if (generics.getUTCNewDate() > doc.stopDate ||  generics.getUTCNewDate() < doc.startDate )
                return res.status(400).send(generics.wrappedResponse("error", "Can't add messages to ride that is in future or in past"));*/

            /*var nowTimeAsNumber = generics.getHourAsNumberFromDate(new Date());
            if (new Date().getDay() !== doc.weekday ||  nowTimeAsNumber < (doc.minPickUpHour - 2) || (doc.maxPickUpHour + 1) < nowTimeAsNumber)
                return res.status(400).send(generics.wrappedResponse("error", "Can't add messages to ride that is not happening in the near future"));
                */
        }

        //successfully got to here
        //ride can receive the message
        callback(doc.driverProfileId);
    });
}
