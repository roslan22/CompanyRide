var rideRequestModel = require("./rideRequestModel");
var enums = require('./enums');
var generics = require('./genericFunctions');
var rideRequestSplitter = require("./rideRequestSplitter");

exports.getRideReqFromDB = function(id, callback)
{
    rideRequestModel.findById(id).select("-__v").exec(
    function (err, doc)
    {
          if (err)
          {
              console.log("Problem fetching ride request" + id);
              callback('Problem fetching ride request: ' + err.message);
          }
          else if (!doc)
          {
              console.log("No ride request document " + id + " was found");
              callback();
          }
          else
          {
            callback(null, doc)
          }
    });
};

exports.getRideRequestById = function (req, res)
{
	var id = req.params.id;
	console.log("Fetching ride request " + id);

	exports.getRideReqFromDB(id, function (err, doc)
	{
		if(err)
		{
			console.log("Problem fetching ride request " + err);
			return res.status(500).send(generics.wrappedResponse('error','Problem fetching ride request ' + id));
		}
		if(!doc)
		{
			return res.status(400).send(generics.wrappedResponse('error','Cannot find ride request ' + id));
		}
		else
		{
			 return res.status(200).send(generics.wrappedResponse('success', 'Successfully fetched document: ' ,doc));
		}
	});
};

exports.addNewRideRequestFromJSON = function(newRideRequest, callback)
{
  newRideRequest.creationDate = new Date();
	newRideRequest.status = 'new';

	console.log(newRideRequest);
	if (!newRideRequest.startDate)
	{
			console.log("Error inserting new ride request: Start date should be specified");
			return callback("Start date should be specified.");
	}

	if (newRideRequest.eventType == 'one-time')
	{
    // newRideRequest.stopDate = new Date();
		// newRideRequest.stopDate.setDate(new Date(newRideRequest.startDate).getDate() + 1) ;
    console.log("Start date: " + newRideRequest.startDate);
    newRideRequest.stopDate = new Date(newRideRequest.startDate);
    console.log("Stop date before: " + newRideRequest.stopDate);
    newRideRequest.stopDate.setDate(newRideRequest.stopDate.getDate() + 1);
    console.log("Stop date after: " + newRideRequest.stopDate);
	}
	else if (newRideRequest.eventType == 'weekly')
	{
		if (!newRideRequest.stopDate)
		{
			console.log("Error inserting new ride request: Stop date should be specified for re-occuring events");
            return callback("Stop date should be specified for re-occuring events");
		}

		if ( new Date(newRideRequest.startDate) > new Date(newRideRequest.stopDate))
		{
			console.log("Error inserting new ride request: Stop date is smaller than start date!");
            return callback("Stop date is smaller than start date!");
		}
	}

	if(newRideRequest.preferredRideTime && newRideRequest.preferredRideTime.toHour < newRideRequest.preferredRideTime.fromHour)
	{
		console.log("Error inserting new ride request: Start time is greater then stop time");
        return callback("Start time is greater then stop time");
	}

	rideRequestModel.create(newRideRequest, function(err, result)
	{
		if (err)
		{
			var response = String("Error adding new ride request. " + err);
			if (!err.errors)
			{
				console.log("Error inserting new ride request:" + response);
                return callback(response);
			}
			else generics.getSchemaValidationErrors(err.errors, function (schemaErrorsString)
			{
				console.log("Error inserting new ride request:" + response + schemaErrorsString);
                return callback(response + schemaErrorsString);
			});
		}
		else if (!result)
		{
            return callback('An error has occurred while adding new ride request');
		}
        else{
                console.log("Result of creating new doc: " + result);
                return callback(null, result._id);
        }
	});
};

exports.addNewRideRequest = function (req, res)
{
	var newRideRequest = req.body;
	exports.addNewRideRequestFromJSON(
    newRideRequest, function(err){
      if (err)
      {
        return res.status(400).send(generics.wrappedResponse('error',err));
      }
      else
      {
        return res.status(200).send(generics.wrappedResponse('success','Successfully added new ride request'));
      }
    }
  );
};


exports.removeRideRequestById = function(req,res)
{
    var id = req.params.id;
	console.log("Removing ride request id: " + id);

    //TODO check first if ride request has matching ride, ask user if delete ride also

    rideRequestModel.findOneAndRemove({ _id: id}, function(err,doc)
    {
        if(err)
        {
            console.log("Problem fetching ride request ", id);
            return res.status(500).send(generics.wrappedResponse("error",'Problem fetching ride request document: ' + err.message));
        }
        if (!doc)
        {
            return res.status(400).send(generics.wrappedResponse("error", 'Ride request with id ' + id + " wasn't found"));
        }
        else
        {
            console.log(doc);
            return res.status(200).send(generics.wrappedResponse("success","Document was successfully removed", doc));
        }
    });
};


//only if ride don't exists for this ride request
//uncompleted
exports.updateRideRequest = function (req, res)
{
	var id = req.params.id;
	console.log("Update ride request id: " + id);

	var newInfo = {};
	if (req.body.eventType) newInfo.eventType = req.body.eventType;
	if (req.body.inconvenientUsers) newInfo.inconvenientUsers = req.body.inconvenientUsers;
	if (req.body.blockedUsers) newInfo.blockedUsers = req.body.blockedUsers;
  if (req.body.stopDate) newInfo.stopDate = req.body.stopDate;
	if (req.body.startDate) {
    newInfo.startDate = req.body.startDate;
    if (newInfo.eventType == 'one-time')
  	{
      console.log("Start date: " + newInfo.startDate);
      newInfo.stopDate = new Date(newInfo.startDate);
      console.log("Stop date before: " + newInfo.stopDate);
      newInfo.stopDate.setDate(newInfo.stopDate.getDate() + 1);
      console.log("Stop date after: " + newInfo.stopDate);
  	}
  }
	if (req.body.weekday) newInfo.weekday = req.body.weekday;
	if (req.body.preferredRideTime)	newInfo.preferredRideTime = req.body.preferredRideTime;
	if (req.body.maxNumOfHitchers) newInfo.maxNumOfHitchers = req.body.maxNumOfHitchers;
	if (req.body.radius) newInfo.radius = req.body.radius;
	if (req.body.from) newInfo.from = req.body.from;
	if (req.body.to) newInfo.to = req.body.to;
  console.log("New info: " + JSON.stringify(newInfo));
	if (newInfo.eventType || newInfo.inconvenientUsers || newInfo.blockedUsers || newInfo.startDate || newInfo.stopDate ||
		newInfo.weekday || newInfo.preferredRideTime || newInfo.maxNumOfHitchers || newInfo.radius ||
		newInfo.from || newInfo.to
	)
	{
		rideRequestModel.findOneAndUpdate({_id: id}, {$set: newInfo},
			function(err, doc)
			{
				if(err)
				{
					console.log("Problem fetching ride request ", id);
					return res.status(500).send(generics.wrappedResponse("error",'Problem fetching ride request document: ' + err.message));
				}
				if (!doc)
				{
					return res.status(400).send(generics.wrappedResponse("error", 'Ride request with id ' + id + " wasn't found"));
				}
				else
				{
					console.log(doc);
					return res.status(200).send(generics.wrappedResponse("success","Document was successfully updated", doc));
				}
			});
	}
	else
	{
		return res.status(400).send(generics.wrappedResponse("error", "No information to update received"));
	}
};


exports.getRideRequestsForMonth  = function(req, res)
{
	var userId = req.params.userid;
	var month  = req.params.month;
	var year = parseInt(month.substring(2,6));
	month = parseInt(month.substring(0,2));

	console.log("Get ride request for user: ", userId, " month: ", month, " year: ", year);
	rideRequestModel.find({ userProfileId : userId })
	         				.select('startDate stopDate eventType rideType weekday preferredRideTime.fromHour preferredRideTime.toHour timeOffset')
									.exec(function callback(err, docs)
	{
		if(err)
		{
			console.log("Problem fetching ride requests for user: ", userId);
			return res.status(500).send(generics.wrappedResponse('error','Problem fetching ride requests: ' + err.message));
		}
		if (!docs)
		{
			return res.status(400).send(generics.wrappedResponse('error','Cannot find ride request ' + userId));
		}
		else
		{
			var relevantDocs = [];
			docs.forEach(function(doc)
			{
				startDate = doc.startDate;
				endDate   = doc.stopDate;
				// if the requested month and year is in between the start nd stop dates
				// then add it to the result list
				if (generics.checkMonthWithinTimeRange(startDate,endDate, year, month))
				{
          var newDoc = {
						startDate : doc.startDate.toISOString(),
						stopDate  : doc.stopDate.toISOString(),
						eventType	: doc.eventType,
						rideType	: doc.rideType,
						weekday 	: doc.weekday,
						rideTimeFrom  : doc.preferredRideTime.fromHour,
						rideTimeTo    : doc.preferredRideTime.toHour,
            timeOffset: doc.timeOffset,
						_id				    : doc._id
					};
          //console.log("New doc: " + JSON.stringify(newDoc));
					relevantDocs.push(newDoc);
				}
			});

			var message = "Found: " + relevantDocs.length + " documents";
			console.log(message);
			var resultMsg = generics.wrappedResponse("success", message, relevantDocs);
			return res.status(200).send(resultMsg);
		}
	});
};

function returnDocumentsWithClosestRideDate(toDocs, initialDate, callback){
  //var currDate = new Date();
  //var newDoc, currDoc;
  var relevantDocs = [];
  console.log("Initial date: " + initialDate + " date minus: " + generics.dateMinusDay(initialDate));
  toDocs.forEach(function(doc)
  {
     var newDoc = {
       eventType	   : doc.eventType,
       rideType	     : doc.rideType,
       weekday 	     : doc.weekday,
       rideTimeFrom  : doc.preferredRideTime.fromHour,
       rideTimeTo    : doc.preferredRideTime.toHour,
       timeOffset    : doc.timeOffset,
       to            : doc.to,
       _id				   : doc._id,
       eventDate     : generics.getFirstWeekDayDateAfterDate(generics.dateMinusDay(initialDate), doc.weekday)
     };
     relevantDocs.push(newDoc);
   });
  relevantDocs.sort(function(a, b) {
      return (new Date(a.eventDate)).getTime() - (new Date(b.eventDate)).getTime();
  });
  callback(relevantDocs);
}

function findRequestInRangeOfSomeDaysSameToLocation(reqId, doc, fromDocs, initialDate, callback){
  rideRequestModel.find(
  {
    _id : { $in : fromDocs},
    userProfileId : { $ne : doc.userProfileId},
    // OR [startDate < doc.startDate < stopDate]
    // OR [doc.startDate + 7 > startDate > doc.startDate]
    $or : [
      {
        startDate : {
          $lte :  new Date(doc.stopDate),
          $gte : new Date(doc.startDate)
        }
      },
        {
        startDate : {
          $lte :  new Date(doc.startDate)
          // $gte :  new Date(new Date(doc.startDate).getTime() + 1000 * 60 * 60 * 24 * 7) // 3 days more
        },
        stopDate : {
          $gte :  new Date(doc.startDate)
        }
      }
    ],
    stopDate : {
      $gte : new Date()
    },
    "to.coordinates":
     // in range of 10km
      { $near :
       {
         $geometry: { type: doc.to.type,  coordinates: [ doc.to.coordinates.long, doc.to.coordinates.lat ] },
         $maxDistance: 10000,
         spherical: true
       }
      }
  })  .select('eventType rideType weekday preferredRideTime.fromHour preferredRideTime.toHour timeOffset to')
     .exec(
      function (err, toDocs)
      {
        if(err)
        {
           callback(null, "Problem fetching ride proposals for ride request: " + reqId + ".Error: " + err);
        }
        if (!toDocs)
        {
           callback(null);
        }
        else
        {
          returnDocumentsWithClosestRideDate(toDocs, initialDate, callback);
        }
      }
    );
}

function findRequestInRangeOfSomeDaysSameFromLocation(reqId, doc, initialDate, callbackFunc){
  rideRequestModel.distinct("_id",
  {
  _id : { $ne : reqId},
  userProfileId : { $ne : doc.userProfileId },
  rideType : { $ne : doc.rideType},
  // OR [startDate < doc.startDate < stopDate]
  // OR [doc.startDate + 7 > startDate > doc.startDate]
  $or : [
    {
      startDate : {
        $lte :  new Date(doc.stopDate),
        $gte : new Date(doc.startDate)
      }
    },
      {
      startDate : {
        $lte :  new Date(doc.startDate)
        // $gte :  new Date(new Date(doc.startDate).getTime() + 1000 * 60 * 60 * 24 * 7) // 3 days more
      },
      stopDate : {
        $gte :  new Date(doc.startDate)
      }
    }
  ],
  stopDate : {
    $gte : new Date()
  },
  "from.coordinates":
  // in range of predefined radius
   { $near :
      {
        $geometry: { type: doc.from.type,  coordinates: [ doc.from.coordinates.long, doc.from.coordinates.lat ]  },
        $maxDistance: doc.radius,
        spherical: true
      }
   }
   })  .select()
       .exec(function (err, fromDocs)
  {
    if(err)
    {
         callbackFunc(null, "Problem fetching ride proposals for ride request: " + reqId + ".Error: " + err);
    }
    if (!fromDocs)
    {
         console.log("Didn't find any potential ridesrequests for request: " + reqId);
         callbackFunc(null);
    }
    else
    {
        console.log("Found nearby 'from' requests: " + fromDocs);
        findRequestInRangeOfSomeDaysSameToLocation(reqId, doc, fromDocs, initialDate, callbackFunc);
    }
  });
}

function findRequestPotentialRides(personType, reqId, initialDate, callback){
  // first find the ride request by ID
  rideRequestModel.findById(reqId)
	         				.select('userProfileId startDate stopDate eventType rideType weekday preferredRideTime.fromHour preferredRideTime.toHour timeOffset radius to from')
									.exec(function (err, doc)
	{
		if(err)
		{
			callback(null, 'Problem fetching ride request: ' + err.message);
		}
		if (!doc)
		{
			callback(null, 'Cannot find ride request ' + reqId);
		}
		else
		{
		  console.log("Found request doc: " + JSON.stringify(doc));
      //update the initialDate with offset from the document (so 00:00 will become 22:00)
      //initialDate = generics.updateDateWithOffset(initialDate, doc.timeOffset);
      console.log("Updated initial date time with offset: " + initialDate);
      if( doc.rideType != personType) callback(null, "Request must be of " + personType + " type!");
      else findRequestInRangeOfSomeDaysSameFromLocation(reqId, doc, initialDate, callback);
		}
	}
);
}

exports.getRideRequestsPotentialRides = function(req, res)
{
    var reqId = req.params.id;
    var message = "";
    var resultMsg = "";
  var rideType = req.params.type;
  var initialDate = generics.getUTCDateFromDate(new Date(Date.parse(req.params.date)));
  console.log("Will try to find potential rides for request: " + reqId + " for the date: " + initialDate);
   findRequestPotentialRides(rideType, reqId, initialDate, function(relevantDocs, err){
     if(err)
     {
       message = "Error occured: " + err;
       resultMsg = generics.wrappedResponse("error", message);
       return res.status(500).send(resultMsg);
     }
     else if (!relevantDocs) {
       message = "No relevant potential rides were fettched for request id: " + reqId;
       resultMsg = generics.wrappedResponse("error", message);
       return res.status(200).send(resultMsg);
     }
     else{
       message = "Found: "+ relevantDocs.length + " potential rides";
       resultMsg = generics.wrappedResponse("success", message, relevantDocs);
       return res.status(200).send(resultMsg);
     }
  });
};

exports.updateSplitRideRequest = function(req, res)
{
  var reqId = req.params.id;
  var data  = req.body;
  data.startDate = new Date(data.startDate);
  data.stopDate = generics.datePlusDay(data.startDate);
  data.origReq.startDate = new Date(data.origReq.startDate);
  data.origReq.stopDate = new Date(data.origReq.stopDate);
  var message = "Received update split for request: " + reqId + " with data: " + JSON.stringify(data);
    var resultMsg = "";
  console.log(message);
  rideRequestSplitter.splitRideRequestForPotentialRide(data,
      function(err, newReqId){
        if(err)
        {
          var message = "Error occured: " + err;
          console.log(message);
          resultMsg = generics.wrappedResponse("error", message);
          return res.status(500).send(resultMsg);
        }
        else
        {
          message = "Ride request was successfully updated!"
          resultMsg = generics.wrappedResponse("success", message, { newReqId : newReqId});
          return res.status(200).send(resultMsg);
        }
      }
    );
};
